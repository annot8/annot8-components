/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.conventions;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class PathUtils {

  public static final String SEPARATOR = "/";

  private PathUtils() {
    // Singleton
  }

  public static String join(String... parts) {
    return join(Stream.of(parts));
  }

  public static String join(Iterable<String> iterable) {
    return join(StreamSupport.stream(iterable.spliterator(), false));
  }

  public static String join(Iterator<String> iterator) {
    return join(StreamSupport.stream(
      Spliterators.spliteratorUnknownSize(
        iterator, Spliterator.ORDERED), false));
  }

  public static String join(Stream<String> stream) {
    return stream.filter(Objects::nonNull).collect(Collectors.joining(SEPARATOR));
  }

  public static String[] split(String path) {
    List<String> l = splitToList(path);
    return l.toArray(new String[l.size()]);
  }

  public static Iterable<String> splitToIterable(String path) {
    return splitToList(path);
  }

  public static Stream<String> splitToStream(String path) {
    return splitToList(path).stream();
  }

  public static List<String> splitToList(String path) {
    return Arrays.stream(path.split(SEPARATOR))
      .filter(s -> !s.isEmpty())
      .map(String::trim)
      .collect(Collectors.toList());
  }

}
