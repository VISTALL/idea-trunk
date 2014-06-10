package com.intellij.seam.constants;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class SeamAnnotationsUtil {
  private SeamAnnotationsUtil() {
  }

  public static List<String> getAllSeamAnnotations() {
      return getAnnotations(SeamAnnotationConstants.class);
  }

  public static List<String> getAnnotations(Class clazz) {
    List<String> annotations = new ArrayList<String>();
    try {
      for (Field field : clazz.getFields()) {
        final Object value = field.get(null);
        if (value instanceof String) {
          annotations.add((String)value);
        }
      }
    } catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
    return annotations;
  }
}
