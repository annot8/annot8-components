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

import io.annot8.api.exceptions.BadConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class StopwordsIso implements Stopwords {

  private final String language;
  private final Set<String> stopwords;

  public StopwordsIso(){
    this("en");
  }

  public StopwordsIso(String language) {
    this.language = language;

    try(InputStream stream = StopwordsIso.class.getResourceAsStream("stopwords-iso-"+language+".txt")) {
      if (stream == null)
        throw new BadConfigurationException("Language " + language + " not supported");

      try(BufferedReader br = new BufferedReader(new InputStreamReader(stream))){
        stopwords = br.lines()
            .map(String::trim)
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
      }
    }catch (IOException ioe){
      throw new BadConfigurationException("Unable to read data file for language "+language, ioe);
    }
  }

  @Override
  public String getLanguage() {
    return language;
  }

  @Override
  public boolean isStopword(String word) {
    return stopwords.contains(word.trim().toLowerCase());
  }
}
