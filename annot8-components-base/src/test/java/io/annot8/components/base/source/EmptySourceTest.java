/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.source;

import io.annot8.api.components.Source;
import io.annot8.api.components.responses.SourceResponse.Status;
import io.annot8.api.settings.NoSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmptySourceTest {

  @Test
  void read() {
    EmptySource source = new EmptySource();

    Source s = source.createComponent(null, NoSettings.getInstance());

    assertEquals(s.read(null).getStatus(), Status.DONE);
    assertEquals(s.read(null).getStatus(), Status.DONE);
    assertEquals(s.read(null).getStatus(), Status.DONE);
  }
}
