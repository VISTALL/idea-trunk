package com.advancedtools.webservices.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlAttributeValue;

/**
 * @author Maxim
 */
public class XmlRecursiveElementVisitor extends PsiElementVisitor {
  public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {
  }

  public void visitXmlAttribute(XmlAttribute xmlAttribute) {
    visitElement(xmlAttribute);
  }

  public void visitXmlAttributeValue(XmlAttributeValue xmlAttributeValue) {
    visitElement(xmlAttributeValue);
  }

  public void visitXmlTag(XmlTag xmlTag) {
    visitElement(xmlTag);
  }

  public void visitElement(PsiElement psiElement) {
    PsiElement child = psiElement.getFirstChild();
    while(child != null) {
      if (child instanceof XmlTag) visitXmlTag((XmlTag) child);
      else if (child instanceof XmlAttribute) visitXmlAttribute((XmlAttribute) child);
      else if (child instanceof XmlAttributeValue) visitXmlAttributeValue((XmlAttributeValue) child);
      else visitElement(child);

      child = child.getNextSibling();
    }
  }
}
