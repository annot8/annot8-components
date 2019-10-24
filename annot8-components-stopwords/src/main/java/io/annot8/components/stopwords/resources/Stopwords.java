/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.resources;

import io.annot8.api.components.Resource;

public interface Stopwords extends Resource {
  /** Return the language for this resource, e.g. en */
  String getLanguage();

  /** Returns true if a word is a stopword, or false otherwise */
  boolean isStopword(String word);
}
