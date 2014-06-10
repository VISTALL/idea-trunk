package com.sixrr.xrp.deleteattribute;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;

import java.util.List;

class DeleteAttributeProcessor extends XRPBaseRefactoringProcessor{

    private final XmlAttribute attribute;
    private final Context context;

    DeleteAttributeProcessor(XmlAttribute attribute, Context context, boolean previewUsages){
        super(attribute.getProject(), previewUsages);
        this.attribute = attribute;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos){
        return new DeleteAttributeUsageViewDescriptor(attribute, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(List<XRPUsageInfo> usages){
        final XmlTag tag = attribute.getParent();
        final String tagName = tag.getName();
        final String attributeName = attribute.getName();
        final String attributeNamespace = attribute.getNamespace();
        final TagSearchVisitor visitor = new TagSearchVisitor(tagName);
        for(XmlFile file : context){
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for(XmlTag xmlTag : tagsFound){
            if(xmlTag.getAttribute(attributeName, attributeNamespace) != null){
                usages.add(new DeleteAttribute(xmlTag, attributeName, attributeNamespace));
            }
        }
    }

    protected String getCommandName(){
        return "Delete attribute " + attribute.getName();
    }
}
