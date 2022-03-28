/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy.api;

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

@SuppressWarnings("unused")
@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class StatusApi {
  private final HttpClient memberVarHttpClient;
  private final ObjectMapper memberVarObjectMapper;
  private final String memberVarBaseUri;
  private final Consumer<HttpRequest.Builder> memberVarInterceptor;
  private final Duration memberVarReadTimeout;
  private final Consumer<HttpResponse<InputStream>> memberVarResponseInterceptor;

  public StatusApi() {
    this(new ApiClient());
  }

  public StatusApi(ApiClient apiClient) {
    memberVarHttpClient = apiClient.getHttpClient();
    memberVarObjectMapper = apiClient.getObjectMapper();
    memberVarBaseUri = apiClient.getBaseUri();
    memberVarInterceptor = apiClient.getRequestInterceptor();
    memberVarReadTimeout = apiClient.getReadTimeout();
    memberVarResponseInterceptor = apiClient.getResponseInterceptor();
  }

  /**
   * Check if all systems are operational
   *
   * @throws ApiException if fails to make API call
   */
  public void healthCheck() throws ApiException {
    healthCheckWithHttpInfo();
  }

  /**
   * Check if all systems are operational
   *
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException if fails to make API call
   */
  public ApiResponse<Void> healthCheckWithHttpInfo() throws ApiException {
    HttpRequest.Builder localVarRequestBuilder = healthCheckRequestBuilder();
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
            "healthCheck call received non-success response",
            localVarResponse.headers(),
            localVarResponse.body() == null
                ? null
                : new String(localVarResponse.body().readAllBytes()));
      }
      return new ApiResponse<Void>(
          localVarResponse.statusCode(), localVarResponse.headers().map(), null);
    } catch (IOException e) {
      throw new ApiException(e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ApiException(e);
    }
  }

  private HttpRequest.Builder healthCheckRequestBuilder() throws ApiException {

    HttpRequest.Builder localVarRequestBuilder = HttpRequest.newBuilder();

    String localVarPath = "/health_check";

    localVarRequestBuilder.uri(URI.create(memberVarBaseUri + localVarPath));

    localVarRequestBuilder.header("Accept", "application/json");

    localVarRequestBuilder.method("GET", HttpRequest.BodyPublishers.noBody());
    if (memberVarReadTimeout != null) {
      localVarRequestBuilder.timeout(memberVarReadTimeout);
    }
    if (memberVarInterceptor != null) {
      memberVarInterceptor.accept(localVarRequestBuilder);
    }
    return localVarRequestBuilder;
  }
}
