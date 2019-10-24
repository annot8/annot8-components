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
