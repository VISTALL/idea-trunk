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

package org.jetbrains.plugins.grails.lang.gsp.psi.html.impl;

import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.api.GspHtmlOuterElement;

/**
 * @author ilyas
 */
public class GspHtmlOuterElementImpl extends LeafPsiElement implements GspHtmlOuterElement {

  public GspHtmlOuterElementImpl(IElementType type, CharSequence text) {
    super(type, text);
  }

  public String toString() {
    return "Outer: " + getElementType();
  }

}
