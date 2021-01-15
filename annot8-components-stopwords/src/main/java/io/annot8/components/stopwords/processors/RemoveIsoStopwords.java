/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.stopwords.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.stopwords.resources.StopwordsIso;
import java.util.List;

@ComponentName("Remove ISO Stopwords")
@ComponentDescription("Remove annotations that are stopwords according to the ISO dictionaries")
@ComponentTags({"annotations", "stopwords"})
@SettingsClass(RemoveIsoStopwords.Settings.class)
public class RemoveIsoStopwords
    extends AbstractProcessorDescriptor<StopwordsProcessor, RemoveIsoStopwords.Settings> {
  @Override
  protected StopwordsProcessor createComponent(Context context, Settings settings) {
    return new StopwordsProcessor(new StopwordsIso(settings.getLanguage()), settings.getTypes());
  }

  @Override
  public Capabilities capabilities() {
    SimpleCapabilities.Builder b =
        new SimpleCapabilities.Builder().withProcessesContent(Text.class);

    if (getSettings().getTypes() == null || getSettings().getTypes().isEmpty()) {
      b = b.withDeletesAnnotations("*", SpanBounds.class);
    } else {
      for (String type : getSettings().getTypes())
        b = b.withDeletesAnnotations(type, SpanBounds.class);
    }

    return b.build();
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String language = "en";
    private List<String> types = List.of();

    @Override
    public boolean validate() {
      return StopwordsIso.SUPPORTED_LANGUAGES.contains(language);
    }

    @Description(value = "The language of the ISO Stopwords dictionary to use", defaultValue = "en")
    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    @Description("List of types to check - if null or empty, then all types are checked")
    public List<String> getTypes() {
      return types;
    }

    public void setTypes(List<String> types) {
      this.types = types;
    }
  }
}
