package com.sixrr.xrp.changeattributevalue;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.utils.XMLUtil;

class ChangeAttributeValue extends XRPUsageInfo {
    private final XmlAttribute attribute;
    private final String newAttributeValue;          

    ChangeAttributeValue(XmlAttribute attribute, String newAttributeValue) {
        super(attribute);
        this.attribute = attribute;
        this.newAttributeValue = newAttributeValue;
    }

    public void fixUsage() throws IncorrectOperationException {
        final String escapedValue = XMLUtil.XMLEscape(newAttributeValue);
        attribute.setValue(escapedValue);
    }
}
