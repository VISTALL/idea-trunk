/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.spring.model.converters;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.MutableLookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlToken;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.impl.GenericDomValueReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * @author Dmitry Avdeev
 */
public class SpringCompletionContributor extends CompletionContributor {

  @Override
  public void fillCompletionVariants(final CompletionParameters parameters, final CompletionResultSet result) {

    if (parameters.getCompletionType() != CompletionType.SMART) {
      return;
    }
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        final GenericDomValueReference reference = getReference(parameters);
        if (reference == null) {
          return;
        }

        Collection<SpringBeanPointer> variants =
          ((SpringBeanResolveConverter)reference.getConverter()).getSmartVariants(reference.getConvertContext());
        for (SpringBeanPointer variant : variants) {
          MutableLookupElement<SpringBeanPointer> element = SpringBeanResolveConverter.createCompletionVariant(variant);
          if (element != null) {
            result.addElement(element);
          }
        }
      }
    });
  }

  @Nullable
  private GenericDomValueReference getReference(CompletionParameters parameters) {

    if (!(parameters.getPosition() instanceof XmlToken)) return null;

    PsiReference[] references = parameters.getPosition().getParent().getReferences();
    for (final PsiReference psiReference : references) {
      if (psiReference instanceof GenericDomValueReference) {
        final Converter converter = ((GenericDomValueReference)psiReference).getConverter();
        if (converter instanceof SpringBeanResolveConverter) {
          return (GenericDomValueReference)psiReference;
        }
      }
    }
    return null;
  }

  @Override
  public String advertise(@NotNull CompletionParameters parameters) {
    if (parameters.getCompletionType() == CompletionType.BASIC && getReference(parameters) != null) {
        final String shortcut = getActionShortcut(IdeActions.ACTION_SMART_TYPE_COMPLETION);
        if (shortcut != null) {
          return CompletionBundle.message("completion.smart.hint", shortcut);
        }
    }
    return null;
  }
}
