package com.sixrr.xrp.renametag;

import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.sixrr.xrp.base.XRPBaseRefactoringProcessor;
import com.sixrr.xrp.base.XRPUsageInfo;
import com.sixrr.xrp.context.Context;
import com.sixrr.xrp.psi.TagSearchVisitor;

import java.util.List;

class RenameTagProcessor extends XRPBaseRefactoringProcessor{

    private final XmlTag tag;
    private final String newTagName;
    private final Context context;

    RenameTagProcessor(XmlTag tag, String newTagName, Context context, boolean previewUsages){
        super(tag.getProject(), previewUsages);
        this.tag = tag;
        this.newTagName = newTagName;
        this.context = context;
    }

    protected UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos){
        return new RenameTagUsageViewDescriptor(tag, usageInfos);
    }

    @SuppressWarnings({"MethodWithMultipleLoops"})
    public void findUsages(List<XRPUsageInfo> usages){
        final String tagName = tag.getName();
        final TagSearchVisitor visitor = new TagSearchVisitor(tagName);
        for(XmlFile file : context){
            file.accept(visitor);
        }
        final List<XmlTag> tagsFound = visitor.getTags();
        for(XmlTag xmlTag : tagsFound){
            if(xmlTag != null){
                usages.add(new RenameTag(xmlTag, newTagName));
            }
        }
    }

    protected String getCommandName(){
        return "Rename tag " + tag.getName();
    }
}
