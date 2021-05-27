/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.Objects;

/** NERResponseSense2vec */
@JsonPropertyOrder({
  NERResponseSense2vec.JSON_PROPERTY_PHRASE,
  NERResponseSense2vec.JSON_PROPERTY_SIMILARITY
})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class NERResponseSense2vec {
  public static final String JSON_PROPERTY_PHRASE = "phrase";
  private String phrase;

  public static final String JSON_PROPERTY_SIMILARITY = "similarity";
  private BigDecimal similarity;

  public NERResponseSense2vec phrase(String phrase) {
    this.phrase = phrase;
    return this;
  }

  /**
   * Get phrase
   *
   * @return phrase
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_PHRASE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPhrase() {
    return phrase;
  }

  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  public NERResponseSense2vec similarity(BigDecimal similarity) {
    this.similarity = similarity;
    return this;
  }

  /**
   * Similarity in the range of 0-1
   *
   * @return similarity
   */
  @ApiModelProperty(required = true, value = "Similarity in the range of 0-1")
  @JsonProperty(JSON_PROPERTY_SIMILARITY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getSimilarity() {
    return similarity;
  }

  public void setSimilarity(BigDecimal similarity) {
    this.similarity = similarity;
  }

  /** Return true if this NERResponse_sense2vec object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NERResponseSense2vec neRResponseSense2vec = (NERResponseSense2vec) o;
    return Objects.equals(this.phrase, neRResponseSense2vec.phrase)
        && Objects.equals(this.similarity, neRResponseSense2vec.similarity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(phrase, similarity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NERResponseSense2vec {\n");
    sb.append("    phrase: ").append(toIndentedString(phrase)).append("\n");
    sb.append("    similarity: ").append(toIndentedString(similarity)).append("\n");
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
