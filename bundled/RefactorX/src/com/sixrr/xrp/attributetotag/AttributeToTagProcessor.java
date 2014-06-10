package com.sixrr.xrp.attributetotag;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;
import com.sixrr.xrp.psi.XMLMutationUtils;

import java.util.List;

class AttributeToTagProcessor extends XRPBaseRefactoringProcessor {
    private final XmlAttribute attribute;
    private final String tagName;
    private final Context context;

    AttributeToTagProcessor(XmlAttribute attribute, String tagName, Context context, boolean previewUsages) {
        super(attribute.getProject(), previewUsages);
        this.attribute = attribute;
        this.tagName = tagName;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos) {
        return new AttributeToTagUsageViewDescriptor(attribute, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(List<XRPUsageInfo> usages) {
        final String attributeName = attribute.getName();
        final String attributeNamespace = attribute.getNamespace();
        final XmlTag parentTag = attribute.getParent();
        final String existingTagName = parentTag.getName();
        final TagSearchVisitor visitor = new TagSearchVisitor(existingTagName);
        for (XmlFile file : context) {
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for (XmlTag xmlTag : tagsFound) {
            if (!XMLMutationUtils.isSpecialJSPTag(xmlTag)) {
                final XmlAttribute foundAttribute =
                        xmlTag.getAttribute(attributeName, attributeNamespace);
                if (foundAttribute != null) {
                    usages.add(new ReplaceAttributeWithTag(xmlTag, tagName, attributeName, attributeNamespace));
                }
            }
        }
    }

    protected String getCommandName() {
        return "Replace attribute " + attribute.getName() +" with tag";
    }
}
