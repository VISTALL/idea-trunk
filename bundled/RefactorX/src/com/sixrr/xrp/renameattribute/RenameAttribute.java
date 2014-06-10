package com.sixrr.xrp.renameattribute;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class RenameAttribute extends XRPUsageInfo{
    private final XmlAttribute attribute;
    private final String newAttributeName;

    RenameAttribute(XmlAttribute attribute, String newAttributeName){
        super(attribute);
        this.attribute = attribute;
        this.newAttributeName = newAttributeName;
    }

    public void fixUsage() throws IncorrectOperationException{
        attribute.setName(newAttributeName);
    }
}
