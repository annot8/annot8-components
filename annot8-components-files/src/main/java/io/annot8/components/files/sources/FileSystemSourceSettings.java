/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.files.sources;

import io.annot8.api.settings.Description;
import io.annot8.api.settings.Settings;
import jakarta.json.bind.annotation.JsonbCreator;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class FileSystemSourceSettings implements Settings {

  private Path rootFolder = Paths.get(".");
  private boolean watching = true;
  private boolean recursive = true;
  private boolean reprocessOnModify = true;
  private Set<Pattern> acceptedFileNamePatterns = new HashSet<>();
  private boolean negateAcceptedFileNamePatterns = false;
  private long delay = 0L;

  @JsonbCreator
  public FileSystemSourceSettings() {
    // Do nothing
  }

  public FileSystemSourceSettings(final Path rootFolder) {
    this.rootFolder = rootFolder;
  }

  @Description("Root folder to read from")
  public Path getRootFolder() {
    return rootFolder;
  }

  public void setRootFolder(final Path rootFolder) {
    this.rootFolder = rootFolder;
  }

  @Description("Should the folder be read recursively")
  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  @Description("Should files be reprocessed if they are modified")
  public boolean isReprocessOnModify() {
    return reprocessOnModify;
  }

  public void setReprocessOnModify(boolean reprocessOnModify) {
    this.reprocessOnModify = reprocessOnModify;
  }

  @Description("Accepted file name patterns")
  public Set<Pattern> getAcceptedFileNamePatterns() {
    return acceptedFileNamePatterns;
  }

  public void setAcceptedFileNamePatterns(Set<Pattern> acceptedFileNamePatterns) {
    this.acceptedFileNamePatterns = acceptedFileNamePatterns;
  }

  public void addAcceptedFilePattern(Pattern acceptedFilePattern) {
    this.acceptedFileNamePatterns.add(acceptedFilePattern);
  }

  @Description(
      "If true, then the list of accepted file name patterns is treated as a reject list rather than an accept list")
  public boolean isNegateAcceptedFileNamePatterns() {
    return negateAcceptedFileNamePatterns;
  }

  public void setNegateAcceptedFileNamePatterns(boolean negateAcceptedFileNamePatterns) {
    this.negateAcceptedFileNamePatterns = negateAcceptedFileNamePatterns;
  }

  @Description("Should the folder be watched for changes (true), or just scanned once (false)")
  public boolean isWatching() {
    return watching;
  }

  public void setWatching(boolean watching) {
    this.watching = watching;
  }

  @Description(
      "The length of delay to introduce between the file being detected and the file being processed - can be used to avoid partially copied files being picked up")
  public long getDelay() {
    return delay;
  }

  public void setDelay(long delay) {
    this.delay = delay;
  }

  @Override
  public boolean validate() {
    return rootFolder != null && delay >= 0;
  }
}
