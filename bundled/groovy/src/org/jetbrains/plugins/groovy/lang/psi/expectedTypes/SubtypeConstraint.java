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
package org.jetbrains.plugins.groovy.lang.psi.expectedTypes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiClassType;
import static org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil.createType;

/**
 * @author ven
 */
public  class SubtypeConstraint extends TypeConstraint {
  private final PsiType myDefaultType;

  protected SubtypeConstraint(PsiType type, PsiType defaultType) {
    super(type);
    myDefaultType = defaultType;
  }

  public boolean satisfied(PsiType type){
    return myType.isAssignableFrom(type);
  }

  public PsiType getDefaultType() {
    return myDefaultType;
  }

  public static SubtypeConstraint create (PsiType type, PsiType defaultType) {
    return new SubtypeConstraint(type, defaultType);
  }

  public static SubtypeConstraint create (String fqName, String defaultFqName, PsiElement context) {
    return new SubtypeConstraint(createType(fqName, context),
                                 createType(defaultFqName, context));
  }

  public static SubtypeConstraint create (PsiType type) {
    return new SubtypeConstraint(type, type);
  }

  public static SubtypeConstraint create (String fqName, PsiElement context) {
    PsiClassType type = createType(fqName, context);
    return new SubtypeConstraint(type, type);
  }
}
