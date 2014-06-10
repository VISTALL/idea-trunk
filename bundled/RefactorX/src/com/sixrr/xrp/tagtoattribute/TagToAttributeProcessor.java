package com.sixrr.xrp.tagtoattribute;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;

import java.util.List;

class TagToAttributeProcessor extends XRPBaseRefactoringProcessor {
    private final XmlTag tag;
    private final String attributeName;
    private final Context context;

    TagToAttributeProcessor(XmlTag tag, String attributeName, Context context, boolean previewUsages) {
        super(tag.getProject(), previewUsages);
        this.tag = tag;
        this.attributeName = attributeName;
        this.context = context;
    }


    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new TagToAttributeUsageViewDescriptor(tag, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(List<XRPUsageInfo> usages) {
        final XmlTag parentTag = (XmlTag) tag.getParent();
        final String parentTagName = parentTag.getName();
        final String tagName = tag.getName();
        final TagSearchVisitor visitor = new TagSearchVisitor(tagName);
        for (XmlFile file : context) {
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for (XmlTag xmlTag : tagsFound) {
            final XmlTag[] subTags = xmlTag.getSubTags();
            if (subTags == null || subTags.length == 0) {
                final XmlTag testParentTag = xmlTag.getParentTag();
                if (testParentTag != null &&
                        parentTagName.equals(testParentTag.getName())) {
                    usages.add(new ReplaceTagWithAttribute(xmlTag, attributeName));
                }
            }
        }
    }

    protected String getCommandName(){
        return "Replace tag " + tag.getName() + " with attribute";
    }
}
