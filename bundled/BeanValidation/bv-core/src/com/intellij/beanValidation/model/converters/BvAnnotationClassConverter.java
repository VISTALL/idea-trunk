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

package com.intellij.beanValidation.model.converters;

import com.intellij.beanValidation.model.BvModelUtils;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Konstantin Bulenkov
 */
public class BvAnnotationClassConverter extends ResolvingConverter<PsiClass> {
  @NotNull
  @Override
  public Collection<? extends PsiClass> getVariants(ConvertContext context) {
    return Collections.emptyList();
  }

  @Override
  public PsiClass fromString(@Nullable @NonNls String s, ConvertContext context) {
    return BvModelUtils.findClass(s, context, false);
  }

  @Override
  public String toString(@Nullable PsiClass psiClass, ConvertContext context) {
    return BvModelUtils.getQualifiedName(psiClass, context, false);
  }
}
