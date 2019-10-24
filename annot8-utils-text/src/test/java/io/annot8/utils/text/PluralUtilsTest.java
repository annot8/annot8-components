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

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PluralUtilsTest {
  @Test
  public void test(){
    Set<String> a = Set.of("apple", "banana", "cherry");
    Set<String> b = PluralUtils.pluraliseSet(a);

    assertEquals(6, b.size());
    assertTrue(b.contains("apple"));
    assertTrue(b.contains("apples"));
    assertTrue(b.contains("banana"));
    assertTrue(b.contains("bananas"));
    assertTrue(b.contains("cherry"));
    assertTrue(b.contains("cherries"));

  }
}
