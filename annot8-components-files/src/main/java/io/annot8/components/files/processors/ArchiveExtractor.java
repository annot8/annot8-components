/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
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
@SettingsClass(RemoveSourceContentSettings.class)
public class ArchiveExtractor
    extends AbstractProcessorDescriptor<ArchiveExtractor.Processor, RemoveSourceContentSettings> {

  @Override
  protected Processor createComponent(Context context, RemoveSourceContentSettings settings) {
    return new Processor(settings.isRemoveSourceContent());
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

    private final boolean removeSourceContent;

    public Processor(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      // TODO: Support for other archives (7z?)

      item.getContents(FileContent.class)
          .forEach(
              fc -> {
                try (InputStream is = new BufferedInputStream(new FileInputStream(fc.getData()))) {
                  decompress(item, is, fc.getData().getPath());
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  log().error("Unable to read archive file", ioe);
                }

                if (removeSourceContent) item.removeContent(fc);
              });

      item.getContents(InputStreamContent.class)
          .forEach(
              isc -> {
                try (InputStream is = new BufferedInputStream(isc.getData())) {
                  decompress(item, is, null);
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  log().error("Unable to read archive file", ioe);
                }

                if (removeSourceContent) item.removeContent(isc);
              });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.itemError(exceptions);
      }
    }

    private void decompress(Item item, InputStream inputStream, String source) throws IOException {
      InputStream is;
      try {
        is =
            new BufferedInputStream(
                compressorStreamFactory.createCompressorInputStream(inputStream));
      } catch (CompressorException ce) {
        log().debug("No suitable compressor found, or stream is not compressed", ce);
        is = inputStream;
      }

      try (ArchiveInputStream ais = archiveStreamFactory.createArchiveInputStream(is)) {
        if (ais == null) return;

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
      } finally {
        is.close();
      }
    }

    private LocalDateTime toLocalDateTime(Date date) {
      if (date == null) return null;

      return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
  }
}
