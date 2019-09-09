/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.TableContent;
import io.annot8.components.files.AbstractCSVDataTest;
import io.annot8.core.components.responses.ProcessorResponse;
import io.annot8.core.components.responses.ProcessorResponse.Status;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;

public class CSVExtractorTest extends AbstractCSVDataTest {

  @Test
  public void testProcess() {
    TestItem item = new TestItem();
    FileContent content = Mockito.mock(FileContent.class);
    when(content.getData()).thenReturn(getTestData("test.csv"));
    when(content.getId()).thenReturn("testContent");
    doReturn(FileContent.class).when(content).getContentClass();
    item.save(content);

    CSVExtractor extractor = new CSVExtractor();
    TestContext context = new TestContext(new CSVExtractorSettings(true));
    ProcessorResponse response = null;
    try {
      extractor.configure(context);
      response = extractor.process(item);
    } catch (Exception e) {
      fail("No error expected during test", e);
    }

    assertEquals(Status.OK, response.getStatus());

    List<TableContent> tables = item.getContents(TableContent.class).collect(Collectors.toList());
    assertEquals(1, tables.size());

    TableContent tableContent = tables.get(0);
    assertEquals("test.csv", tableContent.getName());
  }
}
