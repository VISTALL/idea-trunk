package com.advancedtools.webservices.axis2;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: Jul 28, 2006
 * Time: 10:00:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class Axis2Utils {
  public static boolean isAxis2JarFile(String fileName) {
    return fileName.startsWith("axiom-api-") && fileName.endsWith(".jar");
  }
}
