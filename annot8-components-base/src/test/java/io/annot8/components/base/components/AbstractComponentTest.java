/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.components;

import io.annot8.components.monitor.resources.Logging;
import io.annot8.components.monitor.resources.Metering;
import io.annot8.core.components.Processor;
import io.annot8.core.components.Resource;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;
import io.annot8.core.exceptions.BadConfigurationException;
import io.annot8.core.exceptions.MissingResourceException;
import io.annot8.core.settings.NoSettings;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests to ensure the AbstractComponent implementation provides the correct resources to the
 * implementing class
 */
public class AbstractComponentTest {

  @Test
  public void testAbstractComponent() {
    Map<String, Resource> resources = new HashMap<>();
    resources.put(AbstractComponent.RESOURCE_KEY_LOGGING, Logging.notAvailable());
    resources.put(AbstractComponent.RESOURCE_KEY_METERING, Metering.notAvailable());

    TestComponent component = new TestComponent();
    try {
      component.configure(NoSettings.getInstance(), resources);
    } catch (BadConfigurationException | MissingResourceException e1) {
      fail("No exceptions should occur in this test case");
    }

    component.process(null);

    component.close();
  }

  @Test
  public void testAbstractComponentNoResources() {
    TestComponent component = new TestComponent();
    try {
      component.configure(NoSettings.getInstance());
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
