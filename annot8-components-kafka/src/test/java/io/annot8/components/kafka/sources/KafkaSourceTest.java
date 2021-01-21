/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.kafka.sources;

import static org.junit.jupiter.api.Assertions.*;

import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.data.Content;
import io.annot8.api.data.Item;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.implementations.support.context.SimpleContext;
import io.annot8.testing.testimpl.TestItem;
import io.annot8.testing.testimpl.TestItemFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.MockConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Test;

public class KafkaSourceTest {

  @Test
  public void testCreateComponent() {
    KafkaSource ks = new KafkaSource();
    assertNotNull(ks.createComponent(new SimpleContext(), new KafkaSource.Settings()));
  }

  @Test
  public void testCapabilities() {
    KafkaSource ks = new KafkaSource();
    assertNotNull(ks.capabilities());
  }

  @Test
  public void testKafka() {
    MockConsumer<Object, Object> mockConsumer = new MockConsumer<>(OffsetResetStrategy.EARLIEST);

    HashMap<TopicPartition, Long> startOffsets = new HashMap<>();
    TopicPartition tpEn = new TopicPartition("en", 0);
    TopicPartition tpFr = new TopicPartition("fr", 1);
    startOffsets.put(tpEn, 0L);
    startOffsets.put(tpFr, 0L);
    mockConsumer.updateBeginningOffsets(startOffsets);

    KafkaSource.Source source = new KafkaSource.Source(mockConsumer, List.of("en", "fr"));

    mockConsumer.rebalance(Arrays.asList(tpEn, tpFr));
    mockConsumer.addRecord(new ConsumerRecord<>("en", 0, 0, "greeting", "Hello"));
    mockConsumer.addRecord(new ConsumerRecord<>("fr", 1, 0, "greeting", "Bonjour"));

    TestItemFactory tif = new TestItemFactory();

    assertEquals(SourceResponse.ok(), source.read(tif));
    assertEquals(2, tif.getCreatedItems().size());

    assertEquals(SourceResponse.empty(), source.read(tif));

    mockConsumer.addRecord(new ConsumerRecord<>("en", 0, 1, "greeting", "Hi"));
    assertEquals(SourceResponse.ok(), source.read(tif));
    assertEquals(3, tif.getCreatedItems().size());
  }

  @Test
  public void testCreateItemFromRecordText() {
    TestItemFactory tif = new TestItemFactory();

    Item item =
        KafkaSource.Source.createItemFromRecord(
            tif, new ConsumerRecord<>("en", 0, 0L, "greeting", "Hello"));
    assertNotNull(item);

    assertEquals("en", item.getProperties().get("topic").orElse(null));
    assertEquals(0, item.getProperties().get("partition").orElse(null));
    assertEquals(0L, item.getProperties().get("offset").orElse(null));
    assertEquals("greeting", item.getProperties().get("key").orElse(null));
    assertFalse(item.getProperties().get("createdTime").isPresent());
    assertFalse(item.getProperties().get("loggedTime").isPresent());

    Content<?> content = item.getContents().findFirst().orElse(null);
    assertNotNull(content);

    assertEquals("Hello", content.getData());
    assertNotNull(content.getDescription());
  }

