/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.components;

import io.annot8.components.monitor.resources.Logging;
import io.annot8.components.monitor.resources.Metering;
import io.annot8.components.monitor.resources.metering.Metrics;
import io.annot8.components.monitor.resources.metering.NoOpMetrics;
import io.annot8.core.components.Annot8Component;
import io.annot8.core.components.Resource;
import io.annot8.core.components.annotations.ResourceKey;
import io.annot8.core.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import java.util.Map;

@ResourceKey(key = AbstractComponent.RESOURCE_KEY_LOGGING, type = Logging.class, optional = true)
@ResourceKey(key = AbstractComponent.RESOURCE_KEY_METERING, type = Metering.class, optional = true)
public abstract class AbstractComponent<S extends Settings> implements Annot8Component<S> {

  public static final String RESOURCE_KEY_LOGGING = "LOGGING";
  public static final String RESOURCE_KEY_METERING = "METERING";

  private Logger logger;
  private Metrics metrics;

  @Override
  public void configure(S settings, Map<String, Resource> resources) {
    // Look if we have a logging resource and crate a logger from it is possible
    Logging logging = (Logging) resources.get(RESOURCE_KEY_LOGGING);
    if (logging != null) {
      logger = logging.getLogger(getClass());
    } else {
      createNopLogger();
    }

    // Get Metrics
    Metering metering = (Metering) resources.get(RESOURCE_KEY_METERING);
    if (metering != null) {
      metrics = metering.getMetrics(getClass());
    } else {
      createNopMetrics();
    }

    metrics().counter("configure.called").increment();
  }

  /**
   * Get the (slf4j) logger for this component.
   *
   * <p>Ensure you have called configure (ie super.configure()) before using this. Otherwise you
   * will be given a no-op logger.
   *
   * @return non-null logger
   */
  protected Logger log() {
    // if configure has not been called we might not have a logger, so check and create is necessary
    if (logger == null) {
      createNopLogger();
    }

    return logger;
  }

  /**
   * Get the metrics for this component
   *
   * <p>Ensure you have called configure (ie super.configure()) before using this. Otherwise you
   * will be given a no-op logger.
   *
   * @return non-null metrics
   */
  protected Metrics metrics() {
    // if configure has not been called we might not have a metrics, so check and create is
    // necessary
    if (metrics == null) {
      createNopMetrics();
    }

    return metrics;
  }

  private void createNopLogger() {
    logger = NOPLogger.NOP_LOGGER;
  }

  private void createNopMetrics() {
    metrics = NoOpMetrics.instance();
  }
}
