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

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionStopwordsTest {
  @Test
  public void testDefault(){
    Stopwords sw = new CollectionStopwords("en", Arrays.asList("and", "the"));

    assertEquals("en", sw.getLanguage());
    assertTrue(sw.isStopword("and"));
    assertTrue(sw.isStopword(" THE "));
    assertFalse(sw.isStopword("foo"));
  }
}
