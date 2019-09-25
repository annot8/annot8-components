/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.conventions;

/** Standard names for common group types */
public class GroupTypes {
  public static final String GROUP_PREFIX = "group" + PathUtils.SEPARATOR;

  public static final String GRAMMAR_PREFIX = GROUP_PREFIX + "grammar" + PathUtils.SEPARATOR;

  public static final String GROUP_TYPE_RELATION = GROUP_PREFIX + "relation";
  public static final String RELATION_PREFIX = GROUP_TYPE_RELATION + PathUtils.SEPARATOR;

  public static final String GROUP_TYPE_EVENT = GROUP_PREFIX + "event";
  public static final String EVENT_PREFIX = GROUP_TYPE_EVENT + PathUtils.SEPARATOR;

  public static final String GROUP_TYPE_GRAMMAR_COREFERENCE = GRAMMAR_PREFIX + "coreference";
  public static final String GROUP_TYPE_GRAMMAR_PHRASE = GRAMMAR_PREFIX + "phrase";
  public static final String GROUP_TYPE_RELATION_OWNS = RELATION_PREFIX + "owns";
  public static final String GROUP_TYPE_RELATION_COMMUNICATED = RELATION_PREFIX + "communicated";

  private GroupTypes() {
    // No constructor - only access to public methods
  }
}
