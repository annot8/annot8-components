/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import io.annot8.components.gazetteers.processors.Gazetteer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class CollectionGazetteer implements Gazetteer {

  private final Collection<String> terms;

  public CollectionGazetteer(Collection<String> terms) {
    this.terms = terms;
  }

  @Override
  public Collection<String> getValues() {
    return terms;
  }

  @Override
  public Collection<String> getAliases(String key) {
    return Set.of(key);
  }

  @Override
  public Map<String, Object> getAdditionalData(String key) {
    return Collections.emptyMap();
  }
}
