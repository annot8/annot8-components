/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;
import org.openapi.spacy.ApiClient;
import org.openapi.spacy.ApiException;
import org.openapi.spacy.ApiResponse;
import org.openapi.spacy.model.NERRequest;
import org.openapi.spacy.model.NERResponse;
import org.openapi.spacy.model.PartsOfSpeech;
import org.openapi.spacy.model.Sense2vecPhrases;
import org.openapi.spacy.model.SentenceWithPhrase;
import org.openapi.spacy.model.Sentences;
import org.openapi.spacy.model.Text;
import org.openapi.spacy.model.Tokens;

@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class NlpApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;

  public NlpApi() {
    this(new ApiClient());
  }

  public NlpApi(ApiClient apiClient) {
    memberVarHttpClient = apiClient.getHttpClient();
    memberVarObjectMapper = apiClient.getObjectMapper();
    memberVarBaseUri = apiClient.getBaseUri();
    memberVarInterceptor = apiClient.getRequestInterceptor();
    memberVarReadTimeout = apiClient.getReadTimeout();
    memberVarResponseInterceptor = apiClient.getResponseInterceptor();
  }

  /**
   * Named entity recognition. The pretrained model must have the &#x60;ner&#x60; and
   * &#x60;parser&#x60; pipeline components to use this endpoint. If a sense2vec model was bundled
   * with the service, similar phrases can also be provided.
   *
   * @param neRRequest Text to process (required)
   * @return NERResponse
   * @throws ApiException if fails to make API call
   */
  public NERResponse ner(NERRequest neRRequest) throws ApiException {
    ApiResponse<NERResponse> localVarResponse = nerWithHttpInfo(neRRequest);
    return localVarResponse.getData();
  }

  /**
   * Named entity recognition. The pretrained model must have the &#x60;ner&#x60; and
   * &#x60;parser&#x60; pipeline components to use this endpoint. If a sense2vec model was bundled
   * with the service, similar phrases can also be provided.
   *
   * @param neRRequest Text to process (required)
   * @return ApiResponse&lt;NERResponse&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<NERResponse> nerWithHttpInfo(NERRequest neRRequest) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = nerRequestBuilder(neRRequest);
    try {
      HttpResponse<InputStream> localVarResponse =
          memberVarHttpClient.send(
              localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      if (localVarResponse.statusCode() / 100 != 2) {
        throw new ApiException(
            localVarResponse.statusCode(),
            "ner call received non-success response",
            localVarResponse.headers(),
            localVarResponse.body() == null
                ? null
                : new String(localVarResponse.body().readAllBytes()));
      }
      return new ApiResponse<NERResponse>(
          localVarResponse.statusCode(),
          localVarResponse.headers().map(),
          memberVarObjectMapper.readValue(
              localVarResponse.body(), new TypeReference<NERResponse>() {}));
    } catch (IOException e) {
      throw new ApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder nerRequestBuilder(NERRequest neRRequest) throws ApiException {
    // verify the required parameter 'neRRequest' is set
    if (neRRequest == null) {
      throw new ApiException(400, "Missing the required parameter 'neRRequest' when calling ner");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/ner";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(neRRequest);
      localVarRequestBuilder.method(
          "POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }
  /**
   * Part-of-speech tagging. The pretrained model needs the &#x60;parser&#x60;, &#x60;ner&#x60;, and
   * &#x60;tagger&#x60; pipeline components for this endpoint to be usable.
   *
   * @param text Text to process (required)
   * @return PartsOfSpeech
   * @throws ApiException if fails to make API call
   */
  public PartsOfSpeech pos(Text text) throws ApiException {
    ApiResponse<PartsOfSpeech> localVarResponse = posWithHttpInfo(text);
    return localVarResponse.getData();
  }

  /**
   * Part-of-speech tagging. The pretrained model needs the &#x60;parser&#x60;, &#x60;ner&#x60;, and
   * &#x60;tagger&#x60; pipeline components for this endpoint to be usable.
   *
   * @param text Text to process (required)
   * @return ApiResponse&lt;PartsOfSpeech&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<PartsOfSpeech> posWithHttpInfo(Text text) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = posRequestBuilder(text);
    try {
      HttpResponse<InputStream> localVarResponse =
          memberVarHttpClient.send(
              localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      if (localVarResponse.statusCode() / 100 != 2) {
        throw new ApiException(
            localVarResponse.statusCode(),
            "pos call received non-success response",
            localVarResponse.headers(),
            localVarResponse.body() == null
                ? null
                : new String(localVarResponse.body().readAllBytes()));
      }
      return new ApiResponse<PartsOfSpeech>(
          localVarResponse.statusCode(),
          localVarResponse.headers().map(),
          memberVarObjectMapper.readValue(
              localVarResponse.body(), new TypeReference<PartsOfSpeech>() {}));
    } catch (IOException e) {
      throw new ApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder posRequestBuilder(Text text) throws ApiException {
    // verify the required parameter 'text' is set
    if (text == null) {
      throw new ApiException(400, "Missing the required parameter 'text' when calling pos");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/pos";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(text);
      localVarRequestBuilder.method(
          "POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }
  /**
   * Compute phrases similar to a phrase in a sentence. sense2vec must be bundled with the service,
   * and the pretrained model must have the &#x60;ner&#x60; and &#x60;parser&#x60; pipeline
   * components.
   *
   * @param sentenceWithPhrase The phrase in the sentence (required)
   * @return Sense2vecPhrases
   * @throws ApiException if fails to make API call
   */
  public Sense2vecPhrases sense2vec(SentenceWithPhrase sentenceWithPhrase) throws ApiException {
    ApiResponse<Sense2vecPhrases> localVarResponse = sense2vecWithHttpInfo(sentenceWithPhrase);
    return localVarResponse.getData();
  }

  /**
   * Compute phrases similar to a phrase in a sentence. sense2vec must be bundled with the service,
   * and the pretrained model must have the &#x60;ner&#x60; and &#x60;parser&#x60; pipeline
   * components.
   *
   * @param sentenceWithPhrase The phrase in the sentence (required)
   * @return ApiResponse&lt;Sense2vecPhrases&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Sense2vecPhrases> sense2vecWithHttpInfo(SentenceWithPhrase sentenceWithPhrase)
      throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = sense2vecRequestBuilder(sentenceWithPhrase);
    try {
      HttpResponse<InputStream> localVarResponse =
          memberVarHttpClient.send(
              localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      if (localVarResponse.statusCode() / 100 != 2) {
        throw new ApiException(
            localVarResponse.statusCode(),
            "sense2vec call received non-success response",
            localVarResponse.headers(),
            localVarResponse.body() == null
                ? null
                : new String(localVarResponse.body().readAllBytes()));
      }
      return new ApiResponse<Sense2vecPhrases>(
          localVarResponse.statusCode(),
          localVarResponse.headers().map(),
          memberVarObjectMapper.readValue(
              localVarResponse.body(), new TypeReference<Sense2vecPhrases>() {}));
    } catch (IOException e) {
      throw new ApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder sense2vecRequestBuilder(SentenceWithPhrase sentenceWithPhrase)
      throws ApiException {
    // verify the required parameter 'sentenceWithPhrase' is set
    if (sentenceWithPhrase == null) {
      throw new ApiException(
          400, "Missing the required parameter 'sentenceWithPhrase' when calling sense2vec");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/sense2vec";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(sentenceWithPhrase);
      localVarRequestBuilder.method(
          "POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }
  /**
   * Sentence segmentation. The pretrained model must have the &#x60;parser&#x60; pipeline component
   * for this endpoint to be available.
   *
   * @param text Sentences to segmentize (required)
   * @return Sentences
   * @throws ApiException if fails to make API call
   */
  public Sentences sentencizer(Text text) throws ApiException {
    ApiResponse<Sentences> localVarResponse = sentencizerWithHttpInfo(text);
    return localVarResponse.getData();
  }

  /**
   * Sentence segmentation. The pretrained model must have the &#x60;parser&#x60; pipeline component
   * for this endpoint to be available.
   *
   * @param text Sentences to segmentize (required)
   * @return ApiResponse&lt;Sentences&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Sentences> sentencizerWithHttpInfo(Text text) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = sentencizerRequestBuilder(text);
    try {
      HttpResponse<InputStream> localVarResponse =
          memberVarHttpClient.send(
              localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      if (localVarResponse.statusCode() / 100 != 2) {
        throw new ApiException(
            localVarResponse.statusCode(),
            "sentencizer call received non-success response",
            localVarResponse.headers(),
            localVarResponse.body() == null
                ? null
                : new String(localVarResponse.body().readAllBytes()));
      }
      return new ApiResponse<Sentences>(
          localVarResponse.statusCode(),
          localVarResponse.headers().map(),
          memberVarObjectMapper.readValue(
              localVarResponse.body(), new TypeReference<Sentences>() {}));
    } catch (IOException e) {
      throw new ApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder sentencizerRequestBuilder(Text text) throws ApiException {
    // verify the required parameter 'text' is set
    if (text == null) {
      throw new ApiException(400, "Missing the required parameter 'text' when calling sentencizer");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/sentencizer";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(text);
      localVarRequestBuilder.method(
          "POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }
  /**
   * Tokenization
   *
   * @param text Text to tokenize (required)
   * @return Tokens
   * @throws ApiException if fails to make API call
   */
  public Tokens tokenizer(Text text) throws ApiException {
    ApiResponse<Tokens> localVarResponse = tokenizerWithHttpInfo(text);
    return localVarResponse.getData();
  }

  /**
   * Tokenization
   *
   * @param text Text to tokenize (required)
   * @return ApiResponse&lt;Tokens&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Tokens> tokenizerWithHttpInfo(Text text) throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = tokenizerRequestBuilder(text);
    try {
      HttpResponse<InputStream> localVarResponse =
          memberVarHttpClient.send(
              localVarRequestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());
      if (memberVarResponseInterceptor != null) {
        memberVarResponseInterceptor.accept(localVarResponse);
      }
      if (localVarResponse.statusCode() / 100 != 2) {
        throw new ApiException(
            localVarResponse.statusCode(),
            "tokenizer call received non-success response",
            localVarResponse.headers(),
            localVarResponse.body() == null
                ? null
                : new String(localVarResponse.body().readAllBytes()));
      }
      return new ApiResponse<Tokens>(
          localVarResponse.statusCode(),
          localVarResponse.headers().map(),
          memberVarObjectMapper.readValue(localVarResponse.body(), new TypeReference<Tokens>() {}));
    } catch (IOException e) {
      throw new ApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder tokenizerRequestBuilder(Text text) throws ApiException {
    // verify the required parameter 'text' is set
    if (text == null) {
      throw new ApiException(400, "Missing the required parameter 'text' when calling tokenizer");
    }

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/tokenizer";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Content-Type", "application/json");
    localVarRequestBuilder.header("Accept", "application/json");

    try {
      byte[] localVarPostBody = memberVarObjectMapper.writeValueAsBytes(text);
      localVarRequestBuilder.method(
          "POST", HttpRequest.BodyPublishers.ofByteArray(localVarPostBody));
    } catch (IOException e) {
      throw new ApiException(e);
    }
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }
}
