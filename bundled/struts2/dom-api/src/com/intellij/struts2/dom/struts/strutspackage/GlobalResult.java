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

import com.intellij.openapi.paths.PathReference;
import com.intellij.struts2.dom.params.ParamsElement;
import com.intellij.struts2.dom.struts.HasResultType;
import com.intellij.struts2.dom.struts.action.ResultTypeResolvingConverter;
import com.intellij.struts2.dom.struts.action.StrutsPathReferenceConverter;
import com.intellij.util.xml.*;

/**
 * Global <code>result</code>.
 *
 * @author Yann C&eacute;bron
 */
@Convert(StrutsPathReferenceConverter.class)
public interface GlobalResult extends HasResultType, ParamsElement, GenericDomValue<PathReference> {

  @NameValue
  @Scope(ParentScopeProvider.class)
  GenericAttributeValue<String> getName();

  @Convert(ResultTypeResolvingConverter.class)
  GenericAttributeValue<ResultType> getType();

}