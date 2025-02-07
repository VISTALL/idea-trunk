/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.ruby.codeInsight.completion;

import com.intellij.codeInsight.lookup.LookupValueWithPriority;
import com.intellij.codeInsight.lookup.LookupValueWithPsiElement;
import com.intellij.codeInsight.lookup.LookupValueWithUIHint;
import com.intellij.codeInsight.lookup.PresentableLookupValue;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.ui.RowIcon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.presentation.JavaClassPackagePresentationUtil;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 13, 2007
 */
public class JavaLookupItem implements RubyLookupItem,
        PresentableLookupValue,
        LookupValueWithPsiElement,
        LookupValueWithUIHint,
        LookupValueWithPriority,
        Iconable {

    private String myLookupString;
    private PsiElement myElement;

    @NotNull
    public String getName() {
        return myLookupString;
    }

    public JavaLookupItem(@NotNull final String name, @NotNull final PsiElement element) {
        myLookupString = name;
        myElement = element;
    }

    public JavaLookupItem(@NotNull final PsiElement element) {
        if (element instanceof PsiNamedElement) {
            myLookupString = ((PsiNamedElement) element).getName();
            if (myLookupString == null){
                myLookupString = "";
            }
        }
        myElement = element;
    }

    @NotNull
    public String getPresentation() {
        return myLookupString;
    }

    public PsiElement getElement() {
        return myElement;
    }

    public Icon getIcon(int flags) {
        final RowIcon icon = new RowIcon(2);
        icon.setIcon(JavaClassPackagePresentationUtil.getJavaIcon(), 0);
        icon.setIcon(myElement.getIcon(flags), 1);
        return icon;
    }

    public String getTypeHint() {
        return "";
    }

    public Color getColorHint() {
        return null;
    }

    public boolean isBold() {
        return true;
    }

    public int getPriority() {
        return HIGH;
    }
}
