/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.items.processors;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

public class ItemPropertyAsDateTimeTest {
  @Test
  public void testCapabilities() {
    ItemPropertyAsDateTime d = new ItemPropertyAsDateTime();
    assertNotNull(d.capabilities());
  }

  @Test
  public void testCreate() {
    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();

    ItemPropertyAsDateTime d = new ItemPropertyAsDateTime();

    assertNotNull(d.createComponent(new SimpleContext(), s));
  }

  @Test
  public void testStringToDate() {
    Item item = new TestItem();
    item.getProperties().set("date", "2021-04-23 10:44:12");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATE);
    s.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(
          LocalDate.of(2021, Month.APRIL, 23), item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testStringToTime() {
    Item item = new TestItem();
    item.getProperties().set("date", "2021-04-23 10:44:12");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.TIME);
    s.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(LocalTime.of(10, 44, 12), item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testStringToDateTime() {
    Item item = new TestItem();
    item.getProperties().set("date", "2021-04-23 10:44:12");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATETIME);
    s.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(
          LocalDateTime.of(2021, Month.APRIL, 23, 10, 44, 12),
          item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testStringToZonedDateTime() {
    Item item = new TestItem();
    item.getProperties().set("date", "2021-04-23 10:44:12Z");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.ZONED_DATETIME);
    s.setDateTimeFormat("yyyy-MM-dd HH:mm:ssX");

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(
          ZonedDateTime.of(2021, 4, 23, 10, 44, 12, 0, ZoneId.of("Z")),
          item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testStringToOffsetDateTime() {
    Item item = new TestItem();
    item.getProperties().set("date", "2021-04-23 10:44:12Z");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.OFFSET_DATETIME);
    s.setDateTimeFormat("yyyy-MM-dd HH:mm:ssX");

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(
          OffsetDateTime.of(2021, 4, 23, 10, 44, 12, 0, ZoneOffset.UTC),
          item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testDateTimeToTime() {
    Item item = new TestItem();
    item.getProperties().set("date", LocalDateTime.of(2021, Month.APRIL, 23, 10, 44, 12));

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.TIME);

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(LocalTime.of(10, 44, 12), item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testDateTimeToDate() {
    Item item = new TestItem();
    item.getProperties().set("date", LocalDateTime.of(2021, Month.APRIL, 23, 10, 44, 12));

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATE);

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      p.process(item);

      assertEquals(
          LocalDate.of(2021, Month.APRIL, 23), item.getProperties().get("date").orElseThrow());
    }
  }

  @Test
  public void testDiscardUnparseable() {
    Item item = new TestItem();
    item.getProperties().set("date", "unknown");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATE);
    s.setErrorOnUnparseable(false);

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(item));

      assertFalse(item.getProperties().has("date"));
    }
  }

  @Test
  public void testErrorUnparseable() {
    Item item = new TestItem();
    item.getProperties().set("date", "unknown");

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATE);
    s.setErrorOnUnparseable(true);

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      assertEquals(ProcessorResponse.Status.ITEM_ERROR, p.process(item).getStatus());
    }
  }

  @Test
  public void testDiscardUnconvertible() {
    Item item = new TestItem();
    item.getProperties().set("date", LocalTime.now());

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATE);
    s.setErrorOnUnparseable(false);

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      assertEquals(ProcessorResponse.ok(), p.process(item));

      assertFalse(item.getProperties().has("date"));
    }
  }

  @Test
  public void testErrorUnconvertible() {
    Item item = new TestItem();
    item.getProperties().set("date", LocalTime.now());

    ItemPropertyAsDateTime.Settings s = new ItemPropertyAsDateTime.Settings();
    s.setKey("date");
    s.setDateTimeType(ItemPropertyAsDateTime.DateTimeType.DATE);
    s.setErrorOnUnparseable(true);

    try (ItemPropertyAsDateTime.Processor p = new ItemPropertyAsDateTime.Processor(s)) {
      assertEquals(ProcessorResponse.Status.ITEM_ERROR, p.process(item).getStatus());
    }
  }
}
