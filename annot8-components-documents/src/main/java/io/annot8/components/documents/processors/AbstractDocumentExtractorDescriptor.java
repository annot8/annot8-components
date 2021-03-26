/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Image;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.common.data.content.Text;

public abstract class AbstractDocumentExtractorDescriptor<
        T extends AbstractDocumentExtractorProcessor<?, S>, S extends DocumentExtractorSettings>
    extends AbstractProcessorDescriptor<T, S> {

  @Override
  public Capabilities capabilities() {
    DocumentExtractorSettings settings = getSettings();

    if (settings == null) settings = new DocumentExtractorSettings();

    SimpleCapabilities.Builder builder =
        new SimpleCapabilities.Builder()
            .withProcessesContent(FileContent.class)
            .withProcessesContent(InputStreamContent.class);

    if (settings.isExtractText()) builder = builder.withCreatesContent(Text.class);

    if (settings.isExtractImages()) builder = builder.withCreatesContent(Image.class);

    if (settings.isExtractTables()) builder = builder.withCreatesContent(TableContent.class);

    if (settings.isDiscardOriginal())
      builder =
          builder
              .withDeletesContent(FileContent.class)
              .withDeletesContent(InputStreamContent.class);

    return builder.build();
  }
}
