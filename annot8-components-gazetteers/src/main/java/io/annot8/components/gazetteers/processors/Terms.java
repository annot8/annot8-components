/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import io.annot8.api.context.Context;
import io.annot8.components.gazetteers.processors.impl.CollectionGazetteer;
import java.util.Collections;
import java.util.List;

public class Terms extends AhoCorasick<Terms.Settings> {

  @Override
  protected Processor createComponent(Context context, Settings settings) {
    return new Processor(new CollectionGazetteer(settings.getTerms()), settings);
  }

  public static class Settings extends AhoCorasick.Settings {
    private List<String> terms = Collections.emptyList();

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
