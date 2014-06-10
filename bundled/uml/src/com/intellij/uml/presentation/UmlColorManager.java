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

package com.intellij.uml.presentation;

import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlRelationship;
import static com.intellij.uml.utils.UmlUtils.isEdgeOfType;

import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class UmlColorManager {
  private UmlColorManager() {}
  //TODO save and load colors from FS
  private Color realizationEdgeColor = new Color(0,130,0);
  private final Color interfaceGeneralizationColor = realizationEdgeColor;
  private Color generalizationEdgeColor = new Color(0,0,130);
  private Color innerEdgeColor = new Color(130, 0, 0);
  private Color defaultEdgeColor = Color.GRAY.darker();
  private final Color nodeBGColor = new Color(252, 250, 209);
  private final Color nodeSelectedBGColor = new Color(0x0A, 0x24, 0x6A);
  private final Color nodeFGColor = Color.BLACK;
  private final Color nodeSelectedFGColor = Color.WHITE;
  private final Color caption = new Color(215, 213, 172);
  private final Color packageCaption = new Color(171, 155, 68);
  private final Color subPackageCaption = new Color(171, 230, 68);
  private final Color annotationColor = new Color(153, 153, 0);
  private Color defaultNodeElementColor = Color.BLACK;

  private static final UmlColorManager instance = new UmlColorManager();

  public static UmlColorManager getInstance() {
    return instance;
  }

  public Color getEdgeColor(UmlEdge edge, UmlClassDiagramPresentationModel model) {
    if (model == null || ! model.getPresentation().isColorManagerEnabled()) return defaultEdgeColor;

    if (isEdgeOfType(UmlRelationship.INTERFACE_GENERALIZATION, edge)) return interfaceGeneralizationColor;
    if (isEdgeOfType(UmlRelationship.REALIZATION, edge)) return realizationEdgeColor;
    if (isEdgeOfType(UmlRelationship.GENERALIZATION, edge)) return generalizationEdgeColor;
    if (isEdgeOfType(UmlRelationship.INNER_CLASS, edge)) return innerEdgeColor;
    if (isEdgeOfType(UmlRelationship.ANNOTATION, edge)) return annotationColor;

    else return defaultEdgeColor;
  }

  public Color getRealizationEdgeColor() {
    return realizationEdgeColor;
  }

  public void setRealizationEdgeColor(final Color realizationEdgeColor) {
    this.realizationEdgeColor = realizationEdgeColor;
  }

  public Color getGeneralizationEdgeColor() {
    return generalizationEdgeColor;
  }

  public void setGeneralizationEdgeColor(final Color generalizationEdgeColor) {
    this.generalizationEdgeColor = generalizationEdgeColor;
  }

  public Color getInnerEdgeColor() {
    return innerEdgeColor;
  }

  public void setInnerEdgeColor(final Color innerEdgeColor) {
    this.innerEdgeColor = innerEdgeColor;
  }

  public Color getDefaultEdgeColor() {
    return defaultEdgeColor;
  }

  public void setDefaultEdgeColor(final Color defaultEdgeColor) {
    this.defaultEdgeColor = defaultEdgeColor;
  }

  public Color getNodeBackground(boolean selected) {
    return selected ? nodeSelectedBGColor : nodeBGColor;
  }

  public Color getNodeForeground(boolean selected) {
    return selected ? nodeSelectedFGColor : nodeFGColor;
  }

  public Color getCaptionColor() {
    return caption;
  }

  public Color getPackageCaptionColor() {
    return packageCaption;
  }

  public Color getSubPackageCaptionColor() {
    return subPackageCaption;
  }

  public Color getDefaultNodeElementColor() {
    return defaultNodeElementColor;
  }
}
