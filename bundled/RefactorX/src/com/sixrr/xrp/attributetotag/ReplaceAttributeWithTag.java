package com.sixrr.xrp.attributetotag;

import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.openapi.util.text.StringUtil;
import com.sixrr.xrp.base.XRPUsageInfo;

class ReplaceAttributeWithTag extends XRPUsageInfo {
    private final XmlTag tag;
    private final String attributeName;
    private final String attributeNamespace;
    private final String tagName;

    ReplaceAttributeWithTag(XmlTag tag, String tagName, String attributeName, String attributeNamespace) {
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
        String value = attribute.getValue();
      value = StringUtil.unescapeXml(value);
      value = StringUtil.escapeXml(value);
        final PsiManager manager = tag.getManager();
        final XmlTag childTag = tag.createChildTag(tagName, attributeNamespace, value, true);
        tag.add(childTag);
        attribute.delete();
        final CodeStyleManager codeStyleManager = manager.getCodeStyleManager();
        codeStyleManager.reformat(tag);
    }
}
