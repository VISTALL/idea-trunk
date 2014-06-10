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

package com.intellij.webBeans.jam.decorators;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class WebBeansDecorator implements JamElement {

  public static final JamClassMeta<WebBeansDecorator> META = new JamClassMeta<WebBeansDecorator>(WebBeansDecorator.class);

  private final JamAnnotationMeta myMeta = new JamAnnotationMeta(WebBeansAnnoConstants.DECORATOR_ANNOTATION);

  @Nullable
  public PsiClass getDecoratesType() {
    return null;
  }


  @NotNull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  @Nullable
  public PsiAnnotation getAnnotation() {
    return myMeta.getAnnotation(getPsiElement());
  }

}