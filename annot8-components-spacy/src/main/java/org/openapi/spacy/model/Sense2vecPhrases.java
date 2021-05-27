/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Sense2vecPhrases */
@JsonPropertyOrder({Sense2vecPhrases.JSON_PROPERTY_SENSE2VEC})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class Sense2vecPhrases {
  public static final String JSON_PROPERTY_SENSE2VEC = "sense2vec";
  private List<NERResponseSense2vec> sense2vec = new ArrayList<>();

  public Sense2vecPhrases sense2vec(List<NERResponseSense2vec> sense2vec) {
    this.sense2vec = sense2vec;
    return this;
  }

  public Sense2vecPhrases addSense2vecItem(NERResponseSense2vec sense2vecItem) {
    this.sense2vec.add(sense2vecItem);
    return this;
  }

  /**
   * Phrases similar to the entity
   *
   * @return sense2vec
   */
  @ApiModelProperty(required = true, value = "Phrases similar to the entity")
  @JsonProperty(JSON_PROPERTY_SENSE2VEC)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<NERResponseSense2vec> getSense2vec() {
    return sense2vec;
  }

  public void setSense2vec(List<NERResponseSense2vec> sense2vec) {
    this.sense2vec = sense2vec;
  }

  /** Return true if this Sense2vecPhrases object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Sense2vecPhrases sense2vecPhrases = (Sense2vecPhrases) o;
    return Objects.equals(this.sense2vec, sense2vecPhrases.sense2vec);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sense2vec);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Sense2vecPhrases {\n");
    sb.append("    sense2vec: ").append(toIndentedString(sense2vec)).append("\n");
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
