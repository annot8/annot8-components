/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.components.gazetteers.processors.Gazetteer;

public class FileGazetteer implements Gazetteer {

  private List<Set<String>> terms = new ArrayList<>();

  public FileGazetteer(Path path, char separator) {
    try {
      Files.lines(path)
          .filter(l -> !l.isBlank())
          .forEach(l -> terms.add(Set.of(l.split(Pattern.quote(String.valueOf(separator))))));
    } catch (IOException e) {
      throw new BadConfigurationException("Could not read file gazetteer", e);
    }
  }

  @Override
  public Collection<String> getValues() {
    return terms.stream().flatMap(Set::stream).collect(Collectors.toSet());
  }

  @Override
  public Collection<String> getAliases(String key) {
    for (Set<String> s : terms) {
      if (s.contains(key)) return s;
    }
    return Collections.emptySet();
  }

  @Override
  public Map<String, Object> getAdditionalData(String key) {
    return Collections.emptyMap();
  }
}
