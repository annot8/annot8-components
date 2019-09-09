/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import io.annot8.core.components.Processor;
import io.annot8.core.context.Context;
import io.annot8.core.data.BaseItem;
import io.annot8.core.data.Item;
import io.annot8.testing.testimpl.TestContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestItemFactory;

public class EmlFileExtractorTest {

  @Test
  public void test() throws Exception {

    try (Processor p = new EmlFileExtractor()) {

      Context context = new TestContext();
      p.configure(context);

      TestItem item = new TestItem();
      TestItemFactory itemFactory = (TestItemFactory) item.getItemFactory();

      URL resource = EmlFileExtractorTest.class.getResource("test_sample_message.eml"); // Based on
      // https://www.phpclasses.org/browse/file/14672.html
      File f = Paths.get(resource.toURI()).toFile();

      item.create(FileContent.class).withName("test_sample_message.eml").withData(f).save();

      p.process(item);

      assertEquals("mlemos <mlemos@acm.org>", item.getProperties().get("From").get());
      assertEquals("Manuel Lemos <mlemos@linux.local>", item.getProperties().get("To").get());
      assertEquals(
          "http://www.phpclasses.org/mimemessage $Revision: 1.63 $ (mail)",
          item.getProperties().get("X-Mailer").get());
      assertEquals("Sat, 30 Apr 2005 19:28:29 -0300", item.getProperties().get("Date").get());
      assertEquals(
          Arrays.asList(
              "Original file from https://www.phpclasses.org/browse/file/14672.html",
              "Modified by James Baker"),
          item.getProperties().get("Comment").get());

      Text text1 = (Text) item.getContentByName("body-1-1-1").findFirst().get();
      assertNotNull(text1);
      assertEquals(
          "text/plain; charset=ISO-8859-1", text1.getProperties().get("Content-Type").get());
      assertTrue(text1.getData().contains("Please use an HTML capable mail program"));

      Text text2 = (Text) item.getContentByName("body-1-1-2").findFirst().get();
      assertNotNull(text2);
      assertEquals(
          "text/html; charset=ISO-8859-1", text2.getProperties().get("Content-Type").get());
      assertTrue(
          text2
              .getData()
              .contains(
                  "Testing Manuel Lemos' MIME E-mail composing and sending PHP class: HTML message"));

      final List<Item> newItems = itemFactory.getCreatedItems();
      assertEquals(3, newItems.size());

      BaseItem logoItem = newItems.get(0);
      assertTrue(logoItem.hasContentOfName("logo.gif"));

      BaseItem backgroundItem = newItems.get(1);
      assertTrue(backgroundItem.hasContentOfName("background.gif"));

      BaseItem attachmentItem = newItems.get(2);
      InputStreamContent inputStreamContent =
          (InputStreamContent) attachmentItem.getContentByName("attachment.txt").findFirst().get();
      assertNotNull(inputStreamContent);
      String content =
          CharStreams.toString(
              new InputStreamReader(inputStreamContent.getData(), Charsets.ISO_8859_1));
      assertEquals("This is just a plain text attachment file named attachment.txt .", content);
      assertEquals("This is just a plain text attachment file named attachment.txt .", content);
    }
  }
}
