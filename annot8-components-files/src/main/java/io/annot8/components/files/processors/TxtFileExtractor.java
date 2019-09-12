/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.data.Item;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

public class TxtFileExtractor extends AbstractProcessor {

  @Override
  public ProcessorResponse process(Item item) {

    item.getContents(FileContent.class)
        .filter(f -> f.getData().getName().endsWith(".txt"))
        .forEach(
            f -> {
              try {
                File file = f.getData();
                String data =
                    new String(Files.readAllBytes(file.toPath()), Charset.defaultCharset());
                item.createContent(Text.class)
                    .withDescription("Text from " + f.getId())
                    .withData(data)
                    .save();

                // If we processed it ... lets remove it from our item
                // so it doesn't get reprocessed
                item.removeContent(f);

              } catch (Exception e) {
                log().warn("Unable to process file {}", f.getData().getAbsolutePath());
                log().debug("Unable to process file", e);
              }
            });

    // Always carry on it
    return ProcessorResponse.ok();
  }

//  @Override
//  public Stream<ContentCapability> processesContent() {
//    return Stream.of(new ContentCapability(FileContent.class));
//  }
//
//  @Override
//  public Stream<ContentCapability> createsContent() {
//    return Stream.of(new ContentCapability(Text.class));
//  }
}
