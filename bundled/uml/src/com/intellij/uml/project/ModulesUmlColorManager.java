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

package com.intellij.uml.project;

import com.intellij.psi.PsiClass;
import com.intellij.uml.UmlColorManagerBase;
import com.intellij.uml.UmlEdge;
import com.intellij.uml.UmlRelationshipInfo;
import com.intellij.uml.presentation.UmlLineType;

import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class ModulesUmlColorManager extends UmlColorManagerBase {
  @Override
  public Color getEdgeColor(UmlEdge edge) {
    return edge == null
        || !isInterface(edge.getSource().getIdentifyingElement())
        || !isInterface(edge.getTarget().getIdentifyingElement())
        || edge.getRelationship().getStartArrow() != UmlRelationshipInfo.DELTA
        || edge.getRelationship().getLineType() != UmlLineType.SOLID ?
    super.getEdgeColor(edge) : REALIZATION; 
  }

  private static boolean isInterface(Object element) {
    return element instanceof PsiClass && ((PsiClass)element).isInterface();
  }
}
