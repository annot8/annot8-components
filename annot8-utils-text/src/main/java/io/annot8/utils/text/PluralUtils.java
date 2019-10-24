/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.utils.text;

import java.util.HashSet;
import java.util.Set;

import org.atteo.evo.inflector.English;

public class PluralUtils {
  private PluralUtils() {
    // Private constructor
  }

  public static String pluralise(String original) {
    return English.plural(original);
  }

  public static Set<String> pluraliseSet(Set<String> original) {
    Set<String> result = new HashSet<>(original);
    original.stream().map(English::plural).forEach(result::add);
    return result;
  }
}
