/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class TypeUtilsTest {
  @Test
  public void testWildcardMatches() {
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/foo/bar"));

    assertFalse(TypeUtils.matchesWildcard("entity/foo/bar", "entity/foo/baz"));
    assertFalse(TypeUtils.matchesWildcard("entity/foo/bar", "entity/bar/foo"));

    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "*/foo/bar"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/*/bar"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/foo/*"));

    assertFalse(TypeUtils.matchesWildcard("entity/foo/bar", "entity/*/baz"));

    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "*/*/bar"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "*/foo/*"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/*/*"));

    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "*/*/*"));

    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "**"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/**"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/foo/**"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "**/foo/bar"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "entity/**/bar"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar/baz", "entity/**/baz"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar/baz", "**/foo/**"));
    assertTrue(TypeUtils.matchesWildcard("entity/foo/bar", "**/bar"));

    assertFalse(TypeUtils.matchesWildcard("entity/foo/bar", "entity/foo/bar/**"));
    assertFalse(TypeUtils.matchesWildcard("entity/foo/baz", "**/bar"));
  }
}
