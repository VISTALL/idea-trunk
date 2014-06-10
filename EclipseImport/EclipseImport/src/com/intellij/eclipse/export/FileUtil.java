package com.intellij.eclipse.export;

import java.io.*;
import java.nio.channels.FileChannel;

public class FileUtil {
  public static String getRelativePath(File base, File file) {
    if (base == null || file == null) return null;

    if (!base.isDirectory()) {
      base = base.getParentFile();
      if (base == null) return null;
    }

    if (base.equals(file)) return ".";

    String basePath = base.getAbsolutePath();
    if (!basePath.endsWith(File.separator)) basePath += File.separatorChar;
    final String filePath = file.getAbsolutePath();

    int len = 0;
    int lastSeparatorIndex =
      0; // need this for cases like this: base="/temp/abcde/base" and file="/temp/ab"
    while (len < filePath.length() && len < basePath.length() &&
           filePath.charAt(len) == basePath.charAt(len)) {
      if (basePath.charAt(len) == File.separatorChar) {
        lastSeparatorIndex = len;
      }
      len++;
    }

    if (len == 0) return null;

    StringBuffer relativePath = new StringBuffer();
    for (int i = len; i < basePath.length(); i++) {
      if (basePath.charAt(i) == File.separatorChar) {
        relativePath.append("..");
        relativePath.append(File.separatorChar);
      }
    }
    relativePath.append(filePath.substring(lastSeparatorIndex + 1));

    return relativePath.toString();
  }

  public static void copy(File fromFile, File toFile) throws IOException {
    FileInputStream fis = new FileInputStream(fromFile);
    FileOutputStream fos;
    try {
      fos = new FileOutputStream(toFile);
    }
    catch (FileNotFoundException e) {
      File parentFile = toFile.getParentFile();
      if (parentFile == null) {
        return; // TODO: diagnostics here
      }
      parentFile.mkdirs();
      toFile.createNewFile();
      fos = new FileOutputStream(toFile);
    }

    FileChannel fromChannel = fis.getChannel();
    FileChannel toChannel = fos.getChannel();

    try {
      fromChannel.transferTo(0, Long.MAX_VALUE, toChannel);
    }
    finally {
      fromChannel.close();
      toChannel.close();
    }
  }
}
