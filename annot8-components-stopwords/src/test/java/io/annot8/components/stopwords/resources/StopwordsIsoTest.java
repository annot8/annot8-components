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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StopwordsIsoTest {
  @Test
  public void testDefault(){
    Stopwords sw = new StopwordsIso();

    assertEquals("en", sw.getLanguage());
    assertTrue(sw.isStopword("and"));
  }

  @Test
  public void testEnglish(){
    Stopwords sw = new StopwordsIso("en");

    assertEquals("en", sw.getLanguage());
    assertTrue(sw.isStopword("and"));
    assertTrue(sw.isStopword("YOUR"));
    assertTrue(sw.isStopword("Why"));
    assertFalse(sw.isStopword("java"));
    assertFalse(sw.isStopword("JAVA"));
    assertFalse(sw.isStopword("Java"));
  }

  @Test
  public void testMissing(){
    assertThrows(BadConfigurationException.class, () -> new StopwordsIso("missing"));
  }
}
