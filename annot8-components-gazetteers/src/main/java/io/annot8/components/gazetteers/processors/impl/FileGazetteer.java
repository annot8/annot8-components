/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors.impl;

import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.components.gazetteers.processors.Gazetteer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileGazetteer implements Gazetteer {

  private final List<Set<String>> terms = new ArrayList<>();

  public FileGazetteer(Path path, char separator) {
    try {
      Files.lines(path)
          .filter(l -> !l.isBlank())
          .map(
              l ->
                  Stream.of(l.split(Pattern.quote(String.valueOf(separator))))
                      .map(String::strip)
                      .collect(Collectors.toSet()))
          .forEach(terms::add);
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
