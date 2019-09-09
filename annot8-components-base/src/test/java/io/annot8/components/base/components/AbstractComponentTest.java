/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.components;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.annot8.components.monitor.resources.Logging;
import io.annot8.components.monitor.resources.Metering;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.context.Context;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;

/**
 * Unit tests to ensure the AbstractComponent implementation provides the correct resources to the
 * implementing class
 */
public class AbstractComponentTest {

  @Test
  public void testAbstractComponent() {
    Context context = Mockito.mock(Context.class);

    doReturn(Optional.of(Logging.useLoggerFactory()))
        .when(context)
        .getResource(Mockito.eq(Logging.class));
    doReturn(Optional.of(Metering.useGlobalRegistry()))
        .when(context)
        .getResource(Mockito.eq(Metering.class));

    TestComponent component = new TestComponent();
    try {
      component.configure(context);
    } catch (BadConfigurationException | MissingResourceException e1) {
      fail("No exceptions should occur in this test case");
    }

    component.process(null);

    component.close();
  }

  @Test
  public void testAbstractComponentEmptyContext() {
    Context context = Mockito.mock(Context.class);
    doReturn(Optional.empty()).when(context).getResource(Mockito.any());
    TestComponent component = new TestComponent();
    try {
      component.configure(context);
    } catch (BadConfigurationException | MissingResourceException e) {
      fail("No exceptions should occur in this test case");
    }

    component.process(null);

    component.close();
  }

  private class TestComponent extends AbstractComponent implements Processor {

    @Override
    public ProcessorResponse process(Item item) {
      assertNotNull(log());
      assertNotNull(metrics());
      return null;
    }
  }
}
