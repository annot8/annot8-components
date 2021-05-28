/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** NERResponseEntities */
@JsonPropertyOrder({
  NERResponseEntities.JSON_PROPERTY_TEXT,
  NERResponseEntities.JSON_PROPERTY_LABEL,
  NERResponseEntities.JSON_PROPERTY_START_CHAR,
  NERResponseEntities.JSON_PROPERTY_END_CHAR,
  NERResponseEntities.JSON_PROPERTY_LEMMA,
  NERResponseEntities.JSON_PROPERTY_SENSE2VEC,
  NERResponseEntities.JSON_PROPERTY_START,
  NERResponseEntities.JSON_PROPERTY_END,
  NERResponseEntities.JSON_PROPERTY_TEXT_WITH_WS
})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class NERResponseEntities {
  public static final String JSON_PROPERTY_TEXT = "text";
  private String text;

  public static final String JSON_PROPERTY_LABEL = "label";
  private String label;

  public static final String JSON_PROPERTY_START_CHAR = "start_char";
  private Integer startChar;

  public static final String JSON_PROPERTY_END_CHAR = "end_char";
  private Integer endChar;

  public static final String JSON_PROPERTY_LEMMA = "lemma";
  private String lemma;

  public static final String JSON_PROPERTY_SENSE2VEC = "sense2vec";
  private List<NERResponseSense2vec> sense2vec = new ArrayList<>();

  public static final String JSON_PROPERTY_START = "start";
  private Integer start;

  public static final String JSON_PROPERTY_END = "end";
  private Integer end;

  public static final String JSON_PROPERTY_TEXT_WITH_WS = "text_with_ws";
  private String textWithWs;

  public NERResponseEntities text(String text) {
    this.text = text;
    return this;
  }

  /**
   * A unicode representation of the entity text.
   *
   * @return text
   */
  @ApiModelProperty(required = true, value = "A unicode representation of the entity text.")
  @JsonProperty(JSON_PROPERTY_TEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public NERResponseEntities label(String label) {
    this.label = label;
    return this;
  }

  /**
   * The named entity label. The model&#39;s label scheme lists which are supported. An explanation
   * of the different labels can be found [here](https://spacy.io/api/annotation#named-entities).
   *
   * @return label
   */
  @ApiModelProperty(
      required = true,
      value =
          "The named entity label. The model's label scheme lists which are supported. An explanation of the different labels can be found [here](https://spacy.io/api/annotation#named-entities). ")
  @JsonProperty(JSON_PROPERTY_LABEL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public NERResponseEntities startChar(Integer startChar) {
    this.startChar = startChar;
    return this;
  }

  /**
   * The character offset for the start of the entity.
   *
   * @return startChar
   */
  @ApiModelProperty(required = true, value = "The character offset for the start of the entity.")
  @JsonProperty(JSON_PROPERTY_START_CHAR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getStartChar() {
    return startChar;
  }

  public void setStartChar(Integer startChar) {
    this.startChar = startChar;
  }

  public NERResponseEntities endChar(Integer endChar) {
    this.endChar = endChar;
    return this;
  }

  /**
   * The character offset for the end of the entity.
   *
   * @return endChar
   */
  @ApiModelProperty(required = true, value = "The character offset for the end of the entity.")
  @JsonProperty(JSON_PROPERTY_END_CHAR)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getEndChar() {
    return endChar;
  }

  public void setEndChar(Integer endChar) {
    this.endChar = endChar;
  }

  public NERResponseEntities lemma(String lemma) {
    this.lemma = lemma;
    return this;
  }

  /**
   * The entity’s lemma.
   *
   * @return lemma
   */
  @ApiModelProperty(required = true, value = "The entity’s lemma.")
  @JsonProperty(JSON_PROPERTY_LEMMA)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLemma() {
    return lemma;
  }

  public void setLemma(String lemma) {
    this.lemma = lemma;
  }

  public NERResponseEntities sense2vec(List<NERResponseSense2vec> sense2vec) {
    this.sense2vec = sense2vec;
    return this;
  }

  public NERResponseEntities addSense2vecItem(NERResponseSense2vec sense2vecItem) {
    this.sense2vec.add(sense2vecItem);
    return this;
  }

  /**
   * Phrases similar to the entity (empty if sense2vec was disabled)
   *
   * @return sense2vec
   */
  @ApiModelProperty(
      required = true,
      value = "Phrases similar to the entity (empty if sense2vec was disabled)")
  @JsonProperty(JSON_PROPERTY_SENSE2VEC)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public List<NERResponseSense2vec> getSense2vec() {
    return sense2vec;
  }

  public void setSense2vec(List<NERResponseSense2vec> sense2vec) {
    this.sense2vec = sense2vec;
  }

  public NERResponseEntities start(Integer start) {
    this.start = start;
    return this;
  }

  /**
   * The token offset for the start of the entity.
   *
   * @return start
   */
  @ApiModelProperty(required = true, value = "The token offset for the start of the entity.")
  @JsonProperty(JSON_PROPERTY_START)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public NERResponseEntities end(Integer end) {
    this.end = end;
    return this;
  }

  /**
   * The token offset for the end of the entity.
   *
   * @return end
   */
  @ApiModelProperty(required = true, value = "The token offset for the end of the entity.")
  @JsonProperty(JSON_PROPERTY_END)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getEnd() {
    return end;
  }

  public void setEnd(Integer end) {
    this.end = end;
  }

  public NERResponseEntities textWithWs(String textWithWs) {
    this.textWithWs = textWithWs;
    return this;
  }

  /**
   * The text content of the entity with a trailing whitespace character if the last token has one.
   *
   * @return textWithWs
   */
  @ApiModelProperty(
      required = true,
      value =
          "The text content of the entity with a trailing whitespace character if the last token has one.")
  @JsonProperty(JSON_PROPERTY_TEXT_WITH_WS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTextWithWs() {
    return textWithWs;
  }

  public void setTextWithWs(String textWithWs) {
    this.textWithWs = textWithWs;
  }

  /** Return true if this NERResponse_entities object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NERResponseEntities neRResponseEntities = (NERResponseEntities) o;
    return Objects.equals(this.text, neRResponseEntities.text)
        && Objects.equals(this.label, neRResponseEntities.label)
        && Objects.equals(this.startChar, neRResponseEntities.startChar)
        && Objects.equals(this.endChar, neRResponseEntities.endChar)
        && Objects.equals(this.lemma, neRResponseEntities.lemma)
        && Objects.equals(this.sense2vec, neRResponseEntities.sense2vec)
        && Objects.equals(this.start, neRResponseEntities.start)
        && Objects.equals(this.end, neRResponseEntities.end)
        && Objects.equals(this.textWithWs, neRResponseEntities.textWithWs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, label, startChar, endChar, lemma, sense2vec, start, end, textWithWs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NERResponseEntities {\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    startChar: ").append(toIndentedString(startChar)).append("\n");
    sb.append("    endChar: ").append(toIndentedString(endChar)).append("\n");
    sb.append("    lemma: ").append(toIndentedString(lemma)).append("\n");
    sb.append("    sense2vec: ").append(toIndentedString(sense2vec)).append("\n");
    sb.append("    start: ").append(toIndentedString(start)).append("\n");
    sb.append("    end: ").append(toIndentedString(end)).append("\n");
    sb.append("    textWithWs: ").append(toIndentedString(textWithWs)).append("\n");
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
