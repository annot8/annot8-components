/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.gazetteers.processors;

import io.annot8.api.annotations.Annotation;
import io.annot8.api.annotations.Group;
import io.annot8.api.capabilities.Capabilities;
import io.annot8.api.settings.Description;
import io.annot8.common.components.AbstractProcessorDescriptor;
import io.annot8.common.components.capabilities.SimpleCapabilities;
import io.annot8.common.data.bounds.SpanBounds;
import io.annot8.common.data.content.Text;
import io.annot8.components.base.text.processors.AbstractTextProcessor;
import io.annot8.conventions.AnnotationTypes;
import io.annot8.conventions.GroupTypes;
import io.annot8.conventions.PropertyKeys;
import io.annot8.utils.text.PluralUtils;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AhoCorasick<S extends AhoCorasick.Settings>
    extends AbstractProcessorDescriptor<AhoCorasick.Processor, S> {

  @Override
  public Capabilities capabilities() {
    return new SimpleCapabilities.Builder()
        .withProcessesContent(Text.class)
        .withCreatesAnnotations(getSettings().getType(), SpanBounds.class)
        .withCreatesGroups("aliases")
        .build();
  }

  public static class Processor extends AbstractTextProcessor {
    private final Trie trie;

    private final Gazetteer gazetteer;
    private final Settings settings;

    public Processor(Gazetteer gazetteer, Settings settings) {
      this.gazetteer = gazetteer;
      this.settings = settings;

      trie = buildTrie(gazetteer, settings);
    }

    protected Trie buildTrie(Gazetteer gazetteer, Settings settings) {
      Trie.TrieBuilder builder = Trie.builder().onlyWholeWords();

      if (!settings.isCaseSensitive()) {
        builder = builder.ignoreCase();
      }

      for (String s : gazetteer.getValues()) {
        builder = builder.addKeyword(s);
        if (settings.isPlurals()) {
          builder = builder.addKeyword(PluralUtils.pluralise(s));
        }
      }

      return builder.build();
    }

    @Override
    protected void process(Text content) {
      TransformedString norm =
          settings.isExactWhitespace()
              ? noopString(content.getData())
              : normaliseString(content.getData());
      Collection<Emit> emits = trie.parseText(norm.getTransformedString());

      Map<String, List<Annotation>> aliasGroups = new HashMap<>();

      for (Emit emit : emits) {
        Integer start = norm.getMapping().get(emit.getStart());
        Integer end = norm.getMapping().get(emit.getEnd() + 1);

        Annotation.Builder builder =
            content
                .getAnnotations()
                .create()
                .withBounds(new SpanBounds(start, end))
                .withType(settings.getType());

        if (settings.getSubType() != null)
          builder = builder.withProperty(PropertyKeys.PROPERTY_KEY_SUBTYPE, settings.getSubType());

        if (settings.isAdditionalData()) {
          for (Map.Entry<String, Object> e :
              gazetteer.getAdditionalData(emit.getKeyword()).entrySet()) {
            builder = builder.withProperty(e.getKey(), e.getValue());
          }
        }

        Annotation a = builder.save();

        String key = generateKey(gazetteer.getAliases(emit.getKeyword()));
        aliasGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(a);
      }

      for (List<Annotation> group : aliasGroups.values()) {
        if (group.size() == 1) continue;

        Group.Builder builder =
            content.getItem().getGroups().create().withType(GroupTypes.GROUP_PREFIX + "aliases");

        group.forEach(a -> builder.withAnnotation("alias", a));

        builder.save();
      }
    }

    private String generateKey(Collection<String> aliases) {
      return aliases.stream().map(String::toLowerCase).sorted().collect(Collectors.joining("|"));
    }

    protected static TransformedString noopString(String s) {
      Map<Integer, Integer> indexMap = new HashMap<>();

      for (int i = 0; i < s.length(); i++) indexMap.put(i, i);

      return new TransformedString(s, s, indexMap);
    }

    /**
     * Replace repeated horizontal whitespace characters with a single space character, and return a
     * TransformedString that maps between the original and normalised string
     *
     * @param s The string to normalise
     * @return A TransformedString mapping between the original and normalised text
     */
    protected static TransformedString normaliseString(String s) {
      String remaining = s;
      StringBuilder builder = new StringBuilder();

      String previousChar = "";
      Map<Integer, Integer> indexMap = new HashMap<>();

      Integer index = 0;

      while (!remaining.isEmpty()) {
        indexMap.put(builder.length(), index);
        index++;

        String character = remaining.substring(0, 1);
        remaining = remaining.substring(1);

        if (!(character.matches("\\h") && previousChar.matches("\\h"))) {
          if (character.matches("\\h")) {
            character = " ";
          }

          builder.append(character);
        }

        previousChar = character;
      }
      indexMap.put(builder.length(), index);

      return new TransformedString(s, builder.toString(), indexMap);
    }
  }

  public static class Settings implements io.annot8.api.settings.Settings {

    private boolean caseSensitive = false;
    private boolean exactWhitespace = false;
    private String type = AnnotationTypes.ENTITY_PREFIX + "unknown";
    private String subType = null;
    private boolean additionalData = false;
    private boolean plurals = true;

    @Override
    public boolean validate() {
      return type != null && !type.isEmpty();
    }

    @Description("Should comparisons be done case-sensitively?")
    public boolean isCaseSensitive() {
      return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
      this.caseSensitive = caseSensitive;
    }

    @Description("Should whitespace in document be preserved?")
    public boolean isExactWhitespace() {
      return exactWhitespace;
    }

    public void setExactWhitespace(boolean exactWhitespace) {
      this.exactWhitespace = exactWhitespace;
    }

    @Description("The annotation type")
    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    @Description("The annotation subtype, or null")
    public String getSubType() {
      return subType;
    }

    public void setSubType(String subType) {
      this.subType = subType;
    }

    @Description("Should additional data be added to annotations as properties?")
    public boolean isAdditionalData() {
      return additionalData;
    }

    public void setAdditionalData(boolean additionalData) {
      this.additionalData = additionalData;
    }

    @Description("Should we accept plurals as matches?")
    public boolean isPlurals() {
      return plurals;
    }

    public void setPlurals(boolean plurals) {
      this.plurals = plurals;
    }
  }

  /**
   * A simple class to hold two strings and the mapping between them. Used for when a string has
   * been transformed by some function.
   */
  protected static class TransformedString {
    private final String original;
    private final String transformed;
    private final Map<Integer, Integer> map;

    /** Create a new TransformedString */
    public TransformedString(
        String originalString, String transformedString, Map<Integer, Integer> mapping) {
      original = originalString;
      transformed = transformedString;
      map = mapping;
    }

    /** Get the original string */
    public String getOriginalString() {
      return original;
    }

    /** Get the transformed string */
    public String getTransformedString() {
      return transformed;
    }

    /** Get the mapping from the transformed string back to the original string */
    public Map<Integer, Integer> getMapping() {
      return map;
    }
  }
}
