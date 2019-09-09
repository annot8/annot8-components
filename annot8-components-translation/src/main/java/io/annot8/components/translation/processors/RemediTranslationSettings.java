/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.translation.processors;

import java.net.URI;

import io.annot8.core.settings.Settings;

public class RemediTranslationSettings implements Settings {

  private URI preProcessorUri = URI.create("ws://localhost:9003");
  private URI serverUri = URI.create("ws://localhost:9001");
  private URI postProcessorUri = URI.create("ws://localhost:9003");

  private String sourceLanguage = "auto";
  private String targetLanguage = "English";

  @Override
  public boolean validate() {
    return preProcessorUri != null && serverUri != null && postProcessorUri != null;
  }

  public URI getPreProcessorUri() {
    return preProcessorUri;
  }

  public void setPreProcessorUri(URI preProcessorUri) {
    this.preProcessorUri = preProcessorUri;
  }

  public URI getServerUri() {
    return serverUri;
  }

  public void setServerUri(URI serverUri) {
    this.serverUri = serverUri;
  }

  public URI getPostProcessorUri() {
    return postProcessorUri;
  }

  public void setPostProcessorUri(URI postProcessorUri) {
    this.postProcessorUri = postProcessorUri;
  }

  public String getSourceLanguage() {
    return sourceLanguage;
  }

  public void setSourceLanguage(String sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  public String getTargetLanguage() {
    return targetLanguage;
  }

  public void setTargetLanguage(String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }
}
