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

package com.advancedtools.webservices.inspections;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.inspections.fixes.RemoveElementFix;
import static com.advancedtools.webservices.rest.RestAnnotations.*;
import static com.advancedtools.webservices.utils.RestUtils.*;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.*;
import com.intellij.psi.search.searches.DefinitionsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class NoResourceMethodsFoundInspection extends BaseWebServicesInspection {
  protected void checkMember(final ProblemsHolder problemsHolder, final PsiMember psiMember) {
  }

  protected void doCheckClass(final PsiClass c, final ProblemsHolder problemsHolder) {
    if (isRootResourceClass(c)) {
      checkRootResourceClassHasResourceMethods(c, problemsHolder);
      checkConstructors(c, problemsHolder);
    }
  }

  private static void checkRootResourceClassHasResourceMethods(final PsiClass c, final ProblemsHolder problemsHolder) {
    final PsiMethod[] methods = c.getMethods();
    for (PsiMethod method : methods) {
      if (isResourceMethod(method)) return;
    }

    final Query<PsiElement> query = DefinitionsSearch.search(c);
    for (PsiElement element : query) {
      if (element instanceof PsiClass) {
        final PsiClass psiClass = (PsiClass)element;
        for (PsiMethod method : psiClass.getMethods()) {
          if (isResourceMethod(method)) return;
        }
      }
    }

    PsiAnnotation anno = findAnnotation(PATH, c);
    if (anno == null) return;
    problemsHolder
        .registerProblem(anno,
                         WSBundle.message("webservices.inspections.no.resource.methods.found.problem", c.getQualifiedName()),
                         ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                         new RemoveElementFix(anno,
                                              WSBundle.message("webservices.inspections.remove.annotation.fix.name", "@" + PATH_SHORT)));

  }

  private static void checkConstructors(final PsiClass c, final ProblemsHolder problemsHolder) {
    final PsiMethod[] constructors = c.getConstructors();
    if (constructors.length == 0) return; // Class has default constructor
    List<PsiMethod> publicConstructors = new ArrayList<PsiMethod>();
    for (PsiMethod constructor : constructors) {
      if (isPublic(constructor)) publicConstructors.add(constructor);
    }

    if (publicConstructors.size() == 0) {
      final ProblemHighlightType type = ProblemHighlightType.GENERIC_ERROR_OR_WARNING;
      problemsHolder.registerProblem(c.getNameIdentifier(),
                                   WSBundle.message("webservices.inspections.no.public.constructor.found.in.root.resource.class.problem", c.getQualifiedName()),
                                   type);
    }
  }

  public static boolean isPublic(final PsiMethod psiMethod) {
    return psiMethod.hasModifierProperty(PsiModifier.PUBLIC);
  }


  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.no.resource.methods.found.display.name");
  }

  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.no.resource.methods.found.short.name");
  }
}
