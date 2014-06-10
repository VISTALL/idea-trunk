package com.sixrr.xrp.unwraptag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.psi.XMLMutationUtils;
import com.sixrr.xrp.utils.XMLUtil;

import java.util.Collections;
import java.util.List;

class UnwrapTag extends XRPUsageInfo {
    private final XmlTag tag;

    UnwrapTag(XmlTag tag) {
        super(tag);
        this.tag = tag;
    }

    public void fixUsage() throws IncorrectOperationException {
        final List<PsiElement> contents = XMLMutationUtils.calculateContents(tag);
        final PsiElement tagParent = tag.getParent();
        Collections.reverse(contents);
        for (PsiElement child : contents) {
            if(child instanceof XmlText)
            {
                final String text = child.getText();
                if(XMLUtil.isWhitespace(text))
                {
                    continue;
                }
            }
            final PsiElement childCopy = child.copy();
            tagParent.addAfter(childCopy, tag);
        }
        tag.delete();
        final PsiManager manager = tagParent.getManager();
        final CodeStyleManager styleManager = manager.getCodeStyleManager();
        styleManager.reformat(tagParent);
    }
}
