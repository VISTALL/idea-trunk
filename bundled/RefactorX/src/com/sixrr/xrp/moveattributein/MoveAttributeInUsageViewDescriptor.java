package com.sixrr.xrp.moveattributein;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.usageView.UsageInfo;
import com.sixrr.xrp.base.BaseUsageViewDescriptor;
import com.sixrr.xrp.utils.MyUsageViewUtil;

class MoveAttributeInUsageViewDescriptor extends BaseUsageViewDescriptor {
    private final XmlAttribute attribute;

    MoveAttributeInUsageViewDescriptor(XmlAttribute attribute, UsageInfo[] usages) {
        super(usages);
        this.attribute = attribute;
    }

    public String getCodeReferencesText(int usagesCount, int filesCount) {
        return "Attributes to move inward " + MyUsageViewUtil.getUsageCountInfo(usagesCount, filesCount, REFERENCE_WORD);
    }

    public String getProcessedElementsHeader() {
        return "Selected attribute:";
    }

    public PsiElement[] getElements() {
        return new PsiElement[]{attribute};
    }

}
