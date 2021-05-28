/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.j256.simplemagic.ContentType;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.conventions.PropertyKeys;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

@ComponentName("Archive Extractor")
@ComponentDescription(
    "Extract archive files (*.zip, *.tar.gz, etc.) and create new items from each file")
@SettingsClass(ArchiveExtractor.Settings.class)
public class ArchiveExtractor
    extends AbstractProcessorDescriptor<ArchiveExtractor.Processor, ArchiveExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withProcessesContent(InputStreamContent.class);

    if (getSettings().isRemoveSourceContent()) {
      builder = builder.withDeletesContent(FileContent.class);
    }

    return builder.build();
  }

  public static class Processor extends AbstractProcessor {
    private final ArchiveStreamFactory archiveStreamFactory = new ArchiveStreamFactory();
    private final CompressorStreamFactory compressorStreamFactory = new CompressorStreamFactory();

    private final ContentInfoUtil util = new ContentInfoUtil();

    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      // TODO: Support for other archives (7z?)

      item.getContents(FileContent.class)
          .filter(
              fc -> {
                try {
                  if (settings.getContentTypes().isEmpty()) return true;

                  ContentInfo ci = util.findMatch(fc.getData());
                  if (ci == null) return settings.isAcceptNullContentType();

                  return settings.getContentTypes().contains(ci.getContentType());
                } catch (IOException e) {
                  log()
                      .warn(
                          "Unable to perform content type detection on File {}",
                          fc.getData().getName(),
                          e);
                  return settings.isAcceptNullContentType();
                }
              })
          .forEach(
              fc -> {
                boolean extracted;
                try (InputStream is = new BufferedInputStream(new FileInputStream(fc.getData()))) {
                  extracted = decompress(item, is, fc.getData().getPath());
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  log().error("Unable to read archive file", ioe);
                  return;
                }

                if (extracted && settings.isRemoveSourceContent()) item.removeContent(fc);
              });

      item.getContents(InputStreamContent.class)
          .filter(
              isc -> {
                if (settings.getContentTypes().isEmpty()) return true;

                try {
                  ContentInfo ci = util.findMatch(isc.getData());
                  if (ci == null) return settings.isAcceptNullContentType();

                  return settings.getContentTypes().contains(ci.getContentType());
                } catch (IOException e) {
                  log()
                      .warn(
                          "Unable to perform content type detection on InputStream {}",
                          isc.getId(),
                          e);
                  return settings.isAcceptNullContentType();
                }
              })
          .forEach(
              isc -> {
                boolean extracted;
                try (InputStream is = new BufferedInputStream(isc.getData())) {
                  extracted = decompress(item, is, null);
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  log().error("Unable to read archive file", ioe);
                  return;
                }

                if (extracted && settings.isRemoveSourceContent()) item.removeContent(isc);
              });

      if (exceptions.isEmpty()) {
        if (settings.isDiscardItem()) item.discard();
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.itemError(exceptions);
      }
    }

    private boolean decompress(Item item, InputStream inputStream, String source)
        throws IOException {
      InputStream is;
      try {
        is =
            new BufferedInputStream(
                compressorStreamFactory.createCompressorInputStream(inputStream));

        // Now we've decompressed the stream, we need to check against the allowed content types
        // again
        is.mark(-1);

        if (!settings.getContentTypes().isEmpty()) {
          try {
            ContentInfo ci = util.findMatch(is);
            if (ci == null) {
              if (!settings.isAcceptNullContentType()) return false;
            } else {
              if (!settings.getContentTypes().contains(ci.getContentType())) return false;
            }
          } catch (IOException e) {
            log().warn("Unable to perform content type detection on uncompressed InputStream", e);
            if (!settings.isAcceptNullContentType()) return false;
          }

          is.reset();
        }
      } catch (CompressorException ce) {
        log().debug("No suitable compressor found, or stream is not compressed", ce);
        is = inputStream;
      }

      try (ArchiveInputStream ais = archiveStreamFactory.createArchiveInputStream(is)) {
        if (ais == null) return false;

        ArchiveEntry archiveEntry = null;
        while ((archiveEntry = ais.getNextEntry()) != null) {
          if (!archiveEntry.isDirectory()) {
            Item childItem = item.createChild();

            // Set properties
            Map<String, Object> props = new HashMap<>();
            props.put(PropertyKeys.PROPERTY_KEY_SOURCE, source);
            props.put(PropertyKeys.PROPERTY_KEY_NAME, archiveEntry.getName());
            props.put("lastModifiedDate", toLocalDateTime(archiveEntry.getLastModifiedDate()));
            props.put("size", archiveEntry.getSize());

            props.values().removeIf(Objects::isNull);
            childItem.getProperties().set(props);

            // Create content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = ais.read(buffer)) > 0) {
              baos.write(buffer, 0, len);
            }
            baos.flush();

            childItem
                .createContent(InputStreamContent.class)
                .withData(() -> new ByteArrayInputStream(baos.toByteArray()))
                .withDescription("Content extracted from archive")
                .save();
          }
        }
      } catch (ArchiveException e) {
        log().debug("No suitable archiver found, or stream is not archived", e);
        return false;
      } finally {
        is.close();
      }

      return true;
    }

    private LocalDateTime toLocalDateTime(Date date) {
      if (date == null) return null;

      return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
  }

  public static class Settings extends RemoveSourceContentSettings {
    private boolean discardItem = false;
    private List<ContentType> contentTypes =
        List.of(ContentType.ZIP, ContentType.GZIP, ContentType.TAR);
    private boolean acceptNullContentType = true;

    @Description("If true, this entire Item will be discarded after successful extraction")
    public boolean isDiscardItem() {
      return discardItem;
    }

    public void setDiscardItem(boolean discardItem) {
      this.discardItem = discardItem;
    }

    @Description(
        "List of content types to allow - if the detected content type is not on this list, then it will not be treated as an archive. An empty list will cause the processor to try to parse all Files and InputStreams.")
    public List<ContentType> getContentTypes() {
      return contentTypes;
    }

    public void setContentTypes(List<ContentType> contentTypes) {
      this.contentTypes = contentTypes;
    }

    @Description(
        "Determines whether the processor should try to parse Files and InputStreams where the content type cannot be determined")
    public boolean isAcceptNullContentType() {
      return acceptNullContentType;
    }

    public void setAcceptNullContentType(boolean acceptNullContentType) {
      this.acceptNullContentType = acceptNullContentType;
    }
  }
}
