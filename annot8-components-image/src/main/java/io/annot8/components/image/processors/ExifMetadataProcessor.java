/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.image.processors;

import java.io.IOException;
import java.util.Date;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.Rational;
import com.drew.metadata.Metadata;
import com.drew.metadata.StringValue;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.GpsDirectory;

import io.annot8.common.data.bounds.NoBounds;
import io.annot8.common.data.content.FileContent;
import io.annot8.components.base.processors.AbstractContentProcessor;
import io.annot8.core.components.annotations.ComponentDescription;
import io.annot8.core.components.annotations.ComponentName;
import io.annot8.core.exceptions.IncompleteException;

// TODO: Could move to extending AbstractContentProcessor?
@ComponentName("EXIF Metadata")
@ComponentDescription("Extract EXIF Metadata from images")
public class ExifMetadataProcessor extends AbstractContentProcessor<FileContent> {

  public ExifMetadataProcessor() {
    super(FileContent.class);
  }

  @Override
  public void process(FileContent content) {
    Metadata metadata;
    try {
      metadata = ImageMetadataReader.readMetadata(content.getData());
    } catch (IOException | ImageProcessingException e) {
      log().error("Failed to read the file for Exif extraction", e);
      return;
    }

    for (ExifDirectoryBase directory : metadata.getDirectoriesOfType(ExifDirectoryBase.class)) {
      try {
        if (directory instanceof GpsDirectory) {
          handleGpsDirectory((GpsDirectory) directory, content);
        } else {
          handleDirectory(directory, content);
        }
      } catch (IncompleteException e) {
        log().error("Failed to create annotations", e);
        return;
      }
    }
  }

  private void handleGpsDirectory(GpsDirectory directory, FileContent content) {
    directory.getGpsDate();
    createAnnotation(content, "Geo Location", directory.getGeoLocation());
    createAnnotation(content, "Gps Date", directory.getGpsDate().getTime());
  }

  private void handleDirectory(ExifDirectoryBase directory, FileContent content) {
    for (Tag tag : directory.getTags()) {
      Date date = directory.getDate(tag.getTagType());
      Object value;
      if (date == null) {
        Object object = directory.getObject(tag.getTagType());
        if (object instanceof Rational) {
          value = ((Rational) object).doubleValue();
        } else if (object instanceof StringValue) {
          value = ((StringValue) object).toString();
        } else {
          value = object;
        }
      } else {
        value = date.getTime();
      }
      createAnnotation(content, tag.getTagName(), value);
    }
  }

  private void createAnnotation(FileContent content, String key, Object value) {
    content
        .getAnnotations()
        .create()
        .withProperty(key, value)
        .withType("EXIF_METADATA")
        .withBounds(NoBounds.getInstance())
        .save();
  }

  //  @Override
  //  public Stream<AnnotationCapability> createsAnnotations() {
  //    return Stream.of(new AnnotationCapability("EXIF_METADATA", NoBounds.class));
  //  }
  //
  //  @Override
  //  public Stream<ContentCapability> processesContent() {
  //    return Stream.of(new ContentCapability(FileContent.class));
  //  }
}
