package com.intellij.eclipse.export;

import com.intellij.eclipse.export.model.IdeaModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public class ResourceUtil {
  public static String transormToCanonicalPathString(String path) {
    path = replace(path, "\\", "/");
    path = replace(path, "//", "/");
    path = replace(path, "//", "/");
    return path;
  }

  public static String getFullPath(IPath path, IProject project) {
    IPath pathRes;

    if (path.getDevice() != null) pathRes = path;
    else {
      pathRes = project.getLocation();
      for (int i = 1; i < path.segmentCount(); i++) {
        String segment = path.segment(i);
        pathRes = pathRes.append(segment);
      }
    }

    return pathRes.toString();
  }

  static public String formatFileURL(String path) {
    if (path.endsWith("." + IdeaProjectFileConstants.JAR_EXT) ||
        path.endsWith("." + IdeaProjectFileConstants.ZIP_EXT)) {
      return IdeaProjectFileConstants.JAR_FULL_URL_PREFIX + path
             + IdeaProjectFileConstants.JAR_POSTFIX;
    }

    return IdeaProjectFileConstants.FILE_FULL_URL_PREFIX + path;
  }

  public static String insertModuleDirPrefix(IdeaModule ideaModule, String folder) {
    Object element = ideaModule.getEclipseProject();
    if (element instanceof IProject) {
      IProject project = (IProject)element;
      String moduleDir = ResourceUtil.getFullPath(project.getLocation(), project);
      folder = removePathPrefix(folder, moduleDir);
      folder = IdeaProjectFileConstants.MODULE_DIR_URL_VAR + "/" + folder;
    }
    return folder;
  }

  public static String removePathPrefix(String folder, String parentFolder) {
    if (folder.startsWith(parentFolder))
      folder = folder.substring(parentFolder.length());
    if (folder.startsWith("/"))
      folder = folder.substring(1);
    return folder;
  }

  private static String replace(String str, String pattern, String newSubstring) {
    StringBuffer sb = new StringBuffer();
    int l = str.length();
    int pos = 0;
    int lastPos = 0;
    while (pos < l) {
      pos = str.indexOf(pattern, pos);
      if (pos < 0) {
        pos = l;
      } else {
        sb.append(newSubstring);
      }
      sb.append(str.substring(lastPos, pos));
      pos += pattern.length();
      lastPos = pos;
    }
    return sb.toString();
  }
}