package com.sixrr.xrp.renametag;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class RenameTag extends XRPUsageInfo{
    private final XmlTag tag;
    private final String newTagName;

    RenameTag(XmlTag tag, String newTagName){
        super(tag);
        this.tag = tag;
        this.newTagName = newTagName;
    }

    public void fixUsage() throws IncorrectOperationException{
        tag.setName(newTagName);
    }
}
