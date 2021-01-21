/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.kafka.sources;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.SourceResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.data.ItemFactory;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractSource;
import io.annot8.common.components.AbstractSourceDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.InputStreamContent;
import io.annot8.common.data.content.Text;
import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.record.TimestampType;

@ComponentName("Apache Kafka")
@ComponentDescription("Read data from a Apache Kafka topic")
@SettingsClass(KafkaSource.Settings.class)
public class KafkaSource
    extends AbstractSourceDescriptor<KafkaSource.Source, KafkaSource.Settings> {
  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesContent(Text.class)
        .withCreatesContent(InputStreamContent.class)
        .build();
  }

  @Override
  protected Source createComponent(Context context, Settings settings) {
    return new Source(settings);
  }

  public static class Source extends AbstractSource {

    private final Consumer<Object, Object> consumer;
    private static final String CONTENT_DESCRIPTION = "Value of Apache Kafka record";

    public Source(Settings settings) {
      Map<String, Object> props = new HashMap<>();

      props.put("bootstrap.servers", String.join(",", settings.getServers()));
      props.put("group.id", settings.getGroupId());
      props.put("enable.auto.commit", "true");
      props.put("auto.commit.interval.ms", "1000");
      props.put("key.deserializer", settings.getKeyDeserializer());
      props.put("value.deserializer", settings.getValueDeserializer());

      props.putAll(settings.getOverrideProperties());

      consumer = new KafkaConsumer<>(props);
      consumer.subscribe(settings.getTopics());
    }

    // Primarily for testing
    protected Source(Consumer<Object, Object> consumer, List<String> topics) {
      this.consumer = consumer;
      this.consumer.subscribe(topics);
    }

    @Override
    public SourceResponse read(ItemFactory itemFactory) {
      ConsumerRecords<Object, Object> records = consumer.poll(Duration.ofMillis(100));

      if (records.isEmpty()) return SourceResponse.empty();

      records.forEach(record -> createItemFromRecord(itemFactory, record));

      return SourceResponse.ok();
    }

    public static Item createItemFromRecord(
        ItemFactory itemFactory, ConsumerRecord<Object, Object> record) {
      Item item = itemFactory.create();

      record
          .headers()
          .iterator()
          .forEachRemaining(h -> item.getProperties().set("header/" + h.key(), h.value()));

      if (record.key() != null) item.getProperties().set("key", record.key());

      record.leaderEpoch().ifPresent(i -> item.getProperties().set("leaderEpoch", i));

      item.getProperties().set("offset", record.offset());
      item.getProperties().set("partition", record.partition());
      addTimestampToItem(item, record.timestampType(), record.timestamp());
      item.getProperties().set("topic", record.topic());

      Object value = record.value();
      if (value instanceof String) {
        item.createContent(Text.class)
            .withData((String) value)
            .withDescription(CONTENT_DESCRIPTION)
            .save();
      } else if (value instanceof byte[]) {
        item.createContent(InputStreamContent.class)
            .withData(new ByteArrayInputStream((byte[]) value))
            .withDescription(CONTENT_DESCRIPTION)
            .save();
      } else if (value instanceof ByteBuffer) {
        item.createContent(InputStreamContent.class)
            .withData(new ByteArrayInputStream(((ByteBuffer) value).array()))
            .withDescription(CONTENT_DESCRIPTION)
            .save();
      }

      return item;
    }

    public static void addTimestampToItem(Item item, TimestampType timestampType, long timestamp) {
      switch (timestampType) {
        case CREATE_TIME:
          item.getProperties()
              .set(
                  "createdTimestamp",
                  LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
          break;
        case LOG_APPEND_TIME:
          item.getProperties()
              .set(
                  "loggedTimestamp",
                  LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault()));
          break;
        case NO_TIMESTAMP_TYPE:
          // Do nothing, no timestamp
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> topics = List.of();
    private List<String> servers = List.of("localhost:9092");
    private String keyDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String valueDeserializer = "org.apache.kafka.common.serialization.StringDeserializer";
    private String groupId = "annot8";
    private Map<String, Object> overrideProperties = new HashMap<>();

    @Override
    public boolean validate() {
      if (keyDeserializer == null || valueDeserializer == null) return false;

      try {
        Class.forName(keyDeserializer);
        Class.forName(valueDeserializer);
      } catch (ClassNotFoundException e) {
        return false;
      }

      return topics != null
          && !topics.isEmpty()
          && servers != null
          && !servers.isEmpty()
          && groupId != null
          && overrideProperties != null;
    }

    @Description("The Apache Kafka topics to subscribe to")
    public List<String> getTopics() {
      return topics;
    }

    public void setTopics(List<String> topics) {
      this.topics = topics;
    }

    @Description("Apache Kafka servers to connect to")
    public List<String> getServers() {
      return servers;
    }

    public void setServers(List<String> servers) {
      this.servers = servers;
    }

    @Description(
        value = "The deserializer to use for deserializing record keys",
        defaultValue = "org.apache.kafka.common.serialization.StringDeserializer")
    public String getKeyDeserializer() {
      return keyDeserializer;
    }

    public void setKeyDeserializer(String keyDeserializer) {
      this.keyDeserializer = keyDeserializer;
    }

    @Description(
        value = "The deserializer to use for deserializing record values",
        defaultValue = "org.apache.kafka.common.serialization.StringDeserializer")
    public String getValueDeserializer() {
      return valueDeserializer;
    }

    public void setValueDeserializer(String valueDeserializer) {
      this.valueDeserializer = valueDeserializer;
    }

    @Description(value = "Set the group ID for the consumer", defaultValue = "annot8")
    public String getGroupId() {
      return groupId;
    }

    public void setGroupId(String groupId) {
      this.groupId = groupId;
    }

    @Description(
        value =
            "Override properties on the Kafka connection - these take precedence over other values set")
    public Map<String, Object> getOverrideProperties() {
      return overrideProperties;
    }

    public void setOverrideProperties(Map<String, Object> overrideProperties) {
      this.overrideProperties = overrideProperties;
    }
  }
}
