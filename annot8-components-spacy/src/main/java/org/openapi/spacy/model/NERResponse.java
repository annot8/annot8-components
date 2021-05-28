/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** NERResponse */
@JsonPropertyOrder({NERResponse.JSON_PROPERTY_DATA})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class NERResponse {
  public static final String JSON_PROPERTY_DATA = "data";
  private List<NERResponseData> data = new ArrayList<>();

  public NERResponse data(List<NERResponseData> data) {
    this.data = data;
    return this;
  }

  public NERResponse addDataItem(NERResponseData dataItem) {
    this.data.add(dataItem);
    return this;
  }

  /**
   * Get data
   *
   * @return data
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_DATA)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<NERResponseData> getData() {
    return data;
  }

  public void setData(List<NERResponseData> data) {
    this.data = data;
  }

  /** Return true if this NERResponse object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NERResponse neRResponse = (NERResponse) o;
    return Objects.equals(this.data, neRResponse.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NERResponse {\n");
    sb.append("    data: ").append(toIndentedString(data)).append("\n");
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
