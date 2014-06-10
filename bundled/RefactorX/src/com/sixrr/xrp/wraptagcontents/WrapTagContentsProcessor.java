package com.sixrr.xrp.wraptagcontents;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;

import java.util.List;

class WrapTagContentsProcessor extends XRPBaseRefactoringProcessor {
    private final XmlTag tag;
    private final String wrapTagName;
    private final Context context;

    WrapTagContentsProcessor(XmlTag tag, String wrapTagName, Context context, boolean previewUsages) {
        super(tag.getProject(), previewUsages);
        this.tag = tag;
        this.wrapTagName = wrapTagName;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new WrapTagContentsUsageViewDescriptor(tag, usageInfos);
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
            usages.add(new WrapTagContents(xmlTag, wrapTagName));
        }
    }

    protected String getCommandName(){
        return "Wrap content of tag " + tag.getName() + " with tag " + wrapTagName;
    }
}
