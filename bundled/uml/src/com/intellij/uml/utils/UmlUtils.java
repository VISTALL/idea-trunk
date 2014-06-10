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

package com.intellij.uml.utils;

import com.intellij.openapi.graph.base.Node;
import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.actions.layout.AbstractLayoutAction;
import com.intellij.openapi.graph.layout.NodeLayout;
import com.intellij.openapi.graph.settings.GraphSettings;
import com.intellij.openapi.graph.settings.GraphSettingsProvider;
import com.intellij.openapi.graph.view.*;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.uml.model.*;
import com.intellij.uml.presentation.UmlClassDiagramPresentationModel;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.util.ui.UIUtil;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Konstantin Bulenkov
 */
public class UmlUtils {
  private UmlUtils() {}

  @Nullable
  public static String getPackageName(PsiElement element) {
    if (element instanceof PsiClass) {
      if (isInnerClass(element)) {
        element = ((PsiClass)element).getContainingClass();
        if (element == null) return null;
      }
      String fqn = ((PsiClass)element).getQualifiedName();
      if (fqn == null) return null;
      int ind = fqn.lastIndexOf('.');
      if (ind > 0) {
        fqn = fqn.substring(0, ind);
      }
      return fqn;
    } else if (element instanceof PsiPackage) {
      return ((PsiPackage)element).getQualifiedName();
    }
    return null;
  }

  public static boolean isInnerClass(PsiElement psiClass) {
    return psiClass instanceof PsiClass && ((PsiClass)psiClass).getContainingClass() != null;
  }

  @Nullable
  public static String getRealPackageName(PsiElement psiElement) {
    if (psiElement instanceof PsiClass) {
      PsiClass aClass = (PsiClass)psiElement;
      while (aClass.getContainingClass() != null) {
        PsiClass c = aClass.getContainingClass();
        if (c == null) {
          return getPackageName(c);
        } else {
          aClass = c;
        }
      }
      return getPackageName(aClass);
    }
    return getPackageName(psiElement);
  }

  @Nullable
  public static PsiPackage getPackage(PsiElement element) {
    String fqn = getRealPackageName(element);
    return fqn == null ? null : JavaPsiFacade.getInstance(element.getProject()).findPackage(fqn);    
  }

  public static boolean isDeprecated(PsiElement element) {
    return element instanceof PsiDocCommentOwner && ((PsiDocCommentOwner)element).isDeprecated();
  }

  public static boolean isEqual(final String s1, final String s2) {
    return s1 == null && s2 == null || s1 != null && s1.equals(s2);
  }

  public static void updateGraph(final GraphBuilder<UmlNode, UmlEdge> myBuilder) {
    updateGraph(myBuilder, true, false);
  }

  public static void updateGraph(final GraphBuilder<UmlNode, UmlEdge> myBuilder, final boolean increaseModTrackerCounter, final boolean updateLayout) {
    if (increaseModTrackerCounter) {
      getPresentationModel(myBuilder).update();
    }


    UIUtil.invokeLaterIfNeeded(new Runnable() {
      public void run() {
        myBuilder.updateGraph();

        if (updateLayout) {
          GraphSettings graphSettings = GraphSettingsProvider.getInstance(myBuilder.getProject()).getSettings(myBuilder.getGraph());
          AbstractLayoutAction.doLayout(myBuilder.getView(), graphSettings.getCurrentLayouter(), myBuilder.getProject());
        }
      }
    });
  }

  public static UmlRelationship createUmlRelationships(final LineType lineType, final Arrow arrow) {
    return new UmlRelationship() {
      public LineType getLineType() {return lineType;}
      public Arrow getArrow() {return arrow;}
      public int from() {return 1;}
      public int to() {return 1;}
      public String getLabel() {return "";}
    };
  }

  @Nullable
  public static PsiClass getPsiClass(@Nullable PsiElement file) {
    if (file instanceof PsiClass) return (PsiClass)file;
    //TODO: try to find class with the same name as the java file name
    return (file instanceof PsiJavaFile && ((PsiJavaFile)file).getClasses().length > 0) ?
    ((PsiJavaFile)file).getClasses()[0] : null;
  }

