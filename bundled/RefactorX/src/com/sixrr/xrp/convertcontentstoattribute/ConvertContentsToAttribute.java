package com.sixrr.xrp.convertcontentstoattribute;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.psi.XMLMutationUtils;
import com.sixrr.xrp.utils.XMLUtil;

class ConvertContentsToAttribute extends XRPUsageInfo {
    private final XmlTag tag;
    private final String attributeName;
    private final boolean trim;

    ConvertContentsToAttribute(XmlTag tag, String attributeName, boolean trim) {
        super(tag);
        this.tag = tag;
        this.attributeName = attributeName;
        this.trim = trim;
    }

    public void fixUsage() throws IncorrectOperationException {
        String xmlTagBody = XMLMutationUtils.calculateContentsString(tag);
        if (trim) {
            xmlTagBody = xmlTagBody.trim();
        }

        final String escapedContents = XMLUtil.XMLEscape(xmlTagBody);
        if (!XMLMutationUtils.tagHasContents(tag)) {
            final String startString = tag.getText();
            final String newTag = startString.subSequence(0, startString.length() - 2) + " " + attributeName + " = \"\"/>";
            XMLMutationUtils.replaceTag(tag, newTag);
        } else {
            final String startString = XMLMutationUtils.calculateStartTagString(tag);
            final String newTag = startString.subSequence(0, startString.length() - 1) + " " + attributeName + " = \"" + escapedContents + "\"/>";
            XMLMutationUtils.replaceTag(tag, newTag);
        }

    }
}
