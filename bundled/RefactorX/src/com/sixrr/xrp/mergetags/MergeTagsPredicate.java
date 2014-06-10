package com.sixrr.xrp.mergetags;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.sixrr.xrp.intention.PsiElementPredicate;
import com.sixrr.xrp.utils.XMLUtil;

class MergeTagsPredicate implements PsiElementPredicate {

    public boolean satisfiedBy(PsiElement element) {
        if (!(element instanceof XmlTag) ||
            element.getContainingFile().getViewProvider() instanceof TemplateLanguageFileViewProvider) {
            return false;
        }
        final XmlTag tag = (XmlTag) element;
        final PsiElement nextTag =
                PsiTreeUtil.skipSiblingsForward(element,
                        new Class[]{PsiWhiteSpace.class, XmlText.class});
        if (!(nextTag instanceof XmlTag)) {
            return false;
        }
        return tagsCanBeMerged(tag, (XmlTag) nextTag);
    }


    private static boolean tagsCanBeMerged(XmlTag tag, XmlTag nextTag) {
        final String tagName = tag.getName();
        final String nextTagName = nextTag.getName();
        if (!safeEquals(tagName, nextTagName)) {
            return false;
        }
        final String tagPrefix = tag.getNamespacePrefix();
        final String nextTagPrefix = nextTag.getNamespacePrefix();
        if (!safeEquals(tagPrefix, nextTagPrefix)) {
            return false;
        }
        final XmlAttribute[] attributes = tag.getAttributes();
        final XmlAttribute[] nextAttributes = nextTag.getAttributes();
        if (attributes.length != nextAttributes.length) {
            return false;
        }
        for (XmlAttribute xmlAttribute : attributes) {
            final String attributeName = xmlAttribute.getName();
            final String attributeNameSpace = xmlAttribute.getNamespace();
            final String attributeValue = xmlAttribute.getValue();
            final String nextAttributeValue =
                    nextTag.getAttributeValue(attributeName, attributeNameSpace);

            if (!safeEquals(attributeValue, nextAttributeValue)) {
                return false;
            }
        }
        if (XMLUtil.containsOuterElements(tag) || XMLUtil.containsOuterElements(nextTag)) {
            return false;
        }
        return true;
    }

    private static boolean safeEquals(String tagName, String nextTagName) {
        if (tagName == null) {
            return nextTagName == null;
        } else {
            return tagName.equals(nextTagName);
        }
    }
}
