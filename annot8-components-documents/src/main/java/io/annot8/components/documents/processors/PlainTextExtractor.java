/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.exceptions.ProcessingException;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Table;
import io.annot8.components.documents.data.ExtractionWithProperties;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ComponentName("Plain Text Extractor")
@ComponentDescription(
    "Extracts text from any file, treating the file contents as raw text regardless of file type")
@ComponentTags({"documents", "extractor", "text"})
@SettingsClass(DocumentExtractorSettings.class)
public class PlainTextExtractor
    extends AbstractDocumentExtractorDescriptor<
        PlainTextExtractor.Processor, DocumentExtractorSettings> {

  @Override
  protected Processor createComponent(Context context, DocumentExtractorSettings settings) {
    return new Processor(context, settings);
  }

  public static class Processor
      extends AbstractDocumentExtractorProcessor<String, DocumentExtractorSettings> {

    public Processor(Context context, DocumentExtractorSettings settings) {
      super(context, settings);
    }

    @Override
    protected boolean isMetadataSupported() {
      return false;
    }

    @Override
    protected boolean isImagesSupported() {
      return false;
    }

    @Override
    protected boolean isTablesSupported() {
      return false;
    }

    @Override
    protected boolean acceptFile(FileContent file) {
      return true;
    }

    @Override
    protected boolean acceptInputStream(InputStreamContent inputStream) {
      return true;
    }

    @Override
    protected String extractDocument(FileContent file) throws IOException {
      try (Stream<String> lines = Files.lines(file.getData().toPath(), StandardCharsets.UTF_8)) {
        return lines.collect(Collectors.joining("\n"));
      }
    }

    @Override
    protected String extractDocument(InputStreamContent inputStreamContent) throws IOException {
      try (BufferedReader reader =
          new BufferedReader(
              new InputStreamReader(inputStreamContent.getData(), StandardCharsets.UTF_8))) {
        return reader.lines().collect(Collectors.joining("\n"));
      }
    }

    @Override
    protected Map<String, Object> extractMetadata(String doc) {
      return Collections.emptyMap();
    }

    @Override
    protected Collection<ExtractionWithProperties<String>> extractText(String doc) {
      return Collections.singletonList(new ExtractionWithProperties<>(doc));
    }

    @Override
    protected Collection<ExtractionWithProperties<BufferedImage>> extractImages(String doc) {
      return Collections.emptyList();
    }

    @Override
    protected Collection<ExtractionWithProperties<Table>> extractTables(String doc)
        throws ProcessingException {
      return Collections.emptyList();
    }
  }
}
