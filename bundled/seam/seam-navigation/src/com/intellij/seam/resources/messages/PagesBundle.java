package com.intellij.seam.resources.messages;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * User: Sergey.Vasiliev
 */
public class PagesBundle {
  private static Reference<ResourceBundle> ourBundle;

    @NonNls private static final String PATH_TO_BUNDLE = "resources.messages.PagesBundle";

    private PagesBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = "resources.messages.PagesBundle")String key, Object... params) {
      return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
      ResourceBundle bundle = null;
      if (ourBundle != null) bundle = ourBundle.get();
      if (bundle == null) {
        bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
        ourBundle = new SoftReference<ResourceBundle>(bundle);
      }
      return bundle;
    }
  }


