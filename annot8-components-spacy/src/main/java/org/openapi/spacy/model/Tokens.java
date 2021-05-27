/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Tokens */
@JsonPropertyOrder({Tokens.JSON_PROPERTY_TOKENS})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class Tokens {
  public static final String JSON_PROPERTY_TOKENS = "tokens";
  private List<String> tokens = new ArrayList<>();

  public Tokens tokens(List<String> tokens) {
    this.tokens = tokens;
    return this;
  }

  public Tokens addTokensItem(String tokensItem) {
    this.tokens.add(tokensItem);
    return this;
  }

  /**
   * Get tokens
   *
   * @return tokens
   */
  @ApiModelProperty(required = true, value = "")
  @JsonProperty(JSON_PROPERTY_TOKENS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<String> getTokens() {
    return tokens;
  }

  public void setTokens(List<String> tokens) {
    this.tokens = tokens;
  }

  /** Return true if this Tokens object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Tokens tokens = (Tokens) o;
    return Objects.equals(this.tokens, tokens.tokens);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tokens);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Tokens {\n");
    sb.append("    tokens: ").append(toIndentedString(tokens)).append("\n");
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
