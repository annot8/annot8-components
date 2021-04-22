/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.util.Objects;

/** PartsOfSpeechTags */
@JsonPropertyOrder({
  PartsOfSpeechTags.JSON_PROPERTY_CHAR_OFFSET,
  PartsOfSpeechTags.JSON_PROPERTY_DEP,
  PartsOfSpeechTags.JSON_PROPERTY_ENT_IOB,
  PartsOfSpeechTags.JSON_PROPERTY_ENT_TYPE,
  PartsOfSpeechTags.JSON_PROPERTY_HEAD,
  PartsOfSpeechTags.JSON_PROPERTY_INDEX,
  PartsOfSpeechTags.JSON_PROPERTY_IS_ALPHA,
  PartsOfSpeechTags.JSON_PROPERTY_IS_ASCII,
  PartsOfSpeechTags.JSON_PROPERTY_IS_BRACKET,
  PartsOfSpeechTags.JSON_PROPERTY_IS_CURRENCY,
  PartsOfSpeechTags.JSON_PROPERTY_IS_DIGIT,
  PartsOfSpeechTags.JSON_PROPERTY_IS_LEFT_PUNCT,
  PartsOfSpeechTags.JSON_PROPERTY_IS_OOV,
  PartsOfSpeechTags.JSON_PROPERTY_IS_PUNCT,
  PartsOfSpeechTags.JSON_PROPERTY_IS_QUOTE,
  PartsOfSpeechTags.JSON_PROPERTY_IS_RIGHT_PUNCT,
  PartsOfSpeechTags.JSON_PROPERTY_IS_SPACE,
  PartsOfSpeechTags.JSON_PROPERTY_IS_STOP,
  PartsOfSpeechTags.JSON_PROPERTY_IS_TITLE,
  PartsOfSpeechTags.JSON_PROPERTY_LANG,
  PartsOfSpeechTags.JSON_PROPERTY_LEFT_EDGE,
  PartsOfSpeechTags.JSON_PROPERTY_LEMMA,
  PartsOfSpeechTags.JSON_PROPERTY_LIKE_EMAIL,
  PartsOfSpeechTags.JSON_PROPERTY_LIKE_NUM,
  PartsOfSpeechTags.JSON_PROPERTY_LIKE_URL,
  PartsOfSpeechTags.JSON_PROPERTY_NORMALIZED,
  PartsOfSpeechTags.JSON_PROPERTY_POS,
  PartsOfSpeechTags.JSON_PROPERTY_PREFIX,
  PartsOfSpeechTags.JSON_PROPERTY_PROB,
  PartsOfSpeechTags.JSON_PROPERTY_RIGHT_EDGE,
  PartsOfSpeechTags.JSON_PROPERTY_SHAPE,
  PartsOfSpeechTags.JSON_PROPERTY_SUFFIX,
  PartsOfSpeechTags.JSON_PROPERTY_TAG,
  PartsOfSpeechTags.JSON_PROPERTY_TEXT,
  PartsOfSpeechTags.JSON_PROPERTY_TEXT_WITH_WS,
  PartsOfSpeechTags.JSON_PROPERTY_WHITESPACE
})
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class PartsOfSpeechTags {
  public static final String JSON_PROPERTY_CHAR_OFFSET = "char_offset";
  private Integer charOffset;

  public static final String JSON_PROPERTY_DEP = "dep";
  private String dep;

  /**
   * IOB code of named entity tag. &#x60;“B”&#x60; means the token begins an entity, &#x60;“I”&#x60;
   * means it is inside an entity, &#x60;“O”&#x60; means it is outside an entity, and
   * &#x60;\&quot;\&quot;&#x60; means no entity tag is set.
   */
  public enum EntIobEnum {
    B("B"),

    I("I"),

    O("O"),

    EMPTY("");

    private String value;

    EntIobEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static EntIobEnum fromValue(String value) {
      for (EntIobEnum b : EntIobEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_ENT_IOB = "ent_iob";
  private EntIobEnum entIob;

  public static final String JSON_PROPERTY_ENT_TYPE = "ent_type";
  private String entType;

  public static final String JSON_PROPERTY_HEAD = "head";
  private String head;

  public static final String JSON_PROPERTY_INDEX = "index";
  private Integer index;

  public static final String JSON_PROPERTY_IS_ALPHA = "is_alpha";
  private Boolean isAlpha;

  public static final String JSON_PROPERTY_IS_ASCII = "is_ascii";
  private Boolean isAscii;

  public static final String JSON_PROPERTY_IS_BRACKET = "is_bracket";
  private Boolean isBracket;

  public static final String JSON_PROPERTY_IS_CURRENCY = "is_currency";
  private Boolean isCurrency;

  public static final String JSON_PROPERTY_IS_DIGIT = "is_digit";
  private Boolean isDigit;

  public static final String JSON_PROPERTY_IS_LEFT_PUNCT = "is_left_punct";
  private Boolean isLeftPunct;

  public static final String JSON_PROPERTY_IS_OOV = "is_oov";
  private Boolean isOov;

  public static final String JSON_PROPERTY_IS_PUNCT = "is_punct";
  private Boolean isPunct;

  public static final String JSON_PROPERTY_IS_QUOTE = "is_quote";
  private Boolean isQuote;

  public static final String JSON_PROPERTY_IS_RIGHT_PUNCT = "is_right_punct";
  private Boolean isRightPunct;

  public static final String JSON_PROPERTY_IS_SPACE = "is_space";
  private Boolean isSpace;

  public static final String JSON_PROPERTY_IS_STOP = "is_stop";
  private Boolean isStop;

  public static final String JSON_PROPERTY_IS_TITLE = "is_title";
  private Boolean isTitle;

  /**
   * The [ISO 639-1 code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) for the language of
   * the parent document’s vocabulary.
   */
  public enum LangEnum {
    AF("af"),

    AR("ar"),

    BG("bg"),

    BN("bn"),

    CA("ca"),

    CS("cs"),

    DA("da"),

    DE("de"),

    EL("el"),

    EN("en"),

    ES("es"),

    ET("et"),

    FA("fa"),

    FI("fi"),

    FR("fr"),

    GA("ga"),

    HE("he"),

    HI("hi"),

    HR("hr"),

    HU("hu"),

    ID("id"),

    IS("is"),

    IT("it"),

    JA("ja"),

    KN("kn"),

    KO("ko"),

    LT("lt"),

    LV("lv"),

    MR("mr"),

    NB("nb"),

    NL("nl"),

    PL("pl"),

    PT("pt"),

    RO("ro"),

    RU("ru"),

    SI("si"),

    SK("sk"),

    SL("sl"),

    SQ("sq"),

    SR("sr"),

    SV("sv"),

    TA("ta"),

    TE("te"),

    TH("th"),

    TL("tl"),

    TR("tr"),

    TT("tt"),

    UK("uk"),

    UR("ur"),

    VI("vi"),

    XX("xx"),

    ZH("zh");

    private String value;

    LangEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static LangEnum fromValue(String value) {
      for (LangEnum b : LangEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_LANG = "lang";
  private LangEnum lang;

  public static final String JSON_PROPERTY_LEFT_EDGE = "left_edge";
  private String leftEdge;

  public static final String JSON_PROPERTY_LEMMA = "lemma";
  private String lemma;

  public static final String JSON_PROPERTY_LIKE_EMAIL = "like_email";
  private Boolean likeEmail;

  public static final String JSON_PROPERTY_LIKE_NUM = "like_num";
  private Boolean likeNum;

  public static final String JSON_PROPERTY_LIKE_URL = "like_url";
  private Boolean likeUrl;

  public static final String JSON_PROPERTY_NORMALIZED = "normalized";
  private String normalized;

  public static final String JSON_PROPERTY_POS = "pos";
  private String pos;

  public static final String JSON_PROPERTY_PREFIX = "prefix";
  private String prefix;

  public static final String JSON_PROPERTY_PROB = "prob";
  private BigDecimal prob;

  public static final String JSON_PROPERTY_RIGHT_EDGE = "right_edge";
  private String rightEdge;

  public static final String JSON_PROPERTY_SHAPE = "shape";
  private String shape;

  public static final String JSON_PROPERTY_SUFFIX = "suffix";
  private String suffix;

  public static final String JSON_PROPERTY_TAG = "tag";
  private String tag;

  public static final String JSON_PROPERTY_TEXT = "text";
  private String text;

  public static final String JSON_PROPERTY_TEXT_WITH_WS = "text_with_ws";
  private String textWithWs;

  /** Trailing space character if present. */
  public enum WhitespaceEnum {
    SPACE(" "),

    EMPTY("");

    private String value;

    WhitespaceEnum(String value) {
      this.value = value;
    }

    @JsonValue
    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static WhitespaceEnum fromValue(String value) {
      for (WhitespaceEnum b : WhitespaceEnum.values()) {
        if (b.value.equals(value)) {
          return b;
        }
      }
      throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
  }

  public static final String JSON_PROPERTY_WHITESPACE = "whitespace";
  private WhitespaceEnum whitespace;

  public PartsOfSpeechTags charOffset(Integer charOffset) {
    this.charOffset = charOffset;
    return this;
  }

  /**
   * The character offset of the token within the parent document.
   *
   * @return charOffset
   */
  @ApiModelProperty(
      required = true,
      value = "The character offset of the token within the parent document.")
  @JsonProperty(JSON_PROPERTY_CHAR_OFFSET)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getCharOffset() {
    return charOffset;
  }

  public void setCharOffset(Integer charOffset) {
    this.charOffset = charOffset;
  }

  public PartsOfSpeechTags dep(String dep) {
    this.dep = dep;
    return this;
  }

  /**
   * The dependency label. The model&#39;s label scheme lists which are supported. An explanation of
   * the different labels can be found [here](https://spacy.io/api/annotation#dependency-parsing).
   *
   * @return dep
   */
  @ApiModelProperty(
      required = true,
      value =
          "The dependency label. The model's label scheme lists which are supported. An explanation of the different labels can be found [here](https://spacy.io/api/annotation#dependency-parsing).")
  @JsonProperty(JSON_PROPERTY_DEP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getDep() {
    return dep;
  }

  public void setDep(String dep) {
    this.dep = dep;
  }

  public PartsOfSpeechTags entIob(EntIobEnum entIob) {
    this.entIob = entIob;
    return this;
  }

  /**
   * IOB code of named entity tag. &#x60;“B”&#x60; means the token begins an entity, &#x60;“I”&#x60;
   * means it is inside an entity, &#x60;“O”&#x60; means it is outside an entity, and
   * &#x60;\&quot;\&quot;&#x60; means no entity tag is set.
   *
   * @return entIob
   */
  @ApiModelProperty(
      required = true,
      value =
          "IOB code of named entity tag. `“B”` means the token begins an entity, `“I”` means it is inside an entity, `“O”` means it is outside an entity, and `\"\"` means no entity tag is set.")
  @JsonProperty(JSON_PROPERTY_ENT_IOB)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public EntIobEnum getEntIob() {
    return entIob;
  }

  public void setEntIob(EntIobEnum entIob) {
    this.entIob = entIob;
  }

  public PartsOfSpeechTags entType(String entType) {
    this.entType = entType;
    return this;
  }

  /**
   * The named entity label. The model&#39;s label scheme lists which are supported. An explanation
   * of the different labels can be found [here](https://spacy.io/api/annotation#named-entities).
   *
   * @return entType
   */
  @ApiModelProperty(
      required = true,
      value =
          "The named entity label. The model's label scheme lists which are supported. An explanation of the different labels can be found [here](https://spacy.io/api/annotation#named-entities). ")
  @JsonProperty(JSON_PROPERTY_ENT_TYPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getEntType() {
    return entType;
  }

  public void setEntType(String entType) {
    this.entType = entType;
  }

  public PartsOfSpeechTags head(String head) {
    this.head = head;
    return this;
  }

  /**
   * The syntactic parent, or “governor”, of this token.
   *
   * @return head
   */
  @ApiModelProperty(required = true, value = "The syntactic parent, or “governor”, of this token.")
  @JsonProperty(JSON_PROPERTY_HEAD)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getHead() {
    return head;
  }

  public void setHead(String head) {
    this.head = head;
  }

  public PartsOfSpeechTags index(Integer index) {
    this.index = index;
    return this;
  }

  /**
   * The index of the token within the parent document.
   *
   * @return index
   */
  @ApiModelProperty(required = true, value = "The index of the token within the parent document.")
  @JsonProperty(JSON_PROPERTY_INDEX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Integer getIndex() {
    return index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public PartsOfSpeechTags isAlpha(Boolean isAlpha) {
    this.isAlpha = isAlpha;
    return this;
  }

  /**
   * Does the token consist of alphabetic characters?
   *
   * @return isAlpha
   */
  @ApiModelProperty(required = true, value = "Does the token consist of alphabetic characters?")
  @JsonProperty(JSON_PROPERTY_IS_ALPHA)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsAlpha() {
    return isAlpha;
  }

  public void setIsAlpha(Boolean isAlpha) {
    this.isAlpha = isAlpha;
  }

  public PartsOfSpeechTags isAscii(Boolean isAscii) {
    this.isAscii = isAscii;
    return this;
  }

  /**
   * Does the token consist of ASCII characters?
   *
   * @return isAscii
   */
  @ApiModelProperty(required = true, value = "Does the token consist of ASCII characters?")
  @JsonProperty(JSON_PROPERTY_IS_ASCII)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsAscii() {
    return isAscii;
  }

  public void setIsAscii(Boolean isAscii) {
    this.isAscii = isAscii;
  }

  public PartsOfSpeechTags isBracket(Boolean isBracket) {
    this.isBracket = isBracket;
    return this;
  }

  /**
   * Is the token a bracket?
   *
   * @return isBracket
   */
  @ApiModelProperty(required = true, value = "Is the token a bracket?")
  @JsonProperty(JSON_PROPERTY_IS_BRACKET)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsBracket() {
    return isBracket;
  }

  public void setIsBracket(Boolean isBracket) {
    this.isBracket = isBracket;
  }

  public PartsOfSpeechTags isCurrency(Boolean isCurrency) {
    this.isCurrency = isCurrency;
    return this;
  }

  /**
   * Is the token a currency symbol?
   *
   * @return isCurrency
   */
  @ApiModelProperty(required = true, value = "Is the token a currency symbol?")
  @JsonProperty(JSON_PROPERTY_IS_CURRENCY)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsCurrency() {
    return isCurrency;
  }

  public void setIsCurrency(Boolean isCurrency) {
    this.isCurrency = isCurrency;
  }

  public PartsOfSpeechTags isDigit(Boolean isDigit) {
    this.isDigit = isDigit;
    return this;
  }

  /**
   * Does the token consist of digits?
   *
   * @return isDigit
   */
  @ApiModelProperty(required = true, value = "Does the token consist of digits?")
  @JsonProperty(JSON_PROPERTY_IS_DIGIT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsDigit() {
    return isDigit;
  }

  public void setIsDigit(Boolean isDigit) {
    this.isDigit = isDigit;
  }

  public PartsOfSpeechTags isLeftPunct(Boolean isLeftPunct) {
    this.isLeftPunct = isLeftPunct;
    return this;
  }

  /**
   * Is the token a left punctuation mark (e.g. &#x60;(&#x60;)?
   *
   * @return isLeftPunct
   */
  @ApiModelProperty(required = true, value = "Is the token a left punctuation mark (e.g. `(`)?")
  @JsonProperty(JSON_PROPERTY_IS_LEFT_PUNCT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsLeftPunct() {
    return isLeftPunct;
  }

  public void setIsLeftPunct(Boolean isLeftPunct) {
    this.isLeftPunct = isLeftPunct;
  }

  public PartsOfSpeechTags isOov(Boolean isOov) {
    this.isOov = isOov;
    return this;
  }

  /**
   * Is the token out-of-vocabulary?
   *
   * @return isOov
   */
  @ApiModelProperty(required = true, value = "Is the token out-of-vocabulary?")
  @JsonProperty(JSON_PROPERTY_IS_OOV)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsOov() {
    return isOov;
  }

  public void setIsOov(Boolean isOov) {
    this.isOov = isOov;
  }

  public PartsOfSpeechTags isPunct(Boolean isPunct) {
    this.isPunct = isPunct;
    return this;
  }

  /**
   * Is the token punctuation?
   *
   * @return isPunct
   */
  @ApiModelProperty(required = true, value = "Is the token punctuation?")
  @JsonProperty(JSON_PROPERTY_IS_PUNCT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsPunct() {
    return isPunct;
  }

  public void setIsPunct(Boolean isPunct) {
    this.isPunct = isPunct;
  }

  public PartsOfSpeechTags isQuote(Boolean isQuote) {
    this.isQuote = isQuote;
    return this;
  }

  /**
   * Is the token a quotation mark?
   *
   * @return isQuote
   */
  @ApiModelProperty(required = true, value = "Is the token a quotation mark?")
  @JsonProperty(JSON_PROPERTY_IS_QUOTE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsQuote() {
    return isQuote;
  }

  public void setIsQuote(Boolean isQuote) {
    this.isQuote = isQuote;
  }

  public PartsOfSpeechTags isRightPunct(Boolean isRightPunct) {
    this.isRightPunct = isRightPunct;
    return this;
  }

  /**
   * Is the token a right punctuation mark (e.g. &#x60;)&#x60;)?
   *
   * @return isRightPunct
   */
  @ApiModelProperty(required = true, value = "Is the token a right punctuation mark (e.g. `)`)?")
  @JsonProperty(JSON_PROPERTY_IS_RIGHT_PUNCT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsRightPunct() {
    return isRightPunct;
  }

  public void setIsRightPunct(Boolean isRightPunct) {
    this.isRightPunct = isRightPunct;
  }

  public PartsOfSpeechTags isSpace(Boolean isSpace) {
    this.isSpace = isSpace;
    return this;
  }

  /**
   * Does the token consist of whitespace characters?
   *
   * @return isSpace
   */
  @ApiModelProperty(required = true, value = "Does the token consist of whitespace characters?")
  @JsonProperty(JSON_PROPERTY_IS_SPACE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsSpace() {
    return isSpace;
  }

  public void setIsSpace(Boolean isSpace) {
    this.isSpace = isSpace;
  }

  public PartsOfSpeechTags isStop(Boolean isStop) {
    this.isStop = isStop;
    return this;
  }

  /**
   * Is the token part of a “stop list”?
   *
   * @return isStop
   */
  @ApiModelProperty(required = true, value = "Is the token part of a “stop list”?")
  @JsonProperty(JSON_PROPERTY_IS_STOP)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsStop() {
    return isStop;
  }

  public void setIsStop(Boolean isStop) {
    this.isStop = isStop;
  }

  public PartsOfSpeechTags isTitle(Boolean isTitle) {
    this.isTitle = isTitle;
    return this;
  }

  /**
   * Is the token in titlecase?
   *
   * @return isTitle
   */
  @ApiModelProperty(required = true, value = "Is the token in titlecase?")
  @JsonProperty(JSON_PROPERTY_IS_TITLE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getIsTitle() {
    return isTitle;
  }

  public void setIsTitle(Boolean isTitle) {
    this.isTitle = isTitle;
  }

  public PartsOfSpeechTags lang(LangEnum lang) {
    this.lang = lang;
    return this;
  }

  /**
   * The [ISO 639-1 code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) for the language of
   * the parent document’s vocabulary.
   *
   * @return lang
   */
  @ApiModelProperty(
      required = true,
      value =
          "The [ISO 639-1 code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) for the language of the parent document’s vocabulary.")
  @JsonProperty(JSON_PROPERTY_LANG)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public LangEnum getLang() {
    return lang;
  }

  public void setLang(LangEnum lang) {
    this.lang = lang;
  }

  public PartsOfSpeechTags leftEdge(String leftEdge) {
    this.leftEdge = leftEdge;
    return this;
  }

  /**
   * The leftmost token of this token’s syntactic descendants.
   *
   * @return leftEdge
   */
  @ApiModelProperty(
      required = true,
      value = "The leftmost token of this token’s syntactic descendants.")
  @JsonProperty(JSON_PROPERTY_LEFT_EDGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLeftEdge() {
    return leftEdge;
  }

  public void setLeftEdge(String leftEdge) {
    this.leftEdge = leftEdge;
  }

  public PartsOfSpeechTags lemma(String lemma) {
    this.lemma = lemma;
    return this;
  }

  /**
   * Base form of the token, with no inflectional suffixes.
   *
   * @return lemma
   */
  @ApiModelProperty(
      required = true,
      value = "Base form of the token, with no inflectional suffixes.")
  @JsonProperty(JSON_PROPERTY_LEMMA)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getLemma() {
    return lemma;
  }

  public void setLemma(String lemma) {
    this.lemma = lemma;
  }

  public PartsOfSpeechTags likeEmail(Boolean likeEmail) {
    this.likeEmail = likeEmail;
    return this;
  }

  /**
   * Does the token resemble an email address?
   *
   * @return likeEmail
   */
  @ApiModelProperty(required = true, value = "Does the token resemble an email address?")
  @JsonProperty(JSON_PROPERTY_LIKE_EMAIL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getLikeEmail() {
    return likeEmail;
  }

  public void setLikeEmail(Boolean likeEmail) {
    this.likeEmail = likeEmail;
  }

  public PartsOfSpeechTags likeNum(Boolean likeNum) {
    this.likeNum = likeNum;
    return this;
  }

  /**
   * Does the token represent a number (e.g. &#x60;10.9&#x60;, &#x60;10&#x60;,
   * &#x60;\&quot;ten\&quot;&#x60;)?
   *
   * @return likeNum
   */
  @ApiModelProperty(
      required = true,
      value = "Does the token represent a number (e.g. `10.9`, `10`, `\"ten\"`)?")
  @JsonProperty(JSON_PROPERTY_LIKE_NUM)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getLikeNum() {
    return likeNum;
  }

  public void setLikeNum(Boolean likeNum) {
    this.likeNum = likeNum;
  }

  public PartsOfSpeechTags likeUrl(Boolean likeUrl) {
    this.likeUrl = likeUrl;
    return this;
  }

  /**
   * Does the token resemble a URL?
   *
   * @return likeUrl
   */
  @ApiModelProperty(required = true, value = "Does the token resemble a URL?")
  @JsonProperty(JSON_PROPERTY_LIKE_URL)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public Boolean getLikeUrl() {
    return likeUrl;
  }

  public void setLikeUrl(Boolean likeUrl) {
    this.likeUrl = likeUrl;
  }

  public PartsOfSpeechTags normalized(String normalized) {
    this.normalized = normalized;
    return this;
  }

  /**
   * The token’s norm (i.e., a normalized form of the token text).
   *
   * @return normalized
   */
  @ApiModelProperty(
      required = true,
      value = "The token’s norm (i.e., a normalized form of the token text).")
  @JsonProperty(JSON_PROPERTY_NORMALIZED)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getNormalized() {
    return normalized;
  }

  public void setNormalized(String normalized) {
    this.normalized = normalized;
  }

  public PartsOfSpeechTags pos(String pos) {
    this.pos = pos;
    return this;
  }

  /**
   * Part-of-speech tags for the model
   *
   * @return pos
   */
  @ApiModelProperty(required = true, value = "Part-of-speech tags for the model")
  @JsonProperty(JSON_PROPERTY_POS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPos() {
    return pos;
  }

  public void setPos(String pos) {
    this.pos = pos;
  }

  public PartsOfSpeechTags prefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  /**
   * A length-N substring from the start of the token. Defaults to &#x60;N&#x3D;1&#x60;.
   *
   * @return prefix
   */
  @ApiModelProperty(
      required = true,
      value = "A length-N substring from the start of the token. Defaults to `N=1`.")
  @JsonProperty(JSON_PROPERTY_PREFIX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public PartsOfSpeechTags prob(BigDecimal prob) {
    this.prob = prob;
    return this;
  }

  /**
   * Smoothed log probability estimate of token’s word type (context-independent entry in the
   * vocabulary).
   *
   * @return prob
   */
  @ApiModelProperty(
      required = true,
      value =
          "Smoothed log probability estimate of token’s word type (context-independent entry in the vocabulary).")
  @JsonProperty(JSON_PROPERTY_PROB)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public BigDecimal getProb() {
    return prob;
  }

  public void setProb(BigDecimal prob) {
    this.prob = prob;
  }

  public PartsOfSpeechTags rightEdge(String rightEdge) {
    this.rightEdge = rightEdge;
    return this;
  }

  /**
   * The rightmost token of this token’s syntactic descendants.
   *
   * @return rightEdge
   */
  @ApiModelProperty(
      required = true,
      value = "The rightmost token of this token’s syntactic descendants.")
  @JsonProperty(JSON_PROPERTY_RIGHT_EDGE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getRightEdge() {
    return rightEdge;
  }

  public void setRightEdge(String rightEdge) {
    this.rightEdge = rightEdge;
  }

  public PartsOfSpeechTags shape(String shape) {
    this.shape = shape;
    return this;
  }

  /**
   * Transform of the tokens’s string, to show orthographic features (e.g., &#x60;“Xxxx”&#x60; or
   * &#x60;“dd”&#x60;).
   *
   * @return shape
   */
  @ApiModelProperty(
      required = true,
      value =
          "Transform of the tokens’s string, to show orthographic features (e.g., `“Xxxx”` or `“dd”`).")
  @JsonProperty(JSON_PROPERTY_SHAPE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getShape() {
    return shape;
  }

  public void setShape(String shape) {
    this.shape = shape;
  }

  public PartsOfSpeechTags suffix(String suffix) {
    this.suffix = suffix;
    return this;
  }

  /**
   * Length-N substring from the end of the token. Defaults to &#x60;N&#x3D;3&#x60;.
   *
   * @return suffix
   */
  @ApiModelProperty(
      required = true,
      value = "Length-N substring from the end of the token. Defaults to `N=3`.")
  @JsonProperty(JSON_PROPERTY_SUFFIX)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public PartsOfSpeechTags tag(String tag) {
    this.tag = tag;
    return this;
  }

  /**
   * Part-of-speech tags from the model&#39;s label scheme
   *
   * @return tag
   */
  @ApiModelProperty(required = true, value = "Part-of-speech tags from the model's label scheme")
  @JsonProperty(JSON_PROPERTY_TAG)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public PartsOfSpeechTags text(String text) {
    this.text = text;
    return this;
  }

  /**
   * Verbatim text content.
   *
   * @return text
   */
  @ApiModelProperty(required = true, value = "Verbatim text content.")
  @JsonProperty(JSON_PROPERTY_TEXT)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public PartsOfSpeechTags textWithWs(String textWithWs) {
    this.textWithWs = textWithWs;
    return this;
  }

  /**
   * Text content, with trailing space character if present.
   *
   * @return textWithWs
   */
  @ApiModelProperty(
      required = true,
      value = "Text content, with trailing space character if present.")
  @JsonProperty(JSON_PROPERTY_TEXT_WITH_WS)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public String getTextWithWs() {
    return textWithWs;
  }

  public void setTextWithWs(String textWithWs) {
    this.textWithWs = textWithWs;
  }

  public PartsOfSpeechTags whitespace(WhitespaceEnum whitespace) {
    this.whitespace = whitespace;
    return this;
  }

  /**
   * Trailing space character if present.
   *
   * @return whitespace
   */
  @ApiModelProperty(required = true, value = "Trailing space character if present.")
  @JsonProperty(JSON_PROPERTY_WHITESPACE)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public WhitespaceEnum getWhitespace() {
    return whitespace;
  }

  public void setWhitespace(WhitespaceEnum whitespace) {
    this.whitespace = whitespace;
  }

  /** Return true if this PartsOfSpeech_tags object is equal to o. */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PartsOfSpeechTags partsOfSpeechTags = (PartsOfSpeechTags) o;
    return Objects.equals(this.charOffset, partsOfSpeechTags.charOffset)
        && Objects.equals(this.dep, partsOfSpeechTags.dep)
        && Objects.equals(this.entIob, partsOfSpeechTags.entIob)
        && Objects.equals(this.entType, partsOfSpeechTags.entType)
        && Objects.equals(this.head, partsOfSpeechTags.head)
        && Objects.equals(this.index, partsOfSpeechTags.index)
        && Objects.equals(this.isAlpha, partsOfSpeechTags.isAlpha)
        && Objects.equals(this.isAscii, partsOfSpeechTags.isAscii)
        && Objects.equals(this.isBracket, partsOfSpeechTags.isBracket)
        && Objects.equals(this.isCurrency, partsOfSpeechTags.isCurrency)
        && Objects.equals(this.isDigit, partsOfSpeechTags.isDigit)
        && Objects.equals(this.isLeftPunct, partsOfSpeechTags.isLeftPunct)
        && Objects.equals(this.isOov, partsOfSpeechTags.isOov)
        && Objects.equals(this.isPunct, partsOfSpeechTags.isPunct)
        && Objects.equals(this.isQuote, partsOfSpeechTags.isQuote)
        && Objects.equals(this.isRightPunct, partsOfSpeechTags.isRightPunct)
        && Objects.equals(this.isSpace, partsOfSpeechTags.isSpace)
        && Objects.equals(this.isStop, partsOfSpeechTags.isStop)
        && Objects.equals(this.isTitle, partsOfSpeechTags.isTitle)
        && Objects.equals(this.lang, partsOfSpeechTags.lang)
        && Objects.equals(this.leftEdge, partsOfSpeechTags.leftEdge)
        && Objects.equals(this.lemma, partsOfSpeechTags.lemma)
        && Objects.equals(this.likeEmail, partsOfSpeechTags.likeEmail)
        && Objects.equals(this.likeNum, partsOfSpeechTags.likeNum)
        && Objects.equals(this.likeUrl, partsOfSpeechTags.likeUrl)
        && Objects.equals(this.normalized, partsOfSpeechTags.normalized)
        && Objects.equals(this.pos, partsOfSpeechTags.pos)
        && Objects.equals(this.prefix, partsOfSpeechTags.prefix)
        && Objects.equals(this.prob, partsOfSpeechTags.prob)
        && Objects.equals(this.rightEdge, partsOfSpeechTags.rightEdge)
        && Objects.equals(this.shape, partsOfSpeechTags.shape)
        && Objects.equals(this.suffix, partsOfSpeechTags.suffix)
        && Objects.equals(this.tag, partsOfSpeechTags.tag)
        && Objects.equals(this.text, partsOfSpeechTags.text)
        && Objects.equals(this.textWithWs, partsOfSpeechTags.textWithWs)
        && Objects.equals(this.whitespace, partsOfSpeechTags.whitespace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        charOffset,
        dep,
        entIob,
        entType,
        head,
        index,
        isAlpha,
        isAscii,
        isBracket,
        isCurrency,
        isDigit,
        isLeftPunct,
        isOov,
        isPunct,
        isQuote,
        isRightPunct,
        isSpace,
        isStop,
        isTitle,
        lang,
        leftEdge,
        lemma,
        likeEmail,
        likeNum,
        likeUrl,
        normalized,
        pos,
        prefix,
        prob,
        rightEdge,
        shape,
        suffix,
        tag,
        text,
        textWithWs,
        whitespace);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PartsOfSpeechTags {\n");
    sb.append("    charOffset: ").append(toIndentedString(charOffset)).append("\n");
    sb.append("    dep: ").append(toIndentedString(dep)).append("\n");
    sb.append("    entIob: ").append(toIndentedString(entIob)).append("\n");
    sb.append("    entType: ").append(toIndentedString(entType)).append("\n");
    sb.append("    head: ").append(toIndentedString(head)).append("\n");
    sb.append("    index: ").append(toIndentedString(index)).append("\n");
    sb.append("    isAlpha: ").append(toIndentedString(isAlpha)).append("\n");
    sb.append("    isAscii: ").append(toIndentedString(isAscii)).append("\n");
    sb.append("    isBracket: ").append(toIndentedString(isBracket)).append("\n");
    sb.append("    isCurrency: ").append(toIndentedString(isCurrency)).append("\n");
    sb.append("    isDigit: ").append(toIndentedString(isDigit)).append("\n");
    sb.append("    isLeftPunct: ").append(toIndentedString(isLeftPunct)).append("\n");
    sb.append("    isOov: ").append(toIndentedString(isOov)).append("\n");
    sb.append("    isPunct: ").append(toIndentedString(isPunct)).append("\n");
    sb.append("    isQuote: ").append(toIndentedString(isQuote)).append("\n");
    sb.append("    isRightPunct: ").append(toIndentedString(isRightPunct)).append("\n");
    sb.append("    isSpace: ").append(toIndentedString(isSpace)).append("\n");
    sb.append("    isStop: ").append(toIndentedString(isStop)).append("\n");
    sb.append("    isTitle: ").append(toIndentedString(isTitle)).append("\n");
    sb.append("    lang: ").append(toIndentedString(lang)).append("\n");
    sb.append("    leftEdge: ").append(toIndentedString(leftEdge)).append("\n");
    sb.append("    lemma: ").append(toIndentedString(lemma)).append("\n");
    sb.append("    likeEmail: ").append(toIndentedString(likeEmail)).append("\n");
    sb.append("    likeNum: ").append(toIndentedString(likeNum)).append("\n");
    sb.append("    likeUrl: ").append(toIndentedString(likeUrl)).append("\n");
    sb.append("    normalized: ").append(toIndentedString(normalized)).append("\n");
    sb.append("    pos: ").append(toIndentedString(pos)).append("\n");
    sb.append("    prefix: ").append(toIndentedString(prefix)).append("\n");
    sb.append("    prob: ").append(toIndentedString(prob)).append("\n");
    sb.append("    rightEdge: ").append(toIndentedString(rightEdge)).append("\n");
    sb.append("    shape: ").append(toIndentedString(shape)).append("\n");
    sb.append("    suffix: ").append(toIndentedString(suffix)).append("\n");
    sb.append("    tag: ").append(toIndentedString(tag)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
    sb.append("    textWithWs: ").append(toIndentedString(textWithWs)).append("\n");
    sb.append("    whitespace: ").append(toIndentedString(whitespace)).append("\n");
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
