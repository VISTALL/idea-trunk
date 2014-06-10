package com.sixrr.xrp.references;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class IdrefReference implements PsiReference {
    private final XmlAttributeValue referenceElement;

    IdrefReference(XmlAttributeValue referenceElement) {
        this.referenceElement = referenceElement;
    }

    public PsiElement getElement() {
        return referenceElement;
    }

    public TextRange getRangeInElement() {
       return ElementManipulators.getManipulator(referenceElement).getRangeInElement(referenceElement);
    }

    @Nullable
    public PsiElement resolve() {
        final String text = referenceElement.getText();
      if (text.length() - 1 < 1) return null;
        final String strippedText = text.substring(1, text.length() - 1);
        return findIDAttribute(referenceElement.getContainingFile(), strippedText);
    }

    private static XmlAttribute findIDAttribute(PsiFile containingFile, final String strippedText) {
        final XmlAttribute[] out = new XmlAttribute[1];
        final PsiElementVisitor visitor = new XmlRecursiveElementVisitor() {
            @Override public void visitElement(PsiElement psiElement) {
                if (out[0] != null) {
                    return;
                }
                super.visitElement(psiElement);
            }

            @Override public void visitXmlAttribute(XmlAttribute xmlAttribute) {
                if (out[0] != null) {
                    return;
                }
                super.visitXmlAttribute(xmlAttribute);
                if ("id".equalsIgnoreCase(xmlAttribute.getName()) && strippedText.equals(xmlAttribute.getValue())) {
                    out[0] = xmlAttribute;
                }
            }
        };
        containingFile.accept(visitor);
        return out[0];
    }


    public String getCanonicalText() {
        return referenceElement.getText();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
        final XmlAttribute attribute = (XmlAttribute) referenceElement.getParent();
        attribute.setValue(newElementName);
        final XmlAttributeValue out = attribute.getValueElement();
        assert out != null;
        return out;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    public boolean isReferenceTo(PsiElement element) {
        if (element == null) {
            return false;
        }
        if (!(element instanceof XmlAttribute)) {
            return false;
        }
        final XmlAttribute xmlAttribute = (XmlAttribute) element;
        if (!"id".equalsIgnoreCase(xmlAttribute.getName())) {
            return false;
        }

        return element.equals(resolve());
    }

    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    public boolean isSoft() {
        return false;
    }
}
