package com.sixrr.xrp.tagtoattribute;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.psi.XMLMutationUtils;
import com.sixrr.xrp.utils.XMLUtil;

class ReplaceTagWithAttribute extends XRPUsageInfo {
   private final XmlTag tag;
   private final String attributeName;

    ReplaceTagWithAttribute(XmlTag tag, String attributeName) {
       super(tag);
       this.tag = tag;
       this.attributeName = attributeName;
   }

   public void fixUsage() throws IncorrectOperationException {
       final XmlTag parentTag = tag.getParentTag();
       String value = XMLMutationUtils.calculateContentsString(tag);
       value = value.trim();
       value = XMLUtil.stripCDataWrapper(value);
       tag.delete();
       parentTag.setAttribute(attributeName, value);
   }
}
