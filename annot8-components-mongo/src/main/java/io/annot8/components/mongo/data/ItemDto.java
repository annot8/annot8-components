/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.mongo.data;

import java.util.Collection;
import java.util.Map;

public class ItemDto {

  private String id;
  private String parentId;
  private Map<String, Object> properties;
  private Collection<ContentDto> contents;

  public ItemDto(
      String id, String parentId, Map<String, Object> properties, Collection<ContentDto> contents) {
    this.id = id;
    this.parentId = parentId;
    this.properties = properties;
    this.contents = contents;
  }

  public String getId() {
    return id;
  }

  public String getParentId() {
    return parentId;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public Collection<ContentDto> getContents() {
    return contents;
  }
}
