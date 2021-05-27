/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** PartsOfSpeechData */
@JsonPropertyOrder({PartsOfSpeechData.JSON_PROPERTY_TEXT, PartsOfSpeechData.JSON_PROPERTY_TAGS})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class PartsOfSpeechData {
  public static final String JSON_PROPERTY_TEXT = "text";
  private String text;

  public static final String JSON_PROPERTY_TAGS = "tags";
  private List<PartsOfSpeechTags> tags = new ArrayList<>();

  public PartsOfSpeechData text(String text) {
    this.text = text;
    return this;
  }

  /**
   * The sentence or phrase being tagged.
   *
   * @return text
   */
  @ApiModelProperty(required = true, value = "The sentence or phrase being tagged.")
  @JsonProperty(JSON_PROPERTY_TEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public PartsOfSpeechData tags(List<PartsOfSpeechTags> tags) {
    this.tags = tags;
    return this;
  }

  public PartsOfSpeechData addTagsItem(PartsOfSpeechTags tagsItem) {
    this.tags.add(tagsItem);
    return this;
  }

  /**
   * Get tags
   *
   * @return tags
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_TAGS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<PartsOfSpeechTags> getTags() {
    return tags;
  }

  public void setTags(List<PartsOfSpeechTags> tags) {
    this.tags = tags;
  }

  /** Return true if this PartsOfSpeech_data object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartsOfSpeechData partsOfSpeechData = (PartsOfSpeechData) o;
    return Objects.equals(this.text, partsOfSpeechData.text)
        && Objects.equals(this.tags, partsOfSpeechData.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, tags);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartsOfSpeechData {\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
