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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@ComponentName("Archive Extractor")
@ComponentDescription("Extract archive files (*.zip) and create new items from each file")
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
    private static final byte[] ZIP_MAGIC_NUMBER = new byte[] {0x50, 0x4B, 0x03, 0x04};
    private final boolean removeSourceContent;

    public Processor(boolean removeSourceContent) {
      this.removeSourceContent = removeSourceContent;
    }

    @Override
    public ProcessorResponse process(Item item) {
      List<Exception> exceptions = new ArrayList<>();

      // TODO: Support for other archives (tar? 7z?)

      item.getContents(FileContent.class)
          .filter(
              fc -> {
                try (FileInputStream fis = new FileInputStream(fc.getData())) {
                  byte[] magicNumber = fis.readNBytes(4);

                  return Arrays.equals(ZIP_MAGIC_NUMBER, magicNumber);
                } catch (IOException ioe) {
                  log().warn("Could not read file {}", fc.getData().getPath(), ioe);
                  return false;
                }
              })
          .forEach(
              fc -> {
                try (FileInputStream fis = new FileInputStream(fc.getData())) {
                  handleInputStream(item, fis, fc.getData().getPath());
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  log().error("Unable to read zip file", ioe);
                }

                if (removeSourceContent) item.removeContent(fc);
              });

      item.getContents(InputStreamContent.class)
          .filter(
              isc -> {
                try (InputStream is = isc.getData()) {
                  byte[] magicNumber = is.readNBytes(4);

                  return Arrays.equals(ZIP_MAGIC_NUMBER, magicNumber);
                } catch (IOException ioe) {
                  log().warn("Could not read InputStream {}", isc.getId(), ioe);
                  return false;
                }
              })
          .forEach(
              isc -> {
                try (InputStream is = isc.getData()) {
                  handleInputStream(item, is, null);
                } catch (IOException ioe) {
                  exceptions.add(ioe);
                  log().error("Unable to read zip file", ioe);
                }

                if (removeSourceContent) item.removeContent(isc);
              });

      if (exceptions.isEmpty()) {
        return ProcessorResponse.ok();
      } else {
        return ProcessorResponse.itemError(exceptions);
      }
    }

    private void handleInputStream(Item item, InputStream inputStream, String source)
        throws IOException {
      try (ZipInputStream zis = new ZipInputStream(inputStream)) {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
          if (!zipEntry.isDirectory()) {
            Item childItem = item.createChild();

            // Set properties
            Map<String, Object> props = new HashMap<>();
            if (source != null) props.put(PropertyKeys.PROPERTY_KEY_SOURCE, source);
            props.put(PropertyKeys.PROPERTY_KEY_NAME, zipEntry.getName());
            props.put("comment", zipEntry.getComment());
            props.put("creationDate", toLocalDateTime(zipEntry.getCreationTime()));
            props.put("lastAccessedDate", toLocalDateTime(zipEntry.getLastAccessTime()));
            props.put("lastModifiedDate", toLocalDateTime(zipEntry.getLastModifiedTime()));

            props.values().removeIf(Objects::isNull);
            childItem.getProperties().set(props);

            // Create content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
              baos.write(buffer, 0, len);
            }
            baos.flush();

            childItem
                .createContent(InputStreamContent.class)
                .withData(() -> new ByteArrayInputStream(baos.toByteArray()))
                .withDescription("Content extracted from ZIP")
                .save();
          }
          zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
      }
    }

    private LocalDateTime toLocalDateTime(FileTime fileTime) {
      if (fileTime == null) return null;

      return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
    }
  }
}
