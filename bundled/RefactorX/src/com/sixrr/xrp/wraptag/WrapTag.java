package com.sixrr.xrp.wraptag;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.psi.XMLMutationUtils;

class WrapTag extends XRPUsageInfo {
    private final XmlTag xmlTag;
    private final String parentTagName;

    WrapTag(XmlTag xmlTag, String parentTagName) {
        super(xmlTag);
        this.xmlTag = xmlTag;
        this.parentTagName = parentTagName;
    }

    public void fixUsage() throws IncorrectOperationException {
        final String newTag = '<' + parentTagName + '>' + xmlTag.getText() + "</" + parentTagName + '>';
        XMLMutationUtils.replaceTag(xmlTag, newTag);
    }
}
