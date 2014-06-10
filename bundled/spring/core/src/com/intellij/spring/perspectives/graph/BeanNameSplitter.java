package com.intellij.spring.perspectives.graph;

import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BeanNameSplitter {

  @NonNls private static final String PATTERN = "([/\\-\\.A-Z]?[a-z]+)|([A-Z]+)";
  private static final Pattern REGEX = Pattern.compile(PATTERN);

  public static String[] split(@NotNull final String name) {
    final Matcher matcher = REGEX.matcher(name);
    final List<String> parts = new ArrayList<String>();
    while (matcher.find()) {
      parts.add(matcher.group().toLowerCase());
    }
    return ArrayUtil.toStringArray(parts);
  }
}
