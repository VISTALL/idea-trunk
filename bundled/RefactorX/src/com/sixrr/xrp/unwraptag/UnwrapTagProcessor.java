package com.sixrr.xrp.unwraptag;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;

import java.util.List;

class UnwrapTagProcessor extends XRPBaseRefactoringProcessor {

    private final XmlTag tag;
    private final Context context;

    UnwrapTagProcessor(XmlTag tag, Context context, boolean previewUsages) {
        super(tag.getProject(), previewUsages);
        this.tag = tag;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new UnwrapTagUsageViewDescriptor(tag, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(List<XRPUsageInfo> usages) {
        final String tagName = tag.getName();
        final TagSearchVisitor visitor = new TagSearchVisitor(tagName);
        for (XmlFile file : context) {
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for (XmlTag xmlTag : tagsFound) {
            if (xmlTag.getParentTag() != null) {
                usages.add(new UnwrapTag(xmlTag));
            }
        }
    }

    protected String getCommandName() {
        return "Unwrap tag " + tag.getName();
    }
}
