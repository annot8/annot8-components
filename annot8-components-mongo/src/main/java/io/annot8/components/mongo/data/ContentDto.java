/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.data;

import java.util.Collection;
import java.util.Map;

public class ContentDto {

  private String id;
  private String itemId;
  private String description;
  private String type;
  private Object data;
  private Map<String, Object> properties;
  private Collection<AnnotationDto> annotations;

  public ContentDto(
      String id,
      String description,
      Object data,
      Map<String, Object> properties,
      Collection<AnnotationDto> annotations,
      String itemId,
      String type) {
    this.id = id;
    this.description = description;
    this.data = data;
    this.properties = properties;
    this.annotations = annotations;
    this.itemId = itemId;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public Object getData() {
    return data;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public Collection<AnnotationDto> getAnnotations() {
    return annotations;
  }

  public String getItemId() {
    return itemId;
  }

  public String getType() {
    return type;
  }
}
