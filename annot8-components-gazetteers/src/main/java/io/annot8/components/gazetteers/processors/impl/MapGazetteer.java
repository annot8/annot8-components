/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import io.annot8.components.gazetteers.processors.Gazetteer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MapGazetteer implements Gazetteer {

  private final Map<Set<String>, Map<String, Object>> termsAndData;

  public MapGazetteer(Map<Set<String>, Map<String, Object>> termsAndData) {
    this.termsAndData = termsAndData;
  }

  @Override
  public Collection<String> getValues() {
    return termsAndData.keySet().stream().flatMap(Set::stream).collect(Collectors.toSet());
  }

  @Override
  public Collection<String> getAliases(String key) {
    for (Set<String> s : termsAndData.keySet()) {
      if (s.contains(key)) return s;
    }
    return Collections.emptyList();
  }

  @Override
  public Map<String, Object> getAdditionalData(String key) {
    return termsAndData.get(getAliases(key));
  }
}
