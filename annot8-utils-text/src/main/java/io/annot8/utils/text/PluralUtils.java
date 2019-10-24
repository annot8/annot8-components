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
package io.annot8.utils.text;

import org.atteo.evo.inflector.English;

import java.util.HashSet;
import java.util.Set;

public class PluralUtils {
  private PluralUtils(){
    //Private constructor
  }

  public static String pluralise(String original){
    return English.plural(original);
  }

  public static Set<String> pluraliseSet(Set<String> original){
    Set<String> result = new HashSet<>(original);
    original.stream().map(English::plural).forEach(result::add);
    return result;
  }
}
