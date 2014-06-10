package com.sixrr.xrp.deleteattribute;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class DeleteAttribute extends XRPUsageInfo {
    private final XmlTag tag;
    private final String attributeName;
    private final String attributeNamespace;

    DeleteAttribute(XmlTag tag, String attributeName, String attributeNamespace) {
        super(tag);
        this.tag = tag;
        this.attributeName = attributeName;
        this.attributeNamespace = attributeNamespace;
    }

    public void fixUsage() throws IncorrectOperationException {
        final XmlAttribute attribute = tag.getAttribute(attributeName, attributeNamespace);
        if (attribute != null) {
            attribute.delete();
        }
    }
}