  @Test
  public void testCreateItemFromRecordByteArray() throws IOException {
    TestItemFactory tif = new TestItemFactory();

    Item item =
        KafkaSource.Source.createItemFromRecord(
            tif,
            new ConsumerRecord<>(
                "en", 0, 0L, "greeting", "Hello".getBytes(StandardCharsets.UTF_8)));
    assertNotNull(item);

    assertEquals("en", item.getProperties().get("topic").orElse(null));
    assertEquals(0, item.getProperties().get("partition").orElse(null));
    assertEquals(0L, item.getProperties().get("offset").orElse(null));
    assertEquals("greeting", item.getProperties().get("key").orElse(null));
    assertFalse(item.getProperties().get("createdTime").isPresent());
    assertFalse(item.getProperties().get("loggedTime").isPresent());

    Content<?> content = item.getContents().findFirst().orElse(null);
    assertNotNull(content);

    InputStream is = ((InputStreamContent) content).getData();
    assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), is.readAllBytes());
    assertNotNull(content.getDescription());
  }

  @Test
  public void testCreateItemFromRecordByteBuffer() throws IOException {
    TestItemFactory tif = new TestItemFactory();

    Item item =
        KafkaSource.Source.createItemFromRecord(
            tif,
            new ConsumerRecord<>(
                "en",
                0,
                0L,
                "greeting",
                ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8))));
    assertNotNull(item);

    assertEquals("en", item.getProperties().get("topic").orElse(null));
    assertEquals(0, item.getProperties().get("partition").orElse(null));
    assertEquals(0L, item.getProperties().get("offset").orElse(null));
    assertEquals("greeting", item.getProperties().get("key").orElse(null));
    assertFalse(item.getProperties().get("createdTime").isPresent());
    assertFalse(item.getProperties().get("loggedTime").isPresent());

    Content<?> content = item.getContents().findFirst().orElse(null);
    assertNotNull(content);

    InputStream is = ((InputStreamContent) content).getData();
    assertArrayEquals("Hello".getBytes(StandardCharsets.UTF_8), is.readAllBytes());
    assertNotNull(content.getDescription());
  }

  @Test
  public void testAddTimestampToItemNoTimestamp() {
    Item item = new TestItem();
    KafkaSource.Source.addTimestampToItem(
        item, TimestampType.NO_TIMESTAMP_TYPE, System.currentTimeMillis());

    assertTrue(item.getProperties().getAll().isEmpty());
  }

  @Test
  public void testAddTimestampToItemCreateTimestamp() {
    long now = System.currentTimeMillis();

    Item item = new TestItem();
    KafkaSource.Source.addTimestampToItem(item, TimestampType.CREATE_TIME, now);

    LocalDateTime ldt =
        item.getProperties().get("createdTimestamp", LocalDateTime.class).orElse(null);
    assertNotNull(ldt);

    assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault()), ldt);
  }

  @Test
  public void testAddTimestampToItemLoggedTimestamp() {
    long now = System.currentTimeMillis();

    Item item = new TestItem();
    KafkaSource.Source.addTimestampToItem(item, TimestampType.LOG_APPEND_TIME, now);

    LocalDateTime ldt =
        item.getProperties().get("loggedTimestamp", LocalDateTime.class).orElse(null);
    assertNotNull(ldt);

    assertEquals(LocalDateTime.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault()), ldt);
  }

  @Test
  public void testSettings() {
    KafkaSource.Settings settings = new KafkaSource.Settings();

    settings.setGroupId("abc123");
    assertEquals("abc123", settings.getGroupId());

    settings.setTopics(List.of("topic1", "topic2"));
    assertEquals(List.of("topic1", "topic2"), settings.getTopics());

    settings.setServers(List.of("localhost:9092", "localhost:9093"));
    assertEquals(List.of("localhost:9092", "localhost:9093"), settings.getServers());

    settings.setOverrideProperties(Map.of("hello", "world"));
    assertEquals(Map.of("hello", "world"), settings.getOverrideProperties());

    assertTrue(settings.validate());

    settings.setKeyDeserializer("org.apache.kafka.common.serialization.IntegerDeserializer");
    assertEquals(
        "org.apache.kafka.common.serialization.IntegerDeserializer", settings.getKeyDeserializer());

    assertTrue(settings.validate());

    settings.setValueDeserializer("org.apache.kafka.common.serialization.IntegerDeserializer");
    assertEquals(
        "org.apache.kafka.common.serialization.IntegerDeserializer",
        settings.getValueDeserializer());

    assertTrue(settings.validate());

    settings.setKeyDeserializer("foobarbaz");
    assertFalse(settings.validate());

    settings.setKeyDeserializer(null);
    assertFalse(settings.validate());
  }
}
