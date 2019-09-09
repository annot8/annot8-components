/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.conventions;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

public final class PathUtils {

  public static final String SEPARATOR = "/";

  private static final Joiner JOINER = Joiner.on(SEPARATOR).skipNulls();
  private static final Splitter SPLITTER = Splitter.on(SEPARATOR).omitEmptyStrings().trimResults();

  private PathUtils() {
    // Singleton
  }

  public static String join(String... parts) {
    return JOINER.join(parts);
  }

  public static String join(Iterable<String> iterable) {
    return JOINER.join(iterable);
  }

  public static String join(Iterator<String> iterator) {
    return JOINER.join(iterator);
  }

  public static String join(Stream<String> stream) {
    return stream.filter(Objects::nonNull).collect(Collectors.joining(SEPARATOR));
  }

  public static String[] split(String path) {
    List<String> l = SPLITTER.splitToList(path);
    return l.toArray(new String[l.size()]);
  }

  public static Iterable<String> splitToIterable(String path) {
    return SPLITTER.split(path);
  }

  public static Stream<String> splitToStream(String path) {
    return splitToList(path).stream();
  }

  public static List<String> splitToList(String path) {
    return SPLITTER.splitToList(path);
  }
}
