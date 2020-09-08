/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.cyber.processors;

import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.processors.AbstractRegexProcessor;
import io.annot8.conventions.AnnotationTypes;
import java.util.regex.Pattern;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/** Extract Urls with the option of doing so in a lenient fashion */
@ComponentName("URL")
@ComponentDescription(
    "Extract valid urls from text, with the option to extract in a lenient fashion")
@SettingsClass(Url.Settings.class)
@ComponentTags({"cyber", "url", "text"})
public class Url extends AbstractProcessorDescriptor<Url.Processor, Url.Settings> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_URL, SpanBounds.class)
        .withProcessesContent(Text.class)
        .build();
  }

  @Override
  public Processor createComponent(Context context, Settings settings) {
    return new Processor(settings.isLenient());
  }

  public static class Processor extends AbstractRegexProcessor {
    private static final Pattern URL_PATTERN =
        Pattern.compile(
            "\\b((https?|ftp)://|www.)(([-a-z0-9]+)\\.)?([-a-z0-9.]+\\.[a-z0-9]+)(:([1-9][0-9]{1,5}))?(/([-a-z0-9+&@#/%=~_|$!:,.]*\\?[-a-z0-9+&@#/%=~_|$!:,.]*)|/([-a-z0-9+&@#/%=~_|$!:,.]*[-a-z0-9+&@#/%=~_|$!:,])|/)?",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern LENIENT_URL_PATTERN =
        Pattern.compile(
            "\\b(?<!@)(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.][a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?([?/]\\S*)?\\b",
            Pattern.CASE_INSENSITIVE);

    public Processor(boolean lenient) {
      super(lenient ? LENIENT_URL_PATTERN : URL_PATTERN, 0, AnnotationTypes.ANNOTATION_TYPE_URL);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private final boolean lenient;

    public Settings() {
      this.lenient = false;
    }

    @JsonbCreator
    public Settings(@JsonbProperty("lenient") boolean lenient) {
      this.lenient = lenient;
    }

    public boolean isLenient() {
      return lenient;
    }

    @Override
    public boolean validate() {
      return true;
    }
  }
}
