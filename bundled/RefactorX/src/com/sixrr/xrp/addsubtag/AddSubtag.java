package com.sixrr.xrp.addsubtag;

import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class AddSubtag extends XRPUsageInfo {
    private final XmlTag tag;
    private final String subtagName;

    AddSubtag(XmlTag tag, String subtagName) {
        super(tag);
        this.tag = tag;
        this.subtagName = subtagName;
    }

    public void fixUsage() throws IncorrectOperationException {
        final PsiManager manager = tag.getManager();
        final XmlTag childTag = tag.createChildTag(subtagName, "", "", true);
        tag.add(childTag);
        final CodeStyleManager codeStyleManager = manager.getCodeStyleManager();
        codeStyleManager.reformat(tag);
    }
}
