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

package org.jetbrains.plugins.groovy.annotator.inspections;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspection;
import org.jetbrains.plugins.groovy.codeInspection.BaseInspectionVisitor;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.annotation.GrAnnotation;
import org.jetbrains.plugins.groovy.lang.psi.api.auxiliary.modifiers.GrModifierList;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrConstructor;

/**
 * User: Dmitry.Krasilschikov
 * Date: 29.04.2009
 */
public class GroovySingletonAnnotationInspection extends BaseInspection {
  public static final String SINGLETON = "groovy.lang.Singleton";

  protected BaseInspectionVisitor buildVisitor() {
    return new Visitor();
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @Nls
  @NotNull
  public String getGroupDisplayName() {
    return ANNOTATIONS_ISSUES;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return "Check '@Singleton' annotation conventions";
  }

  private static class Visitor extends BaseInspectionVisitor {
    public void visitAnnotation(GrAnnotation annotation) {
      super.visitAnnotation(annotation);

      PsiElement parent = annotation.getParent().getParent();
      if (parent == null || !(parent instanceof GrTypeDefinition)) return;

      if (SINGLETON.equals(annotation.getQualifiedName())) {
        GrTypeDefinition typeDefinition = (GrTypeDefinition)parent;

        PsiMethod[] methods = typeDefinition.getMethods();
        for (PsiMethod method : methods) {
          if (method.isConstructor()) {
            assert method instanceof GrConstructor;

            GrModifierList modifierList = ((GrConstructor)method).getModifierList();

            if (modifierList.hasModifierProperty(PsiModifier.PUBLIC)) {
              registerClassError(typeDefinition);
            }
          }
        }
      }
    }
  }

  @Override
  protected String buildErrorString(Object... args) {
    return GroovyBundle.message("singleton.class.should.have.private.constructor");
  }
}
