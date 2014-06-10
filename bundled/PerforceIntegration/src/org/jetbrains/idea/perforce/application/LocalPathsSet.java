package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.util.SystemInfo;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class LocalPathsSet {
  private final Set<String> mySet;

  public LocalPathsSet() {
    mySet = new HashSet<String>();
  }

  public void add(final String value) {
    final String valueModified = convert(value);
    mySet.add(valueModified);
  }

  private static String convert(final String value) {
    return SystemInfo.isFileSystemCaseSensitive ? value : value.toLowerCase();
  }

  public boolean contains(final File file) {
    final String modified = convert(file.getAbsolutePath());
    return mySet.contains(modified);
  }

  public boolean contains(final String path) {
    final String modified = convert(path);
    return mySet.contains(modified);
  }
}
