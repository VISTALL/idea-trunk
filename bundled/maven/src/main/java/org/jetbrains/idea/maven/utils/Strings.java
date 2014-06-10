package org.jetbrains.idea.maven.utils;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author Vladislav.Kaznacheev
 */
public class Strings {
  @NonNls public static final String WHITESPACE = " \t\n\r\f";

  public static List<String> tokenize(final String string, final String delim) {
    final List<String> tokens = new ArrayList<String>();
    for ( StringTokenizer tokenizer = new StringTokenizer(string, delim); tokenizer.hasMoreTokens();) {
      tokens.add(tokenizer.nextToken());
    }
    return tokens;
  }

  public static String detokenize(final Collection<String> list, final char delim) {
    final StringBuilder buffer = new StringBuilder();
    for ( String goal : list) {
      if(buffer.length()!=0){
        buffer.append(delim);
      }
      buffer.append(goal);
    }
    return buffer.toString();
  }

  public static String translateMasks(final Collection<String> masks) {
    final StringBuilder patterns = new StringBuilder();
    for (String mask : masks) {
      if (patterns.length() != 0) {
        patterns.append('|');
      }
      patterns.append(translateToRegex(mask));
    }
    return patterns.toString();
  }

  private static String translateToRegex(final String mask) {
    return mask.replaceAll("\\.", "\\.").replaceAll("\\*", ".*").replaceAll("\\?", ".");
  }
}
