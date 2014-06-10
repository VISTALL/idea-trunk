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

package com.intellij.beanValidation.model.xml;

import com.intellij.beanValidation.model.converters.BvConstraintMappingConverter;
import com.intellij.beanValidation.model.converters.PsiClassConverter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public interface ValidationConfig extends BvConfigDomElement {
  @NotNull
  @Convert(PsiClassConverter.class)
  GenericDomValue<PsiClass> getDefaultProvider();

  @NotNull
  @Convert(PsiClassConverter.class)
  GenericDomValue<PsiClass> getMessageInterpolator();

  @NotNull
  @Convert(PsiClassConverter.class)
  GenericDomValue<PsiClass> getTraversableResolver();

  @NotNull
  @Convert(PsiClassConverter.class)
  GenericDomValue<PsiClass> getConstraintValidatorFactory();

  @NotNull
  @Convert(BvConstraintMappingConverter.class)
  List<GenericDomValue<XmlFile>> getConstraintMappings();
}
