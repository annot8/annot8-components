/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import java.util.Collection;
import java.util.Map;

public interface Gazetteer {
  Collection<String> getValues();

  Collection<String> getAliases(String key);

  Map<String, Object> getAdditionalData(String key);
}
