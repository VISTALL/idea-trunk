package com.sixrr.xrp.addattribute;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class AddAttribute extends XRPUsageInfo {
    private final XmlTag tag;
    private final String attributeName;
    private final String attributeValue;

    AddAttribute(XmlTag tag, String attributeName, String attributeValue) {
        super(tag);
        this.tag = tag;
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    public void fixUsage() throws IncorrectOperationException {
        tag.setAttribute(attributeName, attributeValue);
    }
}
