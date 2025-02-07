package com.intellij.coldFusion;

import com.intellij.CommonBundle;
import com.intellij.reference.SoftReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.util.ResourceBundle;

/**
 * Created by Lera Nikolaenko
 * Date: 17.11.2008
 */
public class CfmlBundle {
    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    protected static final String PATH_TO_BUNDLE = "messages.CfmlBundle";

    private CfmlBundle() {
    }

    public static String message(@PropertyKey(resourceBundle = PATH_TO_BUNDLE)String key, Object... params) {
        return CommonBundle.message(getBundle(), key, params);
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = null;
        if (ourBundle != null) {
            bundle = ourBundle.get();
        }
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
            ourBundle = new SoftReference<ResourceBundle>(bundle);
        }
        return bundle;
    }
}
