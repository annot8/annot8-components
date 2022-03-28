/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.text.processors;

import java.util.regex.Pattern;

// Deep type hierarchy
@SuppressWarnings("java:S110")
public class RegexProcessor extends AbstractRegexProcessor {

  public RegexProcessor(Pattern pattern, int group, String type) {
    super(pattern, group, type);
  }

  public RegexProcessor(RegexSettings regexSettings) {
    super(regexSettings.getRegex(), regexSettings.getGroup(), regexSettings.getType());
  }
}
