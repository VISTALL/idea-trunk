package com.sixrr.xrp.deletetag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.sixrr.xrp.base.BaseUsageViewDescriptor;
import com.sixrr.xrp.utils.MyUsageViewUtil;

class DeleteTagUsageViewDescriptor extends BaseUsageViewDescriptor {
    private final XmlTag tag;

    DeleteTagUsageViewDescriptor(XmlTag tag,UsageInfo[] usages) {
        super(usages);
        this.tag = tag;
    }

    public String getCodeReferencesText(int usagesCount, int filesCount) {
        return "Tags to delete " + MyUsageViewUtil.getUsageCountInfo(usagesCount, filesCount, REFERENCE_WORD);
    }

    public String getProcessedElementsHeader() {
        return "Selected tag:";
    }

    public PsiElement[] getElements() {
        return new PsiElement[]{tag};
    }

}
