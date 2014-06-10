/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.model;

import com.intellij.psi.PsiElement;
import com.intellij.openapi.graph.view.LineType;
import com.intellij.openapi.graph.view.Arrow;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPsiElementEdge implements UmlEdge<PsiElement> {
  private UmlNode mySource;
  private UmlNode myTarget;
  private String myName;
  private PsiElement myIdentifyingElement;
  private UmlRelationship myRelationship;

  public UmlPsiElementEdge(@NotNull UmlNode source, @NotNull UmlNode target, String name,
                           @NotNull PsiElement identifyingElement,
                           @NotNull UmlRelationship relationship) {
    mySource = source;
    myTarget = target;
    myName = name;
    myIdentifyingElement = identifyingElement;
    myRelationship = relationship;
  }

  public UmlPsiElementEdge(UmlNode from, UmlNode to, @NotNull UmlRelationship relationship) {
    this(from, to, "", from.getIdentifyingElement(), relationship);
  }

  public UmlPsiElementEdge(final UmlNode source,
                                    final UmlNode target,
                                    final String name,
                                    final @NotNull PsiElement identifyingElement) {
    this(source, target, name, identifyingElement, NO_RELATIONSHIP);
  }

  public UmlNode getSource() {
    return mySource;
  }

  public UmlNode getTarget() {
    return myTarget;
  }

  public String getName() {
    return myName;
  }

  @NotNull
  public PsiElement getIdentifyingElement() {
    return myIdentifyingElement;
  }

  public LineType getLineType() {
    return myRelationship.getLineType();
  }

  public Arrow getArrow() {
    return myRelationship.getArrow();
  }

  public void setRelationship(UmlRelationship relationship) {
    myRelationship = relationship;
  }

  @NotNull
  public UmlRelationship getRelationship() {
    return myRelationship;
  }

  public String getLabel() {
    return myRelationship.getLabel();
  }

  public Arrow getSourceArrow() {
    return (myRelationship instanceof UmlTwoForkedRelationship) ?
      ((UmlTwoForkedRelationship)myRelationship).getSourceArrow() : Arrow.NONE;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UmlPsiElementEdge that = (UmlPsiElementEdge)o;

    if (!myIdentifyingElement.equals(that.myIdentifyingElement)) return false;
    if (!myName.equals(that.myName)) return false;
    if (!myRelationship.equals(that.myRelationship)) return false;
    if (!mySource.equals(that.mySource)) return false;
    if (!myTarget.equals(that.myTarget)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mySource.hashCode();
    result = 31 * result + myTarget.hashCode();
    result = 31 * result + myName.hashCode();
    result = 31 * result + myIdentifyingElement.hashCode();
    result = 31 * result + myRelationship.hashCode();
    return result;
  }
}
