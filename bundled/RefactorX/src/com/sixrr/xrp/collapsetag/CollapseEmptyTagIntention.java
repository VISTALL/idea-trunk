package com.sixrr.xrp.collapsetag;

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

public class CollapseEmptyTagIntention extends Intention {

    @NotNull
    public String getText() {
        return "Collapse Empty Tag";
    }

    @NotNull
    public String getFamilyName() {
        return "Collapse Empty Tag";
    }

    @NotNull
    protected PsiElementPredicate getElementPredicate() {
        return new CollapseEmptyTagPredicate();
    }

    protected void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final XmlTag tag = (XmlTag) element;
        final int textLength = tag.getTextLength();
        final StringBuffer newTagBuffer = new StringBuffer(textLength);
        final PsiElement[] children = tag.getChildren();
        for (PsiElement child : children) {
            if (child instanceof XmlToken) {
                final IElementType tokenType = ((XmlToken) child).getTokenType();
                if (tokenType.equals(XmlTokenType.XML_TAG_END)) {
                    break;
                }
            }
            final String text = child.getText();
            newTagBuffer.append(text);
        }
        newTagBuffer.append("/>");
        final String newTag = newTagBuffer.toString();
        XMLMutationUtils.replaceTag(tag, newTag);
    }

}
