package com.sixrr.xrp.deletetag;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;

class DeleteTag extends XRPUsageInfo {
    private final XmlTag tag;

    DeleteTag(XmlTag tag) {
        super(tag);
        this.tag = tag;
    }

    public void fixUsage() throws IncorrectOperationException {
        tag.delete();
    }
}
