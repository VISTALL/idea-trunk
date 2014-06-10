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
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

/**
 * @by Konstantin Bulenkov
 */
public class UnreachableResourceMethodInspection extends BaseWebServicesInspection {
  protected void checkMember(final ProblemsHolder problemsHolder, final PsiMember psiMember) {
    if (!(psiMember instanceof PsiMethod)) return;
    PsiMethod method = (PsiMethod)psiMember;
    PsiClass c = method.getContainingClass();
    if (c == null) return;
    if (!isResourceMethod(method)
        || isResourceClass(c)
        || !isNonAbstractAndPublicClass(c)
        || findInheritorClassAnnotatedAs(PATH, c) != null) return;

    //Now let's check that method is not annotated as @Path, @GET, etc
    checkAnnotationPresence(PATH, PATH_SHORT, method, problemsHolder);
    checkAnnotationPresence(GET, GET_SHORT, method, problemsHolder);
    checkAnnotationPresence(POST, POST_SHORT, method, problemsHolder);
    checkAnnotationPresence(PUT, PUT_SHORT, method, problemsHolder);
    checkAnnotationPresence(DELETE, DELETE_SHORT, method, problemsHolder);
    checkAnnotationPresence(HEAD, HEAD_SHORT, method, problemsHolder);
  }  

  private static void checkAnnotationPresence(@NotNull String annotationFQN, @NotNull String annotationShortName, @NotNull PsiMethod method, ProblemsHolder problemsHolder) {
    PsiAnnotation annotation = findAnnotation(annotationFQN, method);
    if (annotation != null) {
      String shortName = "@" + annotationShortName;
      problemsHolder.registerProblem(annotation,
                                     WSBundle.message("webservices.inspections.rest.resource.method.inspection.problem", shortName),
                                     ProblemHighlightType.ERROR,
                                     new RemoveElementFix(annotation, WSBundle.message("webservices.inspections.remove.annotation.fix.name", shortName))
                                     );
    }
  }

  protected void doCheckClass(final PsiClass c, final ProblemsHolder problemsHolder) {
  }

  @NotNull
  public String getDisplayName() {
    return WSBundle.message("webservices.inspections.rest.resource.method.inspection.display.name");
  }

  @NotNull
  public String getShortName() {
    return WSBundle.message("webservices.inspections.rest.resource.method.inspection.short.name");
  }
}
