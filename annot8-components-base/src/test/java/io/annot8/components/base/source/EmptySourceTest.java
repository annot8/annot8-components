package io.annot8.components.base.source;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.SourceResponse.Status;
import org.junit.jupiter.api.Test;

class EmptySourceTest {

  @Test
  void read() {
    EmptySource source = new EmptySource();

    assertEquals(source.read(null).getStatus(), Status.DONE);
    assertEquals(source.read(null).getStatus(), Status.DONE);
    assertEquals(source.read(null).getStatus(), Status.DONE);

  }
}