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

import io.annot8.components.gazetteers.processors.Gazetteer;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class CollectionGazetteer implements Gazetteer {

  private final Collection<String> terms;

  public CollectionGazetteer(Collection<String> terms){
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
