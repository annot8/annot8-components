/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.exceptions.IncompleteException;
import io.annot8.api.settings.NoSettings;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.NoBounds;
import io.annot8.common.data.content.FileContent;
import io.annot8.conventions.FileMetadataKeys;
import io.annot8.conventions.PathUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

@ComponentName("File Metadata Extractor")
@ComponentDescription("Extract metadata from files")
public class FileMetadataExtractor
    extends AbstractProcessorDescriptor<FileMetadataExtractor.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder().withProcessesContent(FileContent.class).build();
  }

  public static class Processor extends AbstractProcessor {

    public static final String FILE_METADATA = PathUtils.join("file", "metadata");

    @Override
    public ProcessorResponse process(Item item) {
      boolean withoutErrors =
          item.getContents(FileContent.class)
              .map(this::extractMetadata)
              .reduce(true, (a, b) -> a && b);

      if (!withoutErrors) {
        return ProcessorResponse.itemError();
      }

      return ProcessorResponse.ok();
    }

    private boolean extractMetadata(FileContent fileContent) {
      File file = fileContent.getData();

      if (!file.exists()) {
        return false;
      }

      BasicFileAttributes attr;
      boolean isHidden;
      boolean isRegular;
      boolean isDir;
      boolean isSym;
      String owner;
      try {
        isHidden = Files.isHidden(file.toPath());
        isRegular = Files.isRegularFile(file.toPath());
        isDir = Files.isDirectory(file.toPath());
        isSym = Files.isSymbolicLink(file.toPath());
        owner = Files.getOwner(file.toPath()).getName();
      } catch (IOException e) {
        log().error("Failed to retrieve file metadata", e);
        return false;
      }

      try {
        attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
      } catch (IOException e) {
        log().error("Failed to process file attributes", e);
        return false;
      }

      if (attr != null) {
        createMetadataAnnotation(
            fileContent, FileMetadataKeys.DATE_CREATED, attr.creationTime().toMillis());
        createMetadataAnnotation(
            fileContent, FileMetadataKeys.LAST_MODIFIED, attr.lastModifiedTime().toMillis());
        createMetadataAnnotation(
            fileContent, FileMetadataKeys.LAST_ACCESS_DATE, attr.lastAccessTime().toMillis());
        createMetadataAnnotation(fileContent, FileMetadataKeys.FILE_SIZE, attr.size());
      }
      createMetadataAnnotation(fileContent, FileMetadataKeys.PATH, file.getAbsolutePath());
      createMetadataAnnotation(fileContent, FileMetadataKeys.HIDDEN, isHidden);
      createMetadataAnnotation(fileContent, FileMetadataKeys.REGULAR, isRegular);
      createMetadataAnnotation(fileContent, FileMetadataKeys.DIRECTORY, isDir);
      createMetadataAnnotation(fileContent, FileMetadataKeys.SYM_LINK, isSym);
      createMetadataAnnotation(fileContent, FileMetadataKeys.OWNER, owner);
      createMetadataAnnotation(fileContent, FileMetadataKeys.FILENAME, file.getName());

      String extension = getFileExtension(file);
      if (extension != null) {
        createMetadataAnnotation(fileContent, FileMetadataKeys.EXTENSION, extension);
      }
      return true;
    }

    private String getFileExtension(File file) {
      String name = file.getName();
      int index = name.lastIndexOf('.');
      if (index > 0 && index != name.length()) {
        return name.substring(index + 1);
      }
      return null;
    }

    private void createMetadataAnnotation(FileContent content, String key, Object value) {
      AnnotationStore annotations = content.getAnnotations();
      try {
        annotations
            .create()
            .withType(FILE_METADATA)
            .withBounds(NoBounds.getInstance())
            .withProperty(key, value)
            .save();
      } catch (IncompleteException e) {
        log().error("Failed to create file metadata annotation", e);
      }
    }
  }
}
