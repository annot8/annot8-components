/*
 * Crown Copyright (C) 2019 Dstl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.annot8.components.gazetteers.processors.impl;

import io.annot8.api.exceptions.BadConfigurationException;
import io.annot8.components.gazetteers.processors.Gazetteer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileGazetteer implements Gazetteer {

  private List<Set<String>> terms = new ArrayList<>();

  public FileGazetteer(Path path, char separator) {
    try {
      Files.lines(path).filter(l -> !l.isBlank()).forEach(l -> terms.add(Set.of(l.split(Pattern.quote(String.valueOf(separator))))));
    } catch (IOException e) {
      throw new BadConfigurationException("Could not read file gazetteer", e);
    }
  }

  @Override
  public Collection<String> getValues() {
    return terms.stream()
        .flatMap(Set::stream)
        .collect(Collectors.toSet());
  }

  @Override
  public Collection<String> getAliases(String key) {
    for(Set<String> s : terms){
      if(s.contains(key))
        return s;
    }
    return Collections.emptySet();
  }

  @Override
  public Map<String, Object> getAdditionalData(String key) {
    return Collections.emptyMap();
  }
}
