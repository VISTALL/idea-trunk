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

package com.intellij.struts;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ScopeProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SecurityRoleScopeProvider extends ScopeProvider {

  /**
   * Returns the {@link com.intellij.javaee.model.xml.web.WebApp} for the given DomElement.
   *
   * @param element DomElement.
   * @return null if DomElement does not belong to WebModule.
   */
  @Nullable
  public DomElement getScope(@NotNull DomElement element) {
    WebFacet webFacet = WebUtil.getWebFacet(element.getXmlTag());
    if (webFacet != null) {
      return webFacet.getRoot();
    }
    return null;
  }

}