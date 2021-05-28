/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** NERRequest */
@JsonPropertyOrder({NERRequest.JSON_PROPERTY_SECTIONS, NERRequest.JSON_PROPERTY_SENSE2VEC})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class NERRequest {
  public static final String JSON_PROPERTY_SECTIONS = "sections";
  private List<String> sections = new ArrayList<>();

  public static final String JSON_PROPERTY_SENSE2VEC = "sense2vec";
  private Boolean sense2vec = false;

  public NERRequest sections(List<String> sections) {
    this.sections = sections;
    return this;
  }

  public NERRequest addSectionsItem(String sectionsItem) {
    this.sections.add(sectionsItem);
    return this;
  }

  /**
   * Although you could pass the full text as a single array item, it would be faster to split large
   * text into multiple items. Each item needn&#39;t be semantically related.
   *
   * @return sections
   */
  @ApiModelProperty(
      required = true,
      value =
          "Although you could pass the full text as a single array item, it would be faster to split large text into multiple items. Each item needn't be semantically related.")
  @JsonProperty(JSON_PROPERTY_SECTIONS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getSections() {
    return sections;
  }

  public void setSections(List<String> sections) {
    this.sections = sections;
  }

  public NERRequest sense2vec(Boolean sense2vec) {
    this.sense2vec = sense2vec;
    return this;
  }

  /**
   * Whether to also compute similar phrases using sense2vec (significantly slower)
   *
   * @return sense2vec
   */
  @javax.annotation.Nullable
  @ApiModelProperty(
      value = "Whether to also compute similar phrases using sense2vec (significantly slower)")
  @JsonProperty(JSON_PROPERTY_SENSE2VEC)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Boolean getSense2vec() {
    return sense2vec;
  }

  public void setSense2vec(Boolean sense2vec) {
    this.sense2vec = sense2vec;
  }

  /** Return true if this NERRequest object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NERRequest neRRequest = (NERRequest) o;
    return Objects.equals(this.sections, neRRequest.sections)
        && Objects.equals(this.sense2vec, neRRequest.sense2vec);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sections, sense2vec);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NERRequest {\n");
    sb.append("    sections: ").append(toIndentedString(sections)).append("\n");
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
