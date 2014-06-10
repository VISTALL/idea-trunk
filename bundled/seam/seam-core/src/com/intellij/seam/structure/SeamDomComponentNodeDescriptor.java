package com.intellij.seam.structure;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.javaee.module.view.nodes.JavaeeNodeDescriptor;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiType;
import com.intellij.seam.SeamIcons;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.seam.model.xml.components.SeamDomComponent;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SeamDomComponentNodeDescriptor extends JavaeeNodeDescriptor<SeamDomComponent> {
  public SeamDomComponentNodeDescriptor(final Project project,
                                        final NodeDescriptor parentDescriptor,
                                        final Object parameters,
                                        final SeamDomComponent element) {
    super(project, parentDescriptor, parameters, element);
  }

  protected String getNewNodeText() {
    SeamDomComponent seamDomComponent = getElement();
    if (!seamDomComponent.isValid()) return "";

    String name = seamDomComponent.getComponentName();
    
    return name == null ? "<" + seamDomComponent.getXmlTag().getName()+ " ... />": name;
  }

  protected Icon getNewOpenIcon() {
    return SeamIcons.SEAM_COMPONENT_ICON;
  }

  protected Icon getNewClosedIcon() {
    return SeamIcons.SEAM_COMPONENT_ICON;
  }

  public boolean isValid() {
    return getElement().isValid();
  }

  protected void doUpdate() {
    super.doUpdate();
    final String textExt = getNewNodeTextExt();
    if (textExt != null) {
      addColoredFragment(" (" + getNewNodeTextExt() + ")", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
    }
  }

  @Nullable
  protected String getNewNodeTextExt() {
    if (!isValid()) return null;

    SeamDomComponent seamDomComponent = getElement();
    PsiType psiType = seamDomComponent.getComponentType();

    return psiType == null ? null : psiType.getPresentableText();
  }

  public Object getData(final String dataId) {
    if (DataConstants.PSI_ELEMENT.equals(dataId)) {
      return getElement().getIdentifyingPsiElement();
    }

    return super.getData(dataId);
  }

  public String getNewTooltip() {
    if (!isValid()) return null;

    SeamDomComponent seamDomComponent = getElement();

    StringBuffer tooltip = new StringBuffer();
    final String name = seamDomComponent.getComponentName();
    final PsiType psiType = seamDomComponent.getComponentType();
    final SeamComponentScope scope = seamDomComponent.getComponentScope();

    if (name != null) {
      tooltip.append("<tr><td><strong>name:</strong></td><td>").append(name).append("</td></tr>");
    }
    if (psiType != null) {
      tooltip.append("<tr><td><strong>class:</strong></td><td>").append(psiType.getCanonicalText()).append("</td></tr>");
    }

    if (scope != null) {
     tooltip.append("<tr><td><strong>scope:</strong></td><td>").append(scope.getValue()).append("</td></tr>");
    }

    if (tooltip.length() != 0) {
      return "<html><table>" + tooltip.toString() + "</table></html>";
    }

    return tooltip.toString();
  }
}
