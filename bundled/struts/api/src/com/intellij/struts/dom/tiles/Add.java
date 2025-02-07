/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

// Generated on Wed Apr 05 15:29:47 MSD 2006
// DTD/Schema  :    tiles-config_1_3.dtd

package com.intellij.struts.dom.tiles;

import com.intellij.struts.dom.StrutsRootElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import com.intellij.util.xml.Attribute;
import org.jetbrains.annotations.NotNull;

/**
 * tiles-config_1_3.dtd:add interface.
 * Type add documentation
 * <pre>
 *  The "add" element describes an element of a list. It is similar to the
 *      <put> element.
 *      content         Same as value. For compatibility with the template tag library.
 *      direct          Same as type="string". For compatibility with the template
 *                      tag library.
 *      type            The type of the value. Can be: string, page, template or definition.
 *                      By default, no type is associated to a value. If a type is
 *                      associated, it will be used as a hint to process the value
 *                      when the attribute will be used in the inserted tiles.
 *      value           The value associated to this tiles attribute. The value should
 *                      be specified with this tag attribute, or in the body of the tag.
 * </pre>
 */
public interface Add extends StrutsRootElement {

  /**
   * Returns the value of the simple content.
   *
   * @return the value of the simple content.
   */
  @NotNull
  String getValue();

  /**
   * Sets the value of the simple content.
   *
   * @param value the new value to set
   */
  void setValue1(@NotNull String value);


  /**
   * Returns the value of the type child.
   * Attribute type
   *
   * @return the value of the type child.
   */
  @NotNull
  GenericAttributeValue<Content> getType();


  /**
   * Returns the value of the content child.
   * Attribute content
   *
   * @return the value of the content child.
   */
  @NotNull
  GenericAttributeValue<String> getContent();


  /**
   * Returns the value of the value child.
   * Attribute value
   *
   * @return the value of the value child.
   */
  @Attribute("value")
  @NotNull
  GenericAttributeValue<String> getAttributeValue();


  /**
   * Returns the value of the direct child.
   * Attribute direct
   *
   * @return the value of the direct child.
   */
  @NotNull
  GenericAttributeValue<Boolean> getDirect();


}
