/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** NERResponseData */
@JsonPropertyOrder({NERResponseData.JSON_PROPERTY_ENTITIES, NERResponseData.JSON_PROPERTY_TEXT})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class NERResponseData {
  public static final String JSON_PROPERTY_ENTITIES = "entities";
  private List<NERResponseEntities> entities = new ArrayList<>();

  public static final String JSON_PROPERTY_TEXT = "text";
  private String text;

  public NERResponseData entities(List<NERResponseEntities> entities) {
    this.entities = entities;
    return this;
  }

  public NERResponseData addEntitiesItem(NERResponseEntities entitiesItem) {
    this.entities.add(entitiesItem);
    return this;
  }

  /**
   * Get entities
   *
   * @return entities
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_ENTITIES)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<NERResponseEntities> getEntities() {
    return entities;
  }

  public void setEntities(List<NERResponseEntities> entities) {
    this.entities = entities;
  }

  public NERResponseData text(String text) {
    this.text = text;
    return this;
  }

  /**
   * Get text
   *
   * @return text
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_TEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  /** Return true if this NERResponse_data object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NERResponseData neRResponseData = (NERResponseData) o;
    return Objects.equals(this.entities, neRResponseData.entities)
        && Objects.equals(this.text, neRResponseData.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entities, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NERResponseData {\n");
    sb.append("    entities: ").append(toIndentedString(entities)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
