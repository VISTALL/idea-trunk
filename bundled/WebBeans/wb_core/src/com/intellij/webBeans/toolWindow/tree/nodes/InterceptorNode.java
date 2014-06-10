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

package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.openapi.module.Module;
import com.intellij.webBeans.resources.WebBeansBundle;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import com.intellij.psi.PsiClass;

import java.util.Collection;

public class InterceptorNode extends AbstractWebBeansTypeNode {

  public InterceptorNode(Module module, WebBeansModuleNode webBeansModuleNode) {
    super(webBeansModuleNode, module, WebBeansNodeTypes.INTERCEPTOR, WebBeansBundle.message("actions.show.interceptors"));

    setUniformIcon(WebBeansIcons.INTERCEPTOR_TYPES);
  }

  public SimpleNode[] getChildren() {
    Collection<PsiClass> interceptorBindingTypesClasses = WebBeansCommonUtils.getInterceptorBindingTypesClasses(getModule());

    Collection<PsiMemberSimpleNode> nodes = createSortedList();

    for (PsiClass psiClass : interceptorBindingTypesClasses) {
      nodes.add(new AnnotationTypeNode(getModule(), psiClass, this));
    }
    return nodes.toArray(new SimpleNode[nodes.size()]);
  }
}