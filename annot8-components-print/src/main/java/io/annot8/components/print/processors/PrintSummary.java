/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.print.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.bounds.Bounds;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.api.properties.Properties;
import io.annot8.api.stores.AnnotationStore;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentName("Print Summary")
@ComponentDescription(
    "Counts Items and prints a summary of the number and type of annot8 objects created on pipeline close.")
@SettingsClass(PrintSummarySettings.class)
public class PrintSummary
    extends AbstractProcessorDescriptor<PrintSummary.Processor, PrintSummarySettings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Content.class)
        .withProcessesAnnotations("*", Bounds.class)
        .build();
  }

  @Override
  protected Processor createComponent(Context context, PrintSummarySettings settings) {
    return new Processor(settings);
  }

  public static class Processor extends AbstractProcessor {

    private final PrintSummarySettings settings;

    private AtomicInteger itemCount = new AtomicInteger();
    private ConcurrentHashMap<Class<?>, Integer> contentCounter = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> annotationCounter = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> groupCounter = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> propertyCounter = new ConcurrentHashMap<>();

    public Processor(PrintSummarySettings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {
      int items = itemCount.incrementAndGet();
      List<Properties> properties = new ArrayList<>();
      properties.add(item.getProperties());

      processContent(properties, item.getContents());
      processGroups(properties, item.getGroups().getAll());
      processProperties(properties.stream());
      if (settings.getReportProgress() > 0 && items % settings.getReportProgress() == 0) {
        printSummary("Progress update");
      }
      return ProcessorResponse.ok();
    }

    private void processContent(List<Properties> properties, Stream<Content<?>> content) {
      List<Annotation> annotations = new ArrayList<>();
      content
          .collect(Collectors.groupingBy(Object::getClass))
          .forEach(
              (type, contents) -> {
                int total =
                    contentCounter.compute(
                        type, (k, v) -> v == null ? contents.size() : v + contents.size());
                log().trace("Added {} {} contents, now {}", contents.size(), type, total);
                contents.forEach(c -> properties.add(c.getProperties()));
                contents.stream()
                    .map(Content::getAnnotations)
                    .flatMap(AnnotationStore::getAll)
                    .forEach(annotations::add);
              });

      processAnnotations(properties, annotations.stream());
    }

    private void processGroups(List<Properties> properties, Stream<Group> allGroups) {
      allGroups
          .collect(Collectors.groupingBy(Group::getType))
          .forEach(
              (type, groups) -> {
                int total =
                    groupCounter.compute(
                        type, (k, v) -> v == null ? groups.size() : v + groups.size());
                log().trace("Added {} {} groups, now {}", groups.size(), type, total);
                groups.forEach(c -> properties.add(c.getProperties()));
              });
    }

    private void processAnnotations(
        List<Properties> properties, Stream<Annotation> allAnnotations) {
      allAnnotations
          .collect(Collectors.groupingBy(Annotation::getType))
          .forEach(
              (type, annotations) -> {
                int total =
                    annotationCounter.compute(
                        type, (k, v) -> v == null ? annotations.size() : v + annotations.size());
                log().trace("Added {} {} annotations, now {}", annotations.size(), type, total);
                annotations.forEach(c -> properties.add(c.getProperties()));
              });
    }

    private void processProperties(Stream<Properties> properties) {
      properties
          .map(Properties::getAll)
          .flatMap(m -> m.keySet().stream())
          .collect(Collectors.groupingBy(id -> id))
          .forEach(
              (type, property) -> {
                int total =
                    propertyCounter.compute(
                        type, (k, v) -> v == null ? property.size() : v + property.size());
                log().trace("Added {} {} properties, now {}", property.size(), type, total);
              });
    }

    private void printSummary(String title) {
      settings.output(log(), title);
      settings.output(log(), String.format("Processed %s items and created:", itemCount.get()));
      contentCounter.forEach(
          (type, count) ->
              settings.output(
                  log(), String.format("%s \tContent type\t %s", count, type.getSimpleName())));
      annotationCounter.forEach(
          (type, count) ->
              settings.output(log(), String.format("%s \tAnnotation type\t %s", count, type)));
      groupCounter.forEach(
          (type, count) ->
              settings.output(log(), String.format("%s \tGroup type\t %s", count, type)));
      propertyCounter.forEach(
          (type, count) ->
              settings.output(log(), String.format("%s \tProperty type\t %s", count, type)));
    }

    @Override
    public void close() {
      printSummary("Pipeline summary");
      super.close();
    }
  }
}
