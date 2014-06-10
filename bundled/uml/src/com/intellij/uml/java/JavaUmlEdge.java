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

package com.intellij.uml.java;

import com.intellij.psi.PsiElement;
import com.intellij.uml.PsiUmlEdge;
import com.intellij.uml.UmlNode;
import com.intellij.uml.UmlRelationshipInfo;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class JavaUmlEdge extends PsiUmlEdge<PsiElement> {
  public JavaUmlEdge(@NotNull UmlNode<PsiElement> source, @NotNull UmlNode<PsiElement> target, String name,
                           @NotNull PsiElement identifyingElement,
                           @NotNull UmlRelationshipInfo relationship) {
    super(source, target, name, identifyingElement, relationship);
  }

  public JavaUmlEdge(UmlNode<PsiElement> from, UmlNode<PsiElement> to, @NotNull UmlRelationshipInfo relationship) {
    this(from, to, "", from.getIdentifyingElement(), relationship);
  }

  public JavaUmlEdge(final UmlNode<PsiElement> source,
                                    final UmlNode<PsiElement> target,
                                    final String name,
                                    final @NotNull PsiElement identifyingElement) {
    this(source, target, name, identifyingElement, UmlRelationshipInfo.NO_RELATIONSHIP);
  }
}
