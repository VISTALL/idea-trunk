/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

/**
 * @author Alexey Chmutov
 */
public class VtlLanguage extends Language implements TemplateLanguage, InjectableLanguage {
    public static final VtlLanguage INSTANCE = new VtlLanguage();

    private VtlLanguage() {
        super("VTL");
    }
}
