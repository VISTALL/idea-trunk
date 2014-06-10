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

package com.intellij.spring.metadata;

import com.intellij.javaee.model.common.JamSupportMetaData;
import com.intellij.javaee.util.AnnotationTextUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.spring.model.jam.qualifiers.SpringJamQualifier;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class SpringStereotypeQualifierMetaData extends JamSupportMetaData<SpringJamQualifier> {

  public void setName(final String name) throws IncorrectOperationException {
    PsiAnnotation annotation = getElement().getAnnotation();
    AnnotationTextUtil.setAnnotationParameter(annotation, getParameterName(), AnnotationTextUtil.quote(name), true);
  }

  @NonNls
  protected String getParameterName() {
    return "value";
  }

  public void init(final PsiElement element) {
    PsiModifierListOwner owner = PsiTreeUtil.getParentOfType(element, PsiModifierListOwner.class);
    if (owner != null) {
      SpringJamQualifier component = getModelElement(owner, (PsiAnnotation)element);
      if (component != null) {
        setElement(component);
      }
    }
  }

  @NotNull
  protected SpringJamQualifier getModelElement(final PsiModifierListOwner owner, final PsiAnnotation annotation) {
    return new SpringJamQualifier(annotation, owner, null);
  }
}