  @Nullable
  public static PsiPackage getPsiPackage(PsiElement element) {
    if (element instanceof PsiPackage) {
      return (PsiPackage)element;
    }

    if (element instanceof PsiDirectory) {
      return JavaDirectoryService.getInstance().getPackage((PsiDirectory)element);
    }

    return null;
  }

  @NotNull
  public static PsiElement getNotNull(PsiElement a, PsiElement b) {
    assert a != null || b != null;
    return a == null ? b : a;
  }

  @Nullable
  public static String getFQN(final @NotNull PsiElement element) {
    if (element instanceof PsiClass) {
      return ((PsiClass)element).getQualifiedName();
    }
    if (element instanceof PsiPackage) {
      return ((PsiPackage)element).getQualifiedName();
    }
    return null;
  }

  public static UmlClassDiagramPresentationModel getPresentationModel(@NotNull GraphBuilder builder) {
    return (UmlClassDiagramPresentationModel)builder.getGraphPresentationModel();
  }
  
  public static UmlClassDiagramDataModel getDataModel(@NotNull GraphBuilder builder) {
    return (UmlClassDiagramDataModel)builder.getGraphDataModel();
  }

  public static UmlDiagramPresentation getPresentation(@NotNull GraphBuilder builder) {
    return getPresentationModel(builder).getPresentation();
  }

  public static boolean hasNotNull(Object...objects) {
    for (Object object : objects) {
      if (object != null) return true;
    }
    return false;
  }

  public static Point getNodeCoordinatesOnScreen(Node node, Graph2DView view) {
    final Graph2D graph2D = (Graph2D)node.getGraph();
    final Point viewPoint = ((Graph2DView)graph2D.getCurrentView()).getViewPoint();

    final NodeRealizer nodeRealizer = graph2D.getRealizer(node);
    final double x = nodeRealizer.getX();
    final double y = nodeRealizer.getY();
    final JComponent owner = view.getCanvasComponent();
    final double oX = owner.getLocationOnScreen().getX();
    final double oY = owner.getLocationOnScreen().getY();

    double pX = (x - viewPoint.x)*view.getZoom() + oX;
    double pY = (y - viewPoint.y)*view.getZoom() + oY;
    pX = pX < oX ? oX : pX; 
    //pX *= view.getZoom();
    //pY *= view.getZoom();
    return new Point ((int)pX, (int)pY);
  }

  public static PackageInfo getInfo(PsiPackage p) {
    return new PackageInfo(p);
  }

  private static final UmlRelationship[] KNOWN_RELATIONSHIPS = {
    UmlRelationship.GENERALIZATION,
    UmlRelationship.REALIZATION,
    UmlRelationship.INTERFACE_GENERALIZATION,
    UmlRelationship.ANNOTATION
  };

  @Nullable
  public static UmlRelationship getRelationship(final UmlEdge edge) {
    for (UmlRelationship relationship : KNOWN_RELATIONSHIPS) {
      if (isEdgeOfType(relationship, edge)) {
        return relationship;
      }
    }
    return null;
  }

  public static Point getBestPositionForNode(GraphBuilder builder) {
    double maxY, maxX, rightest, leftest, xx, yy;
    maxY = maxX = rightest = xx = yy = -Double.MAX_VALUE;
    leftest = Double.MAX_VALUE;

    for (Object umlNode : builder.getNodeObjects()) {
      final Node node = builder.getNode(umlNode);
      if (node != null) {
        final NodeLayout nodeLayout = builder.getGraph().getNodeLayout(node);
        if (nodeLayout != null) {
          final double w = nodeLayout.getWidth();
          final double nx = nodeLayout.getX();
          final double x = nx + w;
          final double h = nodeLayout.getHeight();
          final double ny = nodeLayout.getY();
          final double y = ny + h;

          if (x > rightest) rightest = x;
          if (x < leftest) leftest = x - w;

          if (y >= maxY) {
            xx = y == maxY ? Math.max(xx, nx) : nx;
            yy = Math.max(yy, ny);
            maxX = y == maxY ? Math.max(x, maxX) : x;
            maxY = y;
          }
        }
      }
    }
    if (maxY == -Double.MAX_VALUE || maxX == -Double.MAX_VALUE) {
      return new Point(200, 200);
    } else {
      Point p = new Point();
      if (rightest - maxX < 100) {
        p.setLocation(leftest, maxY + 20);
      } else {
        p.setLocation(maxX + 20, yy);
      }
      return p;
    }
  }

