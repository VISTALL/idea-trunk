/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PropertyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class JBossPropertyDescriptor {

    private final String name;

    @Nullable
    private final SmartPsiElementPointer<PsiMethod> getter;

    @Nullable
    private final SmartPsiElementPointer<PsiMethod> setter;

    JBossPropertyDescriptor(@NotNull String name, @NotNull PsiClass psi) {
        this.name = name;
        getter = createPointer(PropertyUtil.findPropertyGetter(psi, name, false, false));
        setter = createPointer(PropertyUtil.findPropertySetter(psi, name, false, false));
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    PsiMethod getGetter() {
        return getPsiMethod(getter);
    }

    @Nullable
    PsiMethod getSetter() {
        return getPsiMethod(setter);
    }

    @Nullable
    private SmartPsiElementPointer<PsiMethod> createPointer(@Nullable PsiMethod method) {
        if (method != null) {
            return SmartPointerManager.getInstance(method.getProject()).createSmartPsiElementPointer(method);
        }
        return null;
    }

    @Nullable
    private static PsiMethod getPsiMethod(SmartPsiElementPointer<PsiMethod> pointer) {
        return (pointer != null) ? pointer.getElement() : null;
    }
}
