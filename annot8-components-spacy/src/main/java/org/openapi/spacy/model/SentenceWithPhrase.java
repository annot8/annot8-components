/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/** SentenceWithPhrase */
@JsonPropertyOrder({
  SentenceWithPhrase.JSON_PROPERTY_SENTENCE,
  SentenceWithPhrase.JSON_PROPERTY_PHRASE
})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class SentenceWithPhrase {
  public static final String JSON_PROPERTY_SENTENCE = "sentence";
  private String sentence;

  public static final String JSON_PROPERTY_PHRASE = "phrase";
  private String phrase;

  public SentenceWithPhrase sentence(String sentence) {
    this.sentence = sentence;
    return this;
  }

  /**
   * The sentence containing the phrase.
   *
   * @return sentence
   */
  @ApiModelProperty(required = true, value = "The sentence containing the phrase.")
  @JsonProperty(JSON_PROPERTY_SENTENCE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSentence() {
    return sentence;
  }

  public void setSentence(String sentence) {
    this.sentence = sentence;
  }

  public SentenceWithPhrase phrase(String phrase) {
    this.phrase = phrase;
    return this;
  }

  /**
   * sense2vec will be run only on this phrase.
   *
   * @return phrase
   */
  @ApiModelProperty(required = true, value = "sense2vec will be run only on this phrase.")
  @JsonProperty(JSON_PROPERTY_PHRASE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPhrase() {
    return phrase;
  }

  public void setPhrase(String phrase) {
    this.phrase = phrase;
  }

  /** Return true if this SentenceWithPhrase object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SentenceWithPhrase sentenceWithPhrase = (SentenceWithPhrase) o;
    return Objects.equals(this.sentence, sentenceWithPhrase.sentence)
        && Objects.equals(this.phrase, sentenceWithPhrase.phrase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sentence, phrase);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SentenceWithPhrase {\n");
    sb.append("    sentence: ").append(toIndentedString(sentence)).append("\n");
    sb.append("    phrase: ").append(toIndentedString(phrase)).append("\n");
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
