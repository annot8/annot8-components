/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.translation.processors;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;

import java.net.URI;

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

  @Description("URL of the REMEDI Pre-Processor")
  public URI getPreProcessorUri() {
    return preProcessorUri;
  }

  public void setPreProcessorUri(URI preProcessorUri) {
    this.preProcessorUri = preProcessorUri;
  }

  @Description("URL of the REMEDI Translation Server or Load Balancer")
  public URI getServerUri() {
    return serverUri;
  }

  public void setServerUri(URI serverUri) {
    this.serverUri = serverUri;
  }

  @Description("URL of the REMEDI Post-Processor")
  public URI getPostProcessorUri() {
    return postProcessorUri;
  }

  public void setPostProcessorUri(URI postProcessorUri) {
    this.postProcessorUri = postProcessorUri;
  }

  @Description("Source language")
  public String getSourceLanguage() {
    return sourceLanguage;
  }

  public void setSourceLanguage(String sourceLanguage) {
    this.sourceLanguage = sourceLanguage;
  }

  @Description("Target language")
  public String getTargetLanguage() {
    return targetLanguage;
  }

  public void setTargetLanguage(String targetLanguage) {
    this.targetLanguage = targetLanguage;
  }
}
