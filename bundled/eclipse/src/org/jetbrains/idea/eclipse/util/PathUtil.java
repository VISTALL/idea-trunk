package org.jetbrains.idea.eclipse.util;

import com.intellij.openapi.util.io.FileUtil;

import java.io.File;

public class PathUtil {

  public static final String UNRESOLVED_PREFIX = "?";
  public static final String HTTP_PREFIX = "http://";
  public static final String HTTPS_PREFIX = "https://";

  public static String normalize(String path) {
    path = FileUtil.toSystemIndependentName(path);
    if (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    while (path.contains("/./")) {
      path = path.replace("/./", "/");
    }
    if (path.startsWith("./")) {
      path = path.substring(2);
    }
    if (path.endsWith("/.")) {
      path = path.substring(0, path.length() - 2);
    }

    while ( true ) {
      int index = path.indexOf("/..");
      if ( index < 0 ) break;
      int slashIndex = path.substring(0,index).lastIndexOf("/");
      if ( slashIndex < 0 ) break;
      path = path.substring(0, slashIndex ) + path.substring(index+3);
    }

    return path;
  }

  public static boolean isAbsolute(String path) {
    return new File(path).isAbsolute();
  }

  public static String getRelative(String baseRoot, String path) {
    baseRoot = normalize(baseRoot);
    path = normalize(path);

    int prefix = findCommonPathPrefixLength(baseRoot, path);

    if (prefix != 0) {
      baseRoot = baseRoot.substring(prefix);
      path = path.substring(prefix);
      if (baseRoot.length() != 0) {
        return normalize(revertRelativePath(baseRoot.substring(1)) + path);
      }
      else if (path.length() != 0) {
        return path.substring(1);
      }
      else {
        return ".";
      }
    }
    else if (isAbsolute(path)) {
      return path;
    }
    else {
      return normalize(revertRelativePath(baseRoot) + "/" + path);
    }
  }

  public static int findCommonPathPrefixLength(String path1, String path2) {
    int end = -1;
    do {
      int beg = end + 1;
      int new_end = endOfToken(path1, beg);
      if (new_end != endOfToken(path2, beg) || !path1.substring(beg, new_end).equals(path2.substring(beg, new_end))) {
        break;
      }
      end = new_end;
    }
    while (end != path1.length());
    return end < 0 ? 0 : end;
  }

  private static int endOfToken(String s, int index) {
    index = s.indexOf("/", index);
    return (index == -1) ? s.length() : index;
  }

  private static String revertRelativePath(String path) {
    if (path.equals(".")) {
      return path;
    }
    else {
      StringBuffer sb = new StringBuffer();
      sb.append("..");
      int count = normalize(path).split("/").length;
      while (--count > 0) {
        sb.append("/..");
      }
      return sb.toString();
    }
  }

}
