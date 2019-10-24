/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.utils.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

public class PluralUtilsTest {
  @Test
  public void test() {
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
