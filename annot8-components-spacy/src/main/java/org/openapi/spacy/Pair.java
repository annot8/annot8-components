/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package org.openapi.spacy;

@javax.annotation.processing.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    date = "2021-04-21T10:03:16.456502+01:00[Europe/London]")
public class Pair {
  private String name = "";
  private String value = "";

  public Pair(String name, String value) {
    setName(name);
    setValue(value);
  }

  private void setName(String name) {
    if (!isValidString(name)) {
      return;
    }

    this.name = name;
  }

  private void setValue(String value) {
    if (!isValidString(value)) {
      return;
    }

    this.value = value;
  }

  public String getName() {
    return this.name;
  }

  public String getValue() {
    return this.value;
  }

  private boolean isValidString(String arg) {
    if (arg == null) {
      return false;
    }

    if (arg.trim().isEmpty()) {
      return false;
    }

    return true;
  }
}
