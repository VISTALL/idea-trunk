/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
package org.jetbrains.plugins.groovy.lang.psi.impl.synthetic;

import com.intellij.openapi.util.Computable;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.LightElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.GroovyFileType;

/**
 * @author ven
 */
public class LightParameterList extends LightElement implements PsiParameterList {
  private final Computable<LightParameter[]> myParametersComputation;
  private LightParameter[] myParameters = null;

  protected LightParameterList(PsiManager manager,
                               Computable<LightParameter[]> parametersComputation) {
    super(manager, GroovyFileType.GROOVY_LANGUAGE);
    myParametersComputation = parametersComputation;
  }

  @NonNls
  public String getText() {
    return null;
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof JavaElementVisitor) {
      ((JavaElementVisitor) visitor).visitParameterList(this);
    }
  }

  public PsiElement copy() {
    return null;
  }

  @NotNull
  public PsiParameter[] getParameters() {
    if (myParameters == null) {
      myParameters = myParametersComputation.compute();
    }

    return myParameters;
  }

  public int getParameterIndex(PsiParameter parameter) {
    final PsiParameter[] parameters = getParameters();
    for (int i = 0; i < parameters.length; i++) {
      if (parameter.equals(parameters[i])) return i;
    }
    return -1;
  }

  public int getParametersCount() {
    return getParameters().length;
  }

  public String toString() {
    return "Light Parameter List";
  }
}
