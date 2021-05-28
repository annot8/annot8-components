/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/** InvalidModel */
@JsonPropertyOrder({InvalidModel.JSON_PROPERTY_DETAIL})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class InvalidModel {
  public static final String JSON_PROPERTY_DETAIL = "detail";
  private String detail;

  public InvalidModel detail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * Get detail
   *
   * @return detail
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_DETAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  /** Return true if this InvalidModel object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InvalidModel invalidModel = (InvalidModel) o;
    return Objects.equals(this.detail, invalidModel.detail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(detail);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvalidModel {\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
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
