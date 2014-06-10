package com.advancedtools.webservices.wsengine;

import com.advancedtools.webservices.utils.LibUtils;
import com.intellij.openapi.module.Module;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 30.07.2006
 * Time: 22:40:21
 * To change this template use File | Settings | File Templates.
 */
public class WSEngineUtils {
  public static String stripPrefixPath(String absolutePath, String xFirePath) {
    if (absolutePath.startsWith(xFirePath)) absolutePath = absolutePath.substring(xFirePath.length() + 1);
    return absolutePath;
  }

}