  public static class PackageInfo {
    private int totalClassesCount = 0;
    private int totalSubpackagesCount = 0;
    private int classesCount = 0;
    private int subpackagesCount = 0;
    private final String fqn;
    private PackageInfo(PsiPackage p) {
      fqn = p.getQualifiedName();
      classesCount = p.getClasses().length;
      subpackagesCount = p.getSubPackages().length;
      traverse(p);
    }

    private void traverse(final PsiPackage p) {
      totalClassesCount += p.getClasses().length;
      totalSubpackagesCount += p.getSubPackages().length;
      for (PsiPackage aPackage : p.getSubPackages()) {
        traverse(aPackage);
      }
    }

    @NonNls
    public String toString() {
      return "<html><body><b>Package <font color=green>" + fqn + "</font><br>"
          + "Contains:<br>"
          + "&nbsp;&nbsp;&nbsp;&nbsp;<font color=blue>" + classesCount  +"</font>"+ ((classesCount == 1) ? " class" : " classes") + "<br>"
          + "&nbsp;&nbsp;&nbsp;&nbsp;<font color=blue>" + subpackagesCount +"</font>"+ ((subpackagesCount == 1) ? " subpackage" : " subpackages") + "<br>"
          + "Total:<br>"
          + "&nbsp;&nbsp;&nbsp;&nbsp;<font color=blue>" + totalClassesCount  +"</font>"+ ((totalClassesCount == 1) ? " class" : " classes") + "<br>"
          + "&nbsp;&nbsp;&nbsp;&nbsp;<font color=blue>" + totalSubpackagesCount +"</font>"+ ((totalSubpackagesCount == 1) ? " subpackage" : " subpackages")
          + "</b></body></html>";
    }
  }

  public static boolean isEdgeOfType(UmlRelationship relationship, UmlEdge edge) {
    if (relationship == UmlRelationship.INTERFACE_GENERALIZATION) {
      final PsiElement c1 = edge.getSource().getIdentifyingElement();
      final PsiElement c2 = edge.getTarget().getIdentifyingElement();
      if (!(c1 instanceof PsiClass && c2 instanceof PsiClass
        && ((PsiClass)c1).isInterface() && ((PsiClass)c2).isInterface())) {
        return false;
      }
    }
    return relationship.getArrow().equals(edge.getArrow())
        && relationship.getLineType().equals(edge.getLineType());
  }

  public static Document readUmlFileFromFile(InputStream is) throws JDOMException, IOException {
    return new SAXBuilder().build(is);
  }

  @Nullable
  public static UmlNode getNodeByFQN(String fqn, @NotNull GraphBuilder<UmlNode, UmlEdge> builder) {
    if (fqn == null) return null;

    for (UmlNode umlNode : getDataModel(builder).getNodes()) {
      if (umlNode instanceof UmlPsiElementNode) {
        if (fqn.equals(((UmlPsiElementNode)umlNode).getFQN())) {
          return umlNode;
        }
      }
    }
    return null;
  }

  public static UmlRelationship findRelationship(String relationship) {
    for (UmlRelationship knownRelationship : KNOWN_RELATIONSHIPS) {
      if (knownRelationship.toString().equals(relationship)) {
        return knownRelationship;
      }
    }
    return UmlRelationship.NO_RELATIONSHIP;
  }

  @NotNull
  public static Color getElementColor(@NotNull Object element) {
    Color color = null;
    if (element instanceof PsiElement) {
      final PsiFile containingFile = ((PsiElement)element).getContainingFile();
    if (containingFile != null) {
      final VirtualFile file = containingFile.getVirtualFile();
      if (file == null) return Color.BLACK;

      color = FileStatusManager.getInstance(((PsiElement)element).getProject())
      .getStatus(file).getColor();
    }
    }
    return color == null ? Color.BLACK : color;
  }


}
