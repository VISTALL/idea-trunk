/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom.struts.strutspackage;

import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.util.xml.*;

/**
 * <code>default-class-ref</code>
 *
 * @author Yann C&eacute;bron
 */
public interface DefaultClassRef extends DomElement {

  @Required
  @Attribute("class")
  @ExtendClass(allowAbstract = false, allowInterface = false)
  @Convert(ExtendableClassConverter.class)
  GenericAttributeValue<PsiClass> getDefaultClass();

}