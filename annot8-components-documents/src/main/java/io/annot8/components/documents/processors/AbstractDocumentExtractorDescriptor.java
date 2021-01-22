/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.documents.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.*;

public abstract class AbstractDocumentExtractorDescriptor<
        T extends AbstractDocumentExtractorProcessor<?>>
    extends AbstractProcessorDescriptor<T, DocumentExtractorSettings> {
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

    if(settings.isDiscardOriginal())
      builder = builder
        .withDeletesContent(FileContent.class)
        .withDeletesContent(InputStreamContent.class);

    return builder.build();
  }
}
