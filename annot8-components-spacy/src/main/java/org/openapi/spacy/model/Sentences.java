/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Sentences */
@JsonPropertyOrder({Sentences.JSON_PROPERTY_SENTENCES})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class Sentences {
  public static final String JSON_PROPERTY_SENTENCES = "sentences";
  private List<String> sentences = new ArrayList<>();

  public Sentences sentences(List<String> sentences) {
    this.sentences = sentences;
    return this;
  }

  public Sentences addSentencesItem(String sentencesItem) {
    this.sentences.add(sentencesItem);
    return this;
  }

  /**
   * Get sentences
   *
   * @return sentences
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_SENTENCES)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getSentences() {
    return sentences;
  }

  public void setSentences(List<String> sentences) {
    this.sentences = sentences;
  }

  /** Return true if this Sentences object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sentences sentences = (Sentences) o;
    return Objects.equals(this.sentences, sentences.sentences);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sentences);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Sentences {\n");
    sb.append("    sentences: ").append(toIndentedString(sentences)).append("\n");
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
