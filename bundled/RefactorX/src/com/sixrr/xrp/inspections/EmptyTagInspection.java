package com.sixrr.xrp.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.psi.XMLMutationUtils;
import com.sixrr.xrp.utils.XMLUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class EmptyTagInspection extends XmlSuppressableInspectionTool {
    @NotNull
    public String getGroupDisplayName(){
        return "XML Inspections";
    }

    @NotNull
    public String getDisplayName(){
        return "Unnecessary closing tag";
    }

    @NotNull
    @NonNls
    public String getShortName(){
        return "EmptyTag";
    }

    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean b){
        return new Visitor(problemsHolder);
    }

    private static class Visitor extends XmlElementVisitor
    {
        private final ProblemsHolder problemsHolder;

        Visitor(ProblemsHolder problemsHolder){
            this.problemsHolder = problemsHolder;
        }

        @Override public void visitXmlTag(XmlTag xmlTag){
            if(!XMLMutationUtils.tagIsWellFormed(xmlTag)){
                return ;
            }
            final XmlTag[] subTags = xmlTag.getSubTags();
            if(subTags != null && subTags.length != 0){
                return ;
            }
            if(!XMLMutationUtils.tagHasContents(xmlTag)){
                return ;
            }
            final String contents = XMLMutationUtils.calculateContentsString(xmlTag);
            if(! XMLUtil.isWhitespace(contents)){
                return;
            }
            problemsHolder.registerProblem(xmlTag,
                    "Unnecessary empty tag",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                    new CollapseTagFix());
        }
    }

    private static class CollapseTagFix implements LocalQuickFix
    {
        private static final Logger LOGGER = Logger.getInstance("EmptyTagInspection");

        @NotNull
        public String getName(){
            return "Collapse Empty Tag";
        }

        @NotNull
        public String getFamilyName(){
            return "";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor){
            final XmlTag tag = (XmlTag) problemDescriptor.getPsiElement();
            final int textLength = tag.getTextLength();
            final StringBuffer newTagBuffer = new StringBuffer(textLength);
            final PsiElement[] children = tag.getChildren();
            for(PsiElement child : children){
                if(child instanceof XmlToken){
                    final IElementType tokenType = ((XmlToken) child).getTokenType();
                    if(tokenType.equals(XmlTokenType.XML_TAG_END)){
                        break;
                    }
                }
                final String text = child.getText();
                newTagBuffer.append(text);
            }
            newTagBuffer.append("/>");
            final String newTag = newTagBuffer.toString();
            try{
                XMLMutationUtils.replaceTag(tag, newTag);
            } catch(IncorrectOperationException e){
                LOGGER.error(e);
            }
        }
    }
}
