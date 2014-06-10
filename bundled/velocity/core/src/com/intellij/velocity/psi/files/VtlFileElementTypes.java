/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.files;

import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.velocity.psi.VtlElementTypes;
import com.intellij.velocity.psi.VtlLanguage;

/**
 * @author Alexey Chmutov
 */
public class VtlFileElementTypes {
    private static final IElementType OUTER_ELEMENT_TYPE = new IElementType("VTL_FRAGMENT", VtlLanguage.INSTANCE);
    public static final TemplateDataElementType TEMPLATE_DATA =
            new TemplateDataElementType("VTL_TEMPLATE_DATA", VtlLanguage.INSTANCE, VtlElementTypes.TEMPLATE_TEXT, OUTER_ELEMENT_TYPE);

    private VtlFileElementTypes() {
    }
}
