package com.sixrr.xrp.wraptagcontents;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.psi.XMLMutationUtils;

class WrapTagContents extends XRPUsageInfo {
    private final XmlTag tag;
    private final String wrapTagName;

    WrapTagContents(XmlTag tag, String wrapTagName) {
        super(tag);
        this.tag = tag;
        this.wrapTagName = wrapTagName;
    }

    public void fixUsage() throws IncorrectOperationException {
        final String newTag;
        if (XMLMutationUtils.tagHasContents(tag)) {
            final String xmlTagStart = XMLMutationUtils.calculateStartTagString(tag);
            final String xmlTagEnd = XMLMutationUtils.calculateEndTagString(tag);
            final String xmlTagBody = XMLMutationUtils.calculateContentsString(tag);
            newTag = xmlTagStart + '<' + wrapTagName + '>' + xmlTagBody + "</" + wrapTagName + '>' + xmlTagEnd;
        }else
        {
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
            newTagBuffer.append(">" + '<' + wrapTagName + "></" + wrapTagName + '>'+
                    "</" + name + '>');
            newTag = newTagBuffer.toString();

        }

        XMLMutationUtils.replaceTag(tag, newTag);
    }
}
