package com.sixrr.xrp.moveattributeout;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class MoveAttributeOut extends XRPUsageInfo {
    private final XmlTag tag;
    private final String attributeName;
    private final String attributeNamespace;

    MoveAttributeOut(XmlTag tag, String attributeName, String attributeNamespace) {
        super(tag);
        this.tag = tag;
        this.attributeName = attributeName;
        this.attributeNamespace = attributeNamespace;
    }

    public void fixUsage() throws IncorrectOperationException {
        final XmlAttribute attribute = tag.getAttribute(attributeName, attributeNamespace);
        if (attribute == null) {
            return;
        }
        final XmlTag parentTag = tag.getParentTag();
        if (parentTag == null) {
            return;
        }
        parentTag.setAttribute(attributeName, attributeNamespace, attribute.getValue());
        attribute.delete();
    }
}
