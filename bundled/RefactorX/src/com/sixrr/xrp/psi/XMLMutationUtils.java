package com.sixrr.xrp.psi;

import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.IncorrectOperationException;

import java.util.ArrayList;
import java.util.List;

public class XMLMutationUtils {
    private XMLMutationUtils() {
        super();
    }

    public static void replaceTag(XmlTag tag, String newTag) throws IncorrectOperationException {
      final PsiManager manager = tag.getManager();
      final XmlElementFactory elementFactory = XmlElementFactory.getInstance(manager.getProject());
      final XmlTag replacementTag = elementFactory.createTagFromText(newTag);
      final PsiElement replacedTag = tag.replace(replacementTag);
      final CodeStyleManager styleManager = manager.getCodeStyleManager();
      styleManager.reformat(replacedTag);
    }

    public static String calculateStartTagString(XmlTag tag) {
        final StringBuffer out = new StringBuffer();
        final PsiElement[] children = tag.getChildren();
        for (PsiElement child : children) {
            final String childText = child.getText();
            out.append(childText);
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();
                if (tokenType.equals(XmlTokenType.XML_TAG_END) ||
                        tokenType.equals(XmlTokenType.XML_EMPTY_ELEMENT_END)) {
                    break;
                }
            }
        }
        return out.toString();
    }

    public static String calculateContentsString(XmlTag tag) {
        final StringBuffer out = new StringBuffer();
        final PsiElement[] children = tag.getChildren();
        boolean inContents = false;
        for (PsiElement child : children) {
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();

                if (tokenType.equals(XmlTokenType.XML_END_TAG_START)) {
                    inContents = false;
                }
            }
            if (inContents) {
                final String childText = child.getText();
                out.append(childText);
            }
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();

                if (tokenType.equals(XmlTokenType.XML_TAG_END)) {
                    inContents = true;
                }
            }
        }
        return out.toString();
    }

    public static List<PsiElement> calculateContents(XmlTag tag) {
        final List<PsiElement> out = new ArrayList<PsiElement>();
        final PsiElement[] children = tag.getChildren();
        boolean inContents = false;
        for (PsiElement child : children) {
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();

                if (tokenType.equals(XmlTokenType.XML_END_TAG_START)) {
                    inContents = false;
                }
            }
            if (inContents) {
                out.add(child);
            }
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();

                if (tokenType.equals(XmlTokenType.XML_TAG_END)) {
                    inContents = true;
                }
            }
        }
        return out;
    }

    public static String calculateEndTagString(XmlTag tag) {
        final StringBuffer out = new StringBuffer();
        final PsiElement[] children = tag.getChildren();
        boolean inEndTag = false;
        for (PsiElement child : children) {
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();
                if (tokenType.equals(XmlTokenType.XML_END_TAG_START)) {
                    inEndTag = true;
                }
            }
            if (inEndTag) {
                final String childText = child.getText();
                out.append(childText);
            }
        }
        return out.toString();
    }

    public static boolean tagIsWellFormed(XmlTag tag) {
        final PsiElement[] children = tag.getChildren();
        for (PsiElement child : children) {
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();
                if (tokenType.equals(XmlTokenType.XML_EMPTY_ELEMENT_END) &&
                        "/>".equals(child.getText())) {
                    return true;
                }
                if (tokenType.equals(XmlTokenType.XML_END_TAG_START)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean tagHasContents(XmlTag tag) {
        final PsiElement[] children = tag.getChildren();
        final PsiElement lastChild = children[children.length - 1];
        if (lastChild instanceof XmlToken) {
            final IElementType tokenType = ((XmlToken) lastChild).getTokenType();
            if (tokenType.equals(XmlTokenType.XML_EMPTY_ELEMENT_END)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isSpecialJSPTag(XmlTag xmlTag) {
        final PsiElement firstChild = xmlTag.getFirstChild();
        assert firstChild != null;
        final String text = firstChild.getText();
        return !"<".equals(text);
    }
}
