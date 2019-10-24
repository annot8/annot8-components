/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

/** An empty stopwords resource, which returns false for all words */
public final class NoOpStopwords implements Stopwords {
  @Override
  public String getLanguage() {
    return "*";
  }

  @Override
  public boolean isStopword(String word) {
    return false;
  }
}
