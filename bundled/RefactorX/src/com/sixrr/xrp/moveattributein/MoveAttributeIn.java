package com.sixrr.xrp.moveattributein;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class MoveAttributeIn extends XRPUsageInfo {
    private final XmlTag tag;
    private final String attributeName;
    private final String attributeNamespace;
    private final String tagName;

    MoveAttributeIn(XmlTag tag, String tagName, String attributeName, String attributeNamespace) {
        super(tag);
        this.tag = tag;
        this.attributeName = attributeName;
        this.attributeNamespace = attributeNamespace;
        this.tagName = tagName;
    }

    public void fixUsage() throws IncorrectOperationException {
        final XmlAttribute attribute = tag.getAttribute(attributeName, attributeNamespace);
        if (attribute == null) {
            return;
        }
        final String value = attribute.getValue();
        final XmlTag[] tags = tag.getSubTags();
        for (XmlTag childTag : tags) {
            if (childTag.getName().equals(tagName)) {
                childTag.setAttribute(attributeName, attributeNamespace, value);
            }
        }
        attribute.delete();
    }
}
