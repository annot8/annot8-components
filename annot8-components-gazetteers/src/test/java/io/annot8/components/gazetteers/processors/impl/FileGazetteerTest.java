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

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class FileGazetteerTest {
  @Test
  public void test() {
    Path path = new File(this.getClass().getResource("gazetteer.txt").getFile()).toPath();

    Gazetteer g = new FileGazetteer(path, ',');

    assertEquals(7, g.getValues().size());
    assertTrue(g.getValues().contains("james"));
    assertTrue(g.getValues().contains("jimmy"));
    assertFalse(g.getValues().contains("elizabeth"));

    assertArrayEquals(Arrays.stream(new String[]{"james","jim","jimmy"}).sorted().toArray(),
            g.getAliases("jimmy").stream().sorted().toArray());

    assertEquals(Collections.emptyMap(), g.getAdditionalData("thom"));

  }
}
