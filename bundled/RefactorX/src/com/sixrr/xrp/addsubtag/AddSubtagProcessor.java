package com.sixrr.xrp.addsubtag;

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

class AddSubtagProcessor extends XRPBaseRefactoringProcessor {
    private final XmlTag tag;
    private final String subtagName;
    private final boolean addOnlyIfAbsent;
    private final Context context;


    AddSubtagProcessor(XmlTag tag,
                          String subtagName,
                          boolean addOnlyIfAbsent,
                          Context context, boolean previewUsages) {
        super(tag.getProject(), previewUsages);
        this.subtagName = subtagName;
        this.tag = tag;
        this.addOnlyIfAbsent = addOnlyIfAbsent;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new AddSubtagUsageViewDescriptor(tag, usageInfos);
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

            if (!addOnlyIfAbsent ||!subtagExists(xmlTag)) {
                usages.add(new AddSubtag(xmlTag, subtagName));
            }
        }
    }

    private boolean subtagExists(XmlTag xmlTag) {
        final XmlTag[] subtags = xmlTag.getSubTags();
        for (XmlTag subtag : subtags) {
            if (subtag.getName().equals(subtagName)) {
                return true;
            }
        }
        return false;
    }

    protected String getCommandName() {
        return "Add subtag " + subtagName + " to tag " + tag.getName();
    }
}
