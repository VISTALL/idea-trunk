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

package org.jetbrains.plugins.grails.projectView.v2.nodes.leafs;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;
import org.jetbrains.plugins.groovy.mvc.projectView.MethodNode;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;

/**
 * User: Dmitry.Krasilschikov
 * Date: 15.04.2009
 */
public class TestMethodNode extends MethodNode {
  public TestMethodNode(@NotNull final Module module,
                        @NotNull final GrMethod method,
                        @Nullable final String locationMark,
                        @Nullable final ViewSettings viewSettings) {
    super(module, method, locationMark, viewSettings);
  }

  @Override
  protected GrMethod extractPsiFromValue() {
    return (GrMethod)super.extractPsiFromValue();
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "Test method: " + ((GrField)psiElement).getName();
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    super.updateImpl(data);

    //final GrMethod method = extractPsiFromValue();
    //data.setIcons(RContainerPresentationUtil.getIconWithModifiers(method, RailsIcons.RAILS_ACTION_NODE));
    data.setIcons(GrailsIcons.GRAILS_TEST_METHOD_NODE);
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.TEST_METHOD;
  }

  //@Override
  //public boolean validate() {
  //  if (!super.validate()) {
  //    return false;
  //  }
  //  final GrMethod field = extractPsiFromValue();
  //  assert field != null;
  //  if (GrailsAction.fromField(field) == null) {
  //    setValue(null);
  //  }
  //  return getValue() != null;
  //}
}
