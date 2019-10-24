/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.geo.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.context.Context;
import io.annot8.api.settings.NoSettings;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

@ComponentName("Nationality")
@ComponentDescription("Extract nationality demonyms, e.g. French, from text")
public class Nationality extends AbstractProcessorDescriptor<Nationality.Processor, NoSettings> {

  @Override
  protected Processor createComponent(Context context, NoSettings settings) {
    return new Processor();
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_NATIONALITY, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private Map<String, Pattern> countryPatterns = new HashMap<>();
    private Map<String, String> countryCodes = new HashMap<>();

    public Processor() {
      JsonReader reader =
          Json.createReader(Nationality.class.getResourceAsStream("countries.json"));
      reader
          .readArray()
          .forEach(
              jv -> {
                JsonObject jo = jv.asJsonObject();

                String countryDemonym = jo.getString("demonym").toLowerCase();
                String countryCode = jo.getString("cca3");

                if (countryDemonym.length() > 1) {
                  Pattern p =
                      Pattern.compile("\\b" + countryDemonym + "\\b", Pattern.CASE_INSENSITIVE);
                  countryPatterns.put(countryDemonym, p);
                  countryCodes.put(countryDemonym, countryCode);
                }
              });
    }

    @Override
    protected void process(Text content) {
      for (Map.Entry<String, Pattern> e : countryPatterns.entrySet()) {
        Matcher m = e.getValue().matcher(content.getData());
        String countryCode = countryCodes.get(e.getKey());

        while (m.find()) {
          content
              .getAnnotations()
              .create()
              .withType(AnnotationTypes.ANNOTATION_TYPE_NATIONALITY)
              .withBounds(new SpanBounds(m.start(), m.end()))
              .withProperty(PropertyKeys.PROPERTY_KEY_NATIONALITY, e.getKey())
              .withProperty("countryCode", countryCode)
              .save();
        }
      }
    }
  }
}
