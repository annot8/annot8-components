/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.comms.processors;

import com.google.i18n.phonenumbers.PhoneNumberMatch;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
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
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.PropertyKeys;

@ComponentName("Telephone Number")
@ComponentDescription("Extract Telephone numbers from text")
@ComponentTags({"communications", "telephone"})
@SettingsClass(Telephone.Settings.class)
public class Telephone
    extends AbstractProcessorDescriptor<Telephone.Processor, Telephone.Settings> {

  @Override
  protected Processor createComponent(Context context, Telephone.Settings settings) {
    return new Processor(settings);
  }

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER, SpanBounds.class)
        .build();
  }

  public static class Processor extends AbstractTextProcessor {

    private final String defaultRegion;
    private final PhoneNumberUtil.Leniency leniency;

    public Processor(Settings settings) {
      this.defaultRegion = settings.getDefaultRegion();
      this.leniency = settings.getLeniency();
    }

    @Override
    protected void process(Text content) {
      for (PhoneNumberMatch match :
          PhoneNumberUtil.getInstance()
              .findNumbers(content.getData(), defaultRegion, leniency, Long.MAX_VALUE)) {
        content
            .getAnnotations()
            .create()
            .withBounds(new SpanBounds(match.start(), match.end()))
            .withType(AnnotationTypes.ANNOTATION_TYPE_PHONENUMBER)
            .withProperty(
                PropertyKeys.PROPERTY_KEY_VALUE,
                PhoneNumberUtil.getInstance()
                    .format(match.number(), PhoneNumberUtil.PhoneNumberFormat.E164))
            .withProperty(
                PropertyKeys.PROPERTY_KEY_SUBTYPE,
                PhoneNumberUtil.getInstance().getNumberType(match.number()))
            .save();
      }
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {
    private String defaultRegion = null;
    private PhoneNumberUtil.Leniency leniency = PhoneNumberUtil.Leniency.VALID;

    public Settings() {
      // Empty constructor
    }

    public Settings(String defaultRegion, PhoneNumberUtil.Leniency leniency) {
      this.defaultRegion = defaultRegion;
      this.leniency = leniency;
    }

    @Override
    public boolean validate() {
      return leniency != null
          && (defaultRegion == null
              || PhoneNumberUtil.getInstance().getSupportedRegions().contains(defaultRegion));
    }

    @Description(
        "Two letter country code to be used as the default region for phone numbers where the number is not a full international phone number. Set to null if you only want to extract full international phone numbers.")
    public String getDefaultRegion() {
      return defaultRegion;
    }

    public void setDefaultRegion(String defaultRegion) {
      this.defaultRegion = defaultRegion;
    }

    @Description("The leniency to use when parsing numbers")
    public PhoneNumberUtil.Leniency getLeniency() {
      return leniency;
    }

    public void setLeniency(PhoneNumberUtil.Leniency leniency) {
      this.leniency = leniency;
    }
  }
}
