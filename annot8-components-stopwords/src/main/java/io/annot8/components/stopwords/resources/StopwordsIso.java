/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

import io.annot8.api.exceptions.BadConfigurationException;

public class StopwordsIso implements Stopwords {

  private final String language;
  private final Set<String> stopwords;

  public StopwordsIso() {
    this("en");
  }

  public StopwordsIso(String language) {
    this.language = language;

    try (InputStream stream =
        StopwordsIso.class.getResourceAsStream("stopwords-iso-" + language + ".txt")) {
      if (stream == null)
        throw new BadConfigurationException("Language " + language + " not supported");

      try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
        stopwords =
            br.lines().map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());
      }
    } catch (IOException ioe) {
      throw new BadConfigurationException("Unable to read data file for language " + language, ioe);
    }
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public boolean isStopword(String word) {
    return stopwords.contains(word.trim().toLowerCase());
  }
}
