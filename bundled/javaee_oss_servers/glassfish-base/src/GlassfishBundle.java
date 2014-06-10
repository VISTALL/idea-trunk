/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;

public class GlassfishBundle extends JavaeeBundle {

    @NonNls
    private static final String PATH = "resources.glassfish";

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(PATH);

    @SuppressWarnings({"MethodOverridesStaticMethodOfSuperclass"})
    public static String getText(@PropertyKey(resourceBundle = PATH) String key, Object... params) {
        return CommonBundle.message(BUNDLE, key, params);
    }

    @Override
    @NotNull
    protected String getName() {
        return getText("GlassfishIntegration.name");
    }
}
