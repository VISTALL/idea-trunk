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

import com.intellij.beanValidation.model.converters.BvBeanClassConverter;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Konstantin Bulenkov
 * @generated from http://jboss.org/xml/ns/javax/validation/mapping:beanType interface.
 */
public interface Bean extends BvMappingsDomElement {

  /**
   * Returns the value of the class child.
   *
   * @return the value of the class child.
   */
  @NotNull
  @com.intellij.util.xml.Attribute("class")
  @Required
  @Convert(BvBeanClassConverter.class)
  GenericAttributeValue<PsiClass> getClassAttr();

  /**
   * Returns the value of the ignore-annotations child.
   *
   * @return the value of the ignore-annotations child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getIgnoreAnnotations();

  /**
   * Returns the value of the class child.
   *
   * @return the value of the class child.
   */
  @NotNull
  @com.intellij.util.xml.SubTag("class")
  Class getClazz();

  /**
   * Returns the list of field children.
   *
   * @return the list of field children.
   */
  @NotNull
  List<Field> getFields();

  /**
   * Adds new child to the list of field children.
   *
   * @return created child
   */
  Field addField();


  /**
   * Returns the list of getter children.
   *
   * @return the list of getter children.
   */
  @NotNull
  List<Getter> getGetters();

  /**
   * Adds new child to the list of getter children.
   *
   * @return created child
   */
  Getter addGetter();
}
