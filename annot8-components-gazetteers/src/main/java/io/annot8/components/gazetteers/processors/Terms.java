/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import io.annot8.api.components.annotations.ComponentDescription;
import io.annot8.api.components.annotations.ComponentName;
import io.annot8.api.components.annotations.ComponentTags;
import io.annot8.api.components.annotations.SettingsClass;
import io.annot8.api.context.Context;
import io.annot8.api.settings.Description;
import io.annot8.components.gazetteers.processors.impl.CollectionGazetteer;

import java.util.Collections;
import java.util.List;

@ComponentName("Terms Gazetteer")
@ComponentDescription("Annotate terms within Text using configurable list of terms as the gazetteer")
@ComponentTags({"gazetteer"})
@SettingsClass(Terms.Settings.class)
public class Terms extends AhoCorasick<Terms.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(new CollectionGazetteer(settings.getTerms()), settings);
  }

  public static class Settings extends AhoCorasick.Settings {
    private List<String> terms = Collections.emptyList();

    @Description("The list of terms to extract")
    public List<String> getTerms() {
      return terms;
    }

    public void setTerms(List<String> terms) {
      this.terms = terms;
    }

    @Override
    public boolean validate() {
      return super.validate() && terms != null;
    }
  }
}
