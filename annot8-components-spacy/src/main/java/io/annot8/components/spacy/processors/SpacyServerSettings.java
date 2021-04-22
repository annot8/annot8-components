/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.spacy.processors;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import java.net.URI;

public class SpacyServerSettings implements Settings {
  private String baseUri = "http://localhost:8000";

  @Override
  public boolean validate() {
    if (baseUri == null || baseUri.isEmpty()) return false;

    try {
      URI.create(baseUri);
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  @Description("Base URI for the SpaCy Server API")
  public String getBaseUri() {
    return baseUri;
  }

  public void setBaseUri(String baseUri) {
    this.baseUri = baseUri;
  }
}
