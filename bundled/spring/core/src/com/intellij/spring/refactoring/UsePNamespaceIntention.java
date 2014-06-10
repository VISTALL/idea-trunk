package com.intellij.spring.refactoring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.DomUtil;
import com.intellij.xml.XmlExtension;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * @author Dmitry Avdeev
 */
public class UsePNamespaceIntention implements IntentionAction {
  @NotNull
  public String getText() {
    return SpringBundle.message("use.p.namespace");
  }

  @NotNull
  public String getFamilyName() {
    return getText();
  }

  public boolean isAvailable(@NotNull final Project project, final Editor editor, final PsiFile file) {
    if (!(file instanceof XmlFile)) return false;

    final SpringProperty property = DomUtil.getContextElement(editor, SpringProperty.class);
    return property != null &&
           property.getParent() instanceof SpringBean &&
           property.getName().getStringValue() != null &&
           (DomUtil.hasXml(property.getValueElement()) || DomUtil.hasXml(property.getRefElement()));
  }

  public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
    if (SpringUpdateSchemaIntention.requestSchemaUpdate((XmlFile)file)) {
      final SpringProperty property = DomUtil.getContextElement(editor, SpringProperty.class);
      assert property != null;
      if (property.getXmlTag().getNamespaceByPrefix("p").equals(XmlUtil.EMPTY_URI)) {
        XmlExtension.getExtension((XmlFile)file).insertNamespaceDeclaration((XmlFile)file, editor,
                                                                            Collections.<String>singleton(SpringConstants.P_NAMESPACE), "p", null);  
      }
      final SpringBean bean = (SpringBean)property.getParent();
      assert bean != null;
      final String name = property.getName().getStringValue();
      final String value = property.getValueElement().getStringValue();
      final XmlTag tag = bean.getXmlTag();
      if (value != null) {
        property.undefine();
        final XmlAttribute attribute = XmlElementFactory.getInstance(project).createXmlAttribute("p:" + name, value);
        tag.add(attribute);
        tag.collapseIfEmpty();
        return;
      }
      final String ref = property.getRefElement().getStringValue();
      if (ref != null) {
        property.undefine();
        final XmlAttribute attribute = XmlElementFactory.getInstance(project).createXmlAttribute("p:" + name + "-ref", ref);
        tag.add(attribute);
        tag.collapseIfEmpty();
      }
    }
  }

  public boolean startInWriteAction() {
    return true;
  }
}
