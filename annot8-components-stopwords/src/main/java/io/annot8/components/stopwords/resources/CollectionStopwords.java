/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

import java.util.Collection;

public class CollectionStopwords implements Stopwords {

  private final String language;
  private final Collection<String> stopwords;

  public CollectionStopwords(String language, Collection<String> stopwords) {
    this.language = language;
    this.stopwords = stopwords;
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public boolean isStopword(String word) {
    return stopwords.stream()
        .map(String::trim)
        .map(String::toLowerCase)
        .anyMatch(s -> s.equalsIgnoreCase(word.trim()));
  }
}
