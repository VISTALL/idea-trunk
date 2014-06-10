package com.sixrr.xrp.expandemptytag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.intention.Intention;
import com.sixrr.xrp.intention.PsiElementPredicate;
import com.sixrr.xrp.psi.XMLMutationUtils;
import org.jetbrains.annotations.NotNull;

public class ExpandEmptyTagIntention extends Intention {

    @NotNull
    public String getText() {
        return "Expand Empty Tag";
    }

    @NotNull
    public String getFamilyName() {
        return "Expand Empty Tag";
    }

    @NotNull
    protected PsiElementPredicate getElementPredicate() {
        return new ExpandEmptyTagPredicate();
    }

    protected void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final XmlTag tag = (XmlTag) element;
        final int textLength = tag.getTextLength();
        final StringBuffer newTagBuffer = new StringBuffer(textLength + 20);
        final PsiElement[] children = tag.getChildren();
        for (PsiElement child : children) {
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();
                if (tokenType.equals(XmlTokenType.XML_EMPTY_ELEMENT_END)) {
                    break;
                }
            }
            final String text = child.getText();
            newTagBuffer.append(text);
        }
        final String name = tag.getName();
        newTagBuffer.append("></" + name + '>');
        final String newTag = newTagBuffer.toString();
        XMLMutationUtils.replaceTag(tag, newTag);
    }

}
