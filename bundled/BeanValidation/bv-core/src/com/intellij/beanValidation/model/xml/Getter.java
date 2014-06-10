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

// Generated on Fri Sep 04 16:11:26 MSD 2009
// DTD/Schema  :    http://jboss.org/xml/ns/javax/validation/mapping

package com.intellij.beanValidation.model.xml;

import com.intellij.psi.PsiMember;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import com.intellij.beanValidation.model.converters.BvGetterNameConverter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Konstantin Bulenkov
 * @generated from http://jboss.org/xml/ns/javax/validation/mapping:getterType interface.
 */
public interface Getter extends BvMappingsDomElement {
  /**
   * Returns the value of the name child.
   *
   * @return the value of the name child.
   */
  @NotNull
  @Required
  @Convert(BvGetterNameConverter.class)
  GenericAttributeValue<PsiMember> getName();

  /**
   * Returns the value of the ignore-annotations child.
   *
   * @return the value of the ignore-annotations child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getIgnoreAnnotations();

  /**
   * Returns the list of constraint children.
   *
   * @return the list of constraint children.
   */
  @NotNull
  List<Constraint> getConstraints();

  /**
   * Adds new child to the list of constraint children.
   *
   * @return created child
   */
  Constraint addConstraint();
}
