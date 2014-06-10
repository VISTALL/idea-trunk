package com.sixrr.xrp.convertcontentstoattribute;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class ConvertContentsToAttributeProcessor extends XRPBaseRefactoringProcessor {
    private final XmlTag tag;
    private final String attributeName;
    private final Context context;
    private final boolean trim;

    ConvertContentsToAttributeProcessor(XmlTag tag, String attributeName, Context context, boolean trim, boolean previewUsages) {
        super(tag.getProject(), previewUsages);
        this.tag = tag;
        this.attributeName = attributeName;
        this.context = context;
        this.trim = trim;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new ConvertContentsToAttributeUsageViewDescriptor(tag, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(@NotNull List<XRPUsageInfo> usages) {
        final String tagName = tag.getName();
        final TagSearchVisitor visitor = new TagSearchVisitor(tagName);
        for (XmlFile file : context) {
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for (XmlTag xmlTag : tagsFound) {
            usages.add(new ConvertContentsToAttribute(xmlTag, attributeName, trim));
        }
    }

    protected String getCommandName() {
        return "Convert contents of tag " + tag.getName() + " to attribute " + attributeName;
    }
}
