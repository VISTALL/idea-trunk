package com.advancedtools.webservices.xfire;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author maxim
 */
public class XFireUtils {
  public static File findXFireJar(String xFirePath) {
    File[] xfireJars = new File(xFirePath).listFiles(new FilenameFilter() {
      public boolean accept(File file, String string) {
        return isXFireJar(string);
      }
    });

    if (xfireJars == null || xfireJars.length == 0) {
      xfireJars = new File(xFirePath,"lib").listFiles(new FilenameFilter() {
      public boolean accept(File file, String string) {
        return isCxfJar(string);
      }
    });
    }
    return xfireJars[0];
  }

  public static boolean isXFireOrCxfHome(VirtualFile t) {
    return isXFireHome(t) || isCxfHome(t);
  }

  public static boolean isCxfHome(VirtualFile t) {
    final VirtualFile child = t.findChild("lib");

    if (child != null && child.isDirectory()) {
      for(VirtualFile f2:child.getChildren()) {
        if (isCxfJar(f2.getName())) return true;
      }
    }

    return false;
  }

  private static boolean isCxfJar(String name) {
    return name.startsWith("cxf-2.") && name.endsWith(".jar");
  }

  public static boolean isXFireHome(VirtualFile t) {
    for(VirtualFile f:t.getChildren()) {
      if (!f.isDirectory()) {
        if (isXFireJar(f.getName())) return true;
      }
    }

    return false;
  }

  private static boolean isXFireJar(String name) {
    return name.startsWith("xfire-all-1.") && name.endsWith(".jar");
  }

  public static boolean isCxf21OrAboveJar(String name) {
    return name.length() > 10
           && name.startsWith("cxf-2.")
           && name.endsWith(".jar")
           && name.charAt(6) > '0';
  }

  public static String getJavaToWsdlClassName(String[] classpath) {
    for (String jar : classpath) {
      if (jar.endsWith(".jar")) {
        final int ind = jar.replace('\\', '/').lastIndexOf('/');
        if (ind > -1 && isCxf21OrAboveJar(jar.substring(ind + 1))) {
          return "org.apache.cxf.tools.java2ws.JavaToWS"; 
        }
      }
    }
    return "org.apache.cxf.tools.java2wsdl.JavaToWSDL";
  }
}
