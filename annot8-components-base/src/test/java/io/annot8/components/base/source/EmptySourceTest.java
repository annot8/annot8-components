/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.source;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.annot8.api.components.responses.SourceResponse.Status;

class EmptySourceTest {

  @Test
  void read() {
    EmptySource source = new EmptySource();

    assertEquals(source.read(null).getStatus(), Status.DONE);
    assertEquals(source.read(null).getStatus(), Status.DONE);
    assertEquals(source.read(null).getStatus(), Status.DONE);
  }
}
