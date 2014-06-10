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

package com.intellij.uml.renderers;

import com.intellij.openapi.graph.builder.GraphBuilder;
import com.intellij.openapi.graph.builder.renderer.GradientFilledPanel;
import com.intellij.openapi.ui.VerticalFlowLayout;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiTypeParameterList;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.uml.model.UmlEdge;
import com.intellij.uml.model.UmlNode;
import com.intellij.uml.model.UmlNodeContainer;
import com.intellij.uml.presentation.UmlColorManager;
import com.intellij.uml.presentation.UmlDiagramPresentation;
import com.intellij.uml.utils.UmlUtils;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Konstantin Bulenkov
 */
public class UmlPsiElementContainer implements UmlNodeContainer {
  private GradientFilledPanel myHeader;
  private final JPanel myBody;
  private final UmlDiagramPresentation myPresentation;
  private final String myFQN;
  @NonNls private static final String CLASS_PARAMS = "<...>";

  public UmlPsiElementContainer(PsiElement psiElement, GraphBuilder<UmlNode, UmlEdge> builder, Point point) {
    myPresentation = UmlUtils.getPresentation(builder);
    myFQN = UmlUtils.getRealPackageName(psiElement);
    initHeader(psiElement);

    myBody = new JPanel(new VerticalFlowLayout(VerticalFlowLayout.TOP, 0, 0, true, false));
    myBody.setBorder(new EmptyBorder(0,0,0,0));
    if (psiElement instanceof PsiClass) {
      final UmlPsiClassContainer comp = new UmlPsiClassContainer((PsiClass)psiElement, builder, point);
      myBody.add(comp);
    }
  }

  public JPanel getHeader() {
    return myHeader;
  }

  public JPanel getBody() {
    return myBody;
  }


  private JLabel createHeaderLabel(PsiElement element) {
    @NonNls String label = "";
    boolean deprecated = UmlUtils.isDeprecated(element);

    if (element instanceof PsiClass) {
      PsiClass psiClass = (PsiClass)element;
      label = myPresentation.isCamel() ? getCamelName(psiClass.getName()) : psiClass.getName();

      PsiTypeParameterList classParams = psiClass.getTypeParameterList();
      if (classParams != null && classParams.getText() != null) {
        final String params = classParams.getText();
        label += params.length() > 7 ? CLASS_PARAMS : params;
      }
    } else if (element instanceof PsiPackage) {
      label = ((PsiPackage)element).getQualifiedName();
    }
    if (deprecated) {
      label = strikeout(label);
    }    
    JLabel headerLabel = new JLabel(label, element.getIcon(Iconable.ICON_FLAG_VISIBILITY), JLabel.HORIZONTAL) {
      @Override
      public int getWidth() {
        return super.getWidth() + 40; //to avoid dots at the end of header when zooming
      }
    };

    int style = Font.BOLD;
    headerLabel.setFont(headerLabel.getFont().deriveFont(style));
    headerLabel.setBorder(IdeBorderFactory.createEmptyBorder(3, 3, 3, 3));
    headerLabel.setHorizontalAlignment(SwingConstants.LEFT);
    return headerLabel;
  }

  private void initHeader(PsiElement element) {
    JLabel headerLabel = createHeaderLabel(element);
    myHeader = new GradientFilledPanel(getBG(getMode())) {
      @Override
      public void paint(final Graphics g) {
        setGradientColor(getBG(getMode()));
        super.paint(g);
      }
    };
    myHeader.setLayout(new BorderLayout());
    myHeader.add(headerLabel, BorderLayout.CENTER);
    headerLabel.setForeground(UmlUtils.getElementColor(element));
    myHeader.setFocusable(false);
  }

  private HighlightingMode getMode() {
    String _package = myPresentation.getHighlightedPackage();
    if (_package == null) return HighlightingMode.NONE;
    if (myFQN == null) return HighlightingMode.NONE;
    if (myFQN.equals(_package)) return HighlightingMode.PACKAGE;
    if (StringUtil.startsWithConcatenationOf(myFQN, _package, ".")) return HighlightingMode.SUB_PACKAGE;
    return HighlightingMode.NONE;
  }

  private static String getCamelName(String name) {
    if (name.length() == 0 || !isCapital(name.charAt(0))) return name;
    return deleteLowCaseLetters( deleteNonLetterFromString(name) );
  }

  private static boolean isCapital(final char c) {
    return 'A' <= c && c <= 'Z';
  }


  private static @NonNls final Pattern NON_LETTER = Pattern.compile("[^a-zA-Z]");
  private static @NonNls final Pattern LOW_CASE_LETTERS = Pattern.compile("[a-z]");

  private static String deleteNonLetterFromString(String tempString) {
    Matcher matcher = NON_LETTER.matcher(tempString);
    return matcher.replaceAll("");
  }

  private static String deleteLowCaseLetters(String tempString) {
    Matcher matcher = LOW_CASE_LETTERS.matcher(tempString);
    return matcher.replaceAll("");
  }

  @NonNls
  private static String strikeout(String s) {
    return "<html><strike>" + s + "</strike></html>";
  }

  private static Color getBG(HighlightingMode mode) {
    switch (mode) {
      case NONE: return UmlColorManager.getInstance().getCaptionColor();
      case PACKAGE: return UmlColorManager.getInstance().getPackageCaptionColor();
      case SUB_PACKAGE: return UmlColorManager.getInstance().getSubPackageCaptionColor();
      default: return UmlColorManager.getInstance().getCaptionColor();
    }
  }
}
