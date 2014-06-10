package com.sixrr.xrp.changeattributevalue;

import com.intellij.psi.xml.XmlAttribute;
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

class ChangeAttributeValueProcessor extends XRPBaseRefactoringProcessor {
    private final XmlAttribute attribute;
    private final String newAttributeValue;
    private final Context context;

    ChangeAttributeValueProcessor(XmlAttribute attribute, String newAttributeValue, Context context, boolean previewUsages) {
        super(attribute.getProject(), previewUsages);
        this.attribute = attribute;
        this.newAttributeValue = newAttributeValue;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new ChangeAttributeValueUsageViewDescriptor(attribute, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(@NotNull List<XRPUsageInfo> usages) {
        final String attributeName = attribute.getName();
        final String attributeNamespace = attribute.getNamespace();
        final String attributeValue = attribute.getValue();
        final XmlTag tag = attribute.getParent();
        final String tagName = tag.getName();

        final TagSearchVisitor visitor = new TagSearchVisitor(tagName);
        for (XmlFile file : context) {
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for (XmlTag xmlTag : tagsFound) {

                final XmlAttribute foundAttribute =
                        xmlTag.getAttribute(attributeName, attributeNamespace);
                if (foundAttribute != null && foundAttribute.getValue().equals(attributeValue)) {
                    usages.add(new ChangeAttributeValue(foundAttribute, newAttributeValue));
                }
        }
    }

    protected String getCommandName() {
        return "Change value of attribute " + attribute.getName() ;
    }
}
