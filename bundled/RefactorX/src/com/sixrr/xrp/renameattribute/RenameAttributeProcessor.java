package com.sixrr.xrp.renameattribute;

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

class RenameAttributeProcessor extends XRPBaseRefactoringProcessor{

    private final XmlAttribute attribute;
    private final String newAttributeName;
    private final Context context;

    RenameAttributeProcessor(XmlAttribute attribute, String newAttributeName, Context context, boolean previewUsages){
        super(attribute.getProject(), previewUsages);
        this.attribute = attribute;
        this.newAttributeName = newAttributeName;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos){
        return new RenameAttributeUsageViewDescriptor(attribute, usageInfos);
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
            final XmlAttribute attribute = xmlTag.getAttribute(attributeName, attributeNamespace);
            if(attribute != null){
                usages.add(new RenameAttribute(attribute, newAttributeName));
            }
        }
    }

    protected String getCommandName(){
        return "Rename attribute " + attribute.getName();
    }
}
