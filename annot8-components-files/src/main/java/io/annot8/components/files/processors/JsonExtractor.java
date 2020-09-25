/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.context.Context;
import io.annot8.api.data.Item;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessor;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.content.FileContent;
import io.annot8.common.data.content.Text;
import io.annot8.conventions.PropertyKeys;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import javax.json.*;
import javax.json.stream.JsonParsingException;

@ComponentName("JSON File Extractor")
@ComponentDescription("Extract content from *.json and *.jsonl files")
@SettingsClass(JsonExtractor.Settings.class)
public class JsonExtractor
    extends AbstractProcessorDescriptor<JsonExtractor.Processor, JsonExtractor.Settings> {

  @Override
  protected Processor createComponent(Context context, JsonExtractor.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(FileContent.class)
        .withCreatesContent(Text.class)
        .withDeletesContent(FileContent.class)
        .build();
  }

  public static class Processor extends AbstractProcessor {
    private final Settings settings;

    public Processor(Settings settings) {
      this.settings = settings;
    }

    @Override
    public ProcessorResponse process(Item item) {

      item.getContents(FileContent.class)
          .forEach(
              f -> {
                int type = isSupportedExtension(f.getData().getName());
                switch (type) {
                  case 1: // JSON
                    if (processJson(f)) {
                      // If we processed it, remove it from item so it doesn't get reprocessed
                      item.removeContent(f);
                    }
                    break;
                  case 2: // JSON-L
                    if (processJsonL(f)) {
                      // If we processed it, remove it from item so it doesn't get reprocessed
                      item.removeContent(f);
                    }
                    break;
                  default:
                    // Not supported
                }
              });

      // Always carry on
      return ProcessorResponse.ok();
    }

    /**
     * @param filename The filename to check
     * @return 1 for JSON file, 2 for JSONL file, -1 for unsupported
     */
    private int isSupportedExtension(String filename) {
      if (settings.getJsonExtension() != null
          && settings.getJsonExtension().stream()
              .anyMatch(s -> filename.toLowerCase().endsWith(s.toLowerCase()))) return 1;

      if (settings.getJsonlExtension() != null
          && settings.getJsonlExtension().stream()
              .anyMatch(s -> filename.toLowerCase().endsWith(s.toLowerCase()))) return 2;

      return -1;
    }

    private boolean processJson(FileContent content) {
      try {
        String data = Files.readString(content.getData().toPath(), Charset.defaultCharset());
        processJson(content.getItem(), data, content.getData(), -1);
      } catch (IOException | JsonParsingException e) {
        log().error("Unable to process JSON file", e);
        return false;
      }

      return true;
    }

    private boolean processJsonL(FileContent content) {
      try (FileInputStream fis = new FileInputStream(content.getData());
          Scanner scanner = new Scanner(fis)) {
        int line = 1;
        while (scanner.hasNextLine()) {
          Item item = content.getItem().createChild();

          processJson(item, scanner.nextLine(), content.getData(), line);
          line++;
        }
      } catch (IOException | JsonParsingException e) {
        log().error("Unable to process JSONL file", e);
        return false;
      }

      return true;
    }

    private void processJson(Item item, String jsonString, File file, int line) {
      try (JsonReader jsonReader =
          Json.createReader(
              new ByteArrayInputStream(jsonString.getBytes(Charset.defaultCharset())))) {
        JsonObject object = jsonReader.readObject();

        for (String key : object.keySet()) {
          if (settings.getContentFields() == null || settings.getContentFields().contains(key)) {
            Object o = getJavaObject(object.get(key));
            if (o == null) continue;

            item.createContent(Text.class)
                .withDescription("Value of " + key + " field from " + file.getName())
                .withData(o.toString())
                .withProperty(PropertyKeys.PROPERTY_KEY_IDENTIFIER, key)
                .withProperty(
                    PropertyKeys.PROPERTY_KEY_SOURCE,
                    file.getAbsolutePath() + (line > 0 ? "#" + line : ""))
                .withProperty(PropertyKeys.PROPERTY_KEY_ACCESSEDAT, LocalDateTime.now())
                .save();
          } else {
            if (object.get(key).getValueType() != JsonValue.ValueType.NULL)
              item.getProperties().set(key, getJavaObject(object.get(key)));
          }
        }
      }
    }

    private Object getJavaObject(JsonValue o) {
      switch (o.getValueType()) {
        case NULL:
          return null;
        case TRUE:
          return true;
        case FALSE:
          return false;
        case STRING:
          JsonString js = (JsonString) o;
          return js.getString();
        case NUMBER:
          JsonNumber jn = (JsonNumber) o;
          return jn.numberValue();
        case ARRAY:
          JsonArray ja = (JsonArray) o;

          List<Object> l = new ArrayList<>(ja.size());
          ja.forEach(v -> l.add(getJavaObject(v)));

          return l;
        case OBJECT:
          JsonObject jo = (JsonObject) o;

          Map<String, Object> m = new HashMap<>(jo.size());
          jo.forEach((k, v) -> m.put(k, getJavaObject(v)));

          return m;
      }

      return null;
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private List<String> contentFields = null;
    private List<String> jsonExtension = List.of("json");
    private List<String> jsonlExtension = List.of("jsonl");

    @Override
    public boolean validate() {
      return true;
    }

    @Description(
        "List of JSON fields to extract as Content, with other fields will be treated as Item properties. If null, all fields will be extracted as Content.")
    public List<String> getContentFields() {
      return contentFields;
    }

    public void setContentFields(List<String> contentFields) {
      this.contentFields = contentFields;
    }

    @Description("List of file extensions to treat as JSON files")
    public List<String> getJsonExtension() {
      return jsonExtension;
    }

    public void setJsonExtension(List<String> jsonExtension) {
      this.jsonExtension = jsonExtension;
    }

    @Description("List of file extensions to treat as JSONL (JSON Lines) files")
    public List<String> getJsonlExtension() {
      return jsonlExtension;
    }

    public void setJsonlExtension(List<String> jsonlExtension) {
      this.jsonlExtension = jsonlExtension;
    }
  }
}
