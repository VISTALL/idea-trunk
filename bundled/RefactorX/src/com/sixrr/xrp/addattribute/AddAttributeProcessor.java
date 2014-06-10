package com.sixrr.xrp.addattribute;

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

class AddAttributeProcessor extends XRPBaseRefactoringProcessor {
    private final XmlTag tag;
    private final String attributeName;
    private final String attributeValue;
    private final boolean addOnlyIfAbsent;
    private final Context context;


    AddAttributeProcessor(XmlTag tag,
                          String attributeName,
                          String attributeValue,
                          boolean addOnlyIfAbsent,
                          Context context, boolean previewUsages) {
        super(tag.getProject(), previewUsages);
        this.attributeValue = attributeValue;
        this.attributeName = attributeName;
        this.tag = tag;
        this.addOnlyIfAbsent = addOnlyIfAbsent;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new AddAttributeUsageViewDescriptor(tag, usageInfos);
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
            if (!addOnlyIfAbsent || xmlTag.getAttributeValue(attributeName) == null) {
                usages.add(new AddAttribute(xmlTag, attributeName, attributeValue));
            }
        }
    }

    protected String getCommandName() {
        return "Add attribute " + attributeName + " to tag " + tag.getName();
    }
}
