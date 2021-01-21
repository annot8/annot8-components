/* Annot8 (annot8.io) - Licensed under Apache-2.0. */
package io.annot8.components.base.utils;

import io.annot8.conventions.PathUtils;

public class TypeUtils {
  public static boolean matchesWildcard(String type, String wildcard) {
    String[] t = type.split(PathUtils.SEPARATOR);
    String[] w = wildcard.split(PathUtils.SEPARATOR);

    int i = 0;
    for (int j = 0; j < w.length; j++) {
      String wp = w[j];

      if (i >= t.length) return false;

      String tp = t[i];

      if (wp.equals(tp) || wp.equals("*")) {
        i++;
      } else {
        if (wp.equals("**")) {
          if (j + 1 >= w.length) // This is the last part, so it must match the rest
          return true;

          String wpn = w[j + 1];
          for (; i < t.length; i++) {
            String tpn = t[i];

            if (wpn.equals(tpn)) {
              break;
            }
          }
        } else {
          return false;
        }
      }
    }

    return true;
  }
}
