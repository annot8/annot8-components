/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.processors;

import java.util.regex.Pattern;

public class RegexProcessor extends AbstractRegexProcessor {

  public RegexProcessor(Pattern pattern, int group, String type) {
    this.pattern = pattern;
    this.group = group;
    this.type = type;
  }

  public RegexProcessor(RegexSettings regexSettings) {
    this(
        Pattern.compile(regexSettings.getRegex()),
        regexSettings.getGroup(),
        regexSettings.getType());
  }
}
