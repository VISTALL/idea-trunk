package com.sixrr.xrp.addsubtag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.sixrr.xrp.base.BaseUsageViewDescriptor;
import com.sixrr.xrp.utils.MyUsageViewUtil;

class AddSubtagUsageViewDescriptor extends BaseUsageViewDescriptor {
    private final XmlTag tag;

    AddSubtagUsageViewDescriptor(XmlTag tag, UsageInfo[] usages) {
        super(usages);
        this.tag = tag;
    }

    public String getCodeReferencesText(int usagesCount, int filesCount) {
        return "Tags to add subtag to " + MyUsageViewUtil.getUsageCountInfo(usagesCount, filesCount, REFERENCE_WORD);
    }

    public String getProcessedElementsHeader() {
        return "Selected tag:";
    }

    public PsiElement[] getElements() {
        return new PsiElement[]{tag};
    }

}
