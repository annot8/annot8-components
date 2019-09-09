/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.common.data.content.FileContent;
import io.annot8.components.base.components.AbstractComponent;
import io.annot8.core.capabilities.ProcessesContent;
import io.annot8.core.components.Processor;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Content;
import io.annot8.core.data.Item;

// This is more like premature optimisation... really but I guess it's sensible to clean up
// the pipeline as we go.
@ProcessesContent(FileContent.class)
public class DiscardUnextractedFiles extends AbstractComponent implements Processor {

  @Override
  public ProcessorResponse process(Item item) {

    item.getContents(FileContent.class).map(Content::getName).forEach(item::removeContent);

    boolean noOtherContent = item.getContents().count() == 0;

    if (noOtherContent) {
      item.discard();
    }

    return ProcessorResponse.ok();
  }
}
