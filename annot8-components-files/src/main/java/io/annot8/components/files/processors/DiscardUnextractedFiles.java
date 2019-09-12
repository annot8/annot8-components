/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.content.FileContent;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;

public class DiscardUnextractedFiles extends AbstractProcessor {

  @Override
  public ProcessorResponse process(Item item) {

    item.getContents(FileContent.class).forEach(item::removeContent);

    boolean noOtherContent = item.getContents().count() == 0;

    if (noOtherContent) {
      item.discard();
    }

    return ProcessorResponse.ok();
  }

//  @Override
//  public Stream<ContentCapability> processesContent() {
//    return Stream.of(new ContentCapability(FileContent.class));
//  }
//
//  @Override
//  public Stream<ContentCapability> deletesContent() {
//    return Stream.of(new ContentCapability(FileContent.class));
//  }
}
