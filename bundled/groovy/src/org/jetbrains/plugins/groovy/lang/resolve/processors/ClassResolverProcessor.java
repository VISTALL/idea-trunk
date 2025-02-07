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
package org.jetbrains.plugins.groovy.lang.resolve.processors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement;

import java.util.EnumSet;

/**
 * @author ven
 */
public class ClassResolverProcessor extends ResolverProcessor {
  public ClassResolverProcessor(String refName, GrReferenceElement ref, EnumSet<ResolveKind> kinds) {
    super(refName, kinds, ref, ref.getTypeArguments());
  }

  public ClassResolverProcessor(String refName, PsiElement place) {
    super(refName, EnumSet.of(ClassHint.ResolveKind.CLASS), place, PsiType.EMPTY_ARRAY);
  }
}
