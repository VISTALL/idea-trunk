package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 27.10.2008
 */
public class CfmlTag extends CfmlCompositeElement {
    public CfmlTag(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement place) {
        if ("cfscript".equals(getTagName().toLowerCase())) {

        }

        /*
        CfmlNamedAttribute attribute = findChildByClass(CfmlNamedAttribute.class);
        if (attribute != null) {
            CfmlPsiUtil.processDeclarations(processor, , attribute);
        }
        if ()
        CfmlTag[] tags = findChildrenByClass(CfmlTag.class);
        for (tags)
        */
        return super.processDeclarations(processor, state, lastParent, place);
    }

    @NotNull
    public String getTagName() {
        PsiElement pe = findChildByType(CfmlTokenTypes.CF_TAG_NAME);
        return pe == null ? "" : pe.getText();
    }

    @Override
    public String getName() {
        return getTagName();
    }

    // needed for structure view
    public String getArgumentsList() {
        int currentStartOffset = getTextRange().getStartOffset();
        PsiElement tagName = this.findChildByType(CfmlTokenTypes.CF_TAG_NAME);
        if (tagName == null) {
            return null;
        }
        PsiElement closer = this.findChildByType(CfmlTokenTypes.CLOSER);
        PsiElement rightBracket = this.findChildByType(CfmlTokenTypes.R_ANGLEBRACKET);
        int endOffset;
        if (rightBracket != null) {
            endOffset = rightBracket.getTextRange().getStartOffset();
        } else if (closer != null) {
            endOffset = closer.getTextRange().getStartOffset();
        } else {
            return null;
        }
        int startOffset = tagName.getTextRange().getEndOffset();
        return getText().substring(startOffset - currentStartOffset, endOffset - currentStartOffset);
    }

    public boolean isDeclarativeInside() {
        return findChildByClass(CfmlNamedAttribute.class) != null || "cfset".equals(getName());
    }

    public PsiElement getDeclarativeElement() {
        if ("cfset".equals(getName())) {
            return findChildByClass(CfmlAssignment.class);
        }
        return findChildByClass(CfmlNamedAttribute.class);
    }
}
