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

package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.execution.PsiLocation;
import com.intellij.execution.junit.JUnitUtil;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.projectView.v2.nodes.leafs.TestMethodNode;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;

/**
 * User: Dmitry.Krasilschikov
 * Date: 15.04.2009
 */
public class TestClassNode extends ClassNode {
  public TestClassNode(@NotNull final Module module,
                       @NotNull final GrTypeDefinition controllerClass,
                       @Nullable final ViewSettings viewSettings) {
    super(module, controllerClass, NodeId.TEST_CLASS_IN_TESTS_SUBTREE, viewSettings);
  }

  @Nullable
  @Override
  protected MethodNode createNodeForMethod(final Module module, final GrMethod method, final String parentLocationRootMark) {
    if (method == null) return null;

    final boolean isTestMethod = JUnitUtil.isTestMethod(new PsiLocation<PsiMethod>(getProject(), method));

    if (isTestMethod) {
      return new TestMethodNode(module, method, NodeId.TEST_CLASS_IN_TESTS_SUBTREE, getSettings());
    }

    return new MethodNode(module, method, NodeId.TEST_CLASS_IN_TESTS_SUBTREE, getSettings());
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "Test class: " + ((GrTypeDefinition)psiElement).getName();
  }                                                                                                                                                

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.TEST_METHOD;
  }
}
