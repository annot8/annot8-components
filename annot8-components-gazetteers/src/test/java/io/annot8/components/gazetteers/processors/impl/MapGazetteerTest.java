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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class MapGazetteerTest {
  @Test
  public void test(){
    Set<String> skodaTerms = Set.of("Fabia", "Superb", "Octavia");
    Map<String, Object> skodaData = new HashMap<>();
    skodaData.put("manufacturer", "Skoda");
    skodaData.put("nationality", "Czech");

    Set<String> renaultTerms = Set.of("Clio", "Scenic", "Captur", "Twingo");
    Map<String, Object> renaultData = new HashMap<>();
    renaultData.put("manufacturer", "Renault");
    renaultData.put("nationality", "French");

    Map<Set<String>, Map<String, Object>> terms = new HashMap<>();
    terms.put(skodaTerms, skodaData);
    terms.put(renaultTerms, renaultData);

    Gazetteer g = new MapGazetteer(terms);

    assertEquals(7, g.getValues().size());
    assertTrue(g.getValues().contains("Superb"));
    assertFalse(g.getValues().contains("Focus"));

    assertArrayEquals(Arrays.stream(new String[]{"Fabia", "Superb", "Octavia"}).sorted().toArray(),
            g.getAliases("Octavia").stream().sorted().toArray());

    assertEquals(renaultData, g.getAdditionalData("Scenic"));
  }
}
