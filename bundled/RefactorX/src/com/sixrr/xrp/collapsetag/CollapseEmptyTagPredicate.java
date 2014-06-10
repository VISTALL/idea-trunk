package com.sixrr.xrp.collapsetag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.intention.PsiElementPredicate;
import com.sixrr.xrp.psi.XMLMutationUtils;
import com.sixrr.xrp.utils.XMLUtil;

class CollapseEmptyTagPredicate implements PsiElementPredicate {
    public boolean satisfiedBy(PsiElement element) {
        if (!(element instanceof XmlTag)) {
            return false;
        }

        if (!XMLMutationUtils.tagIsWellFormed((XmlTag) element)) {
            return false;
        }
        final XmlTag tag = (XmlTag) element;
        final XmlTag[] subTags = tag.getSubTags();
        if(subTags!=null && subTags.length!=0)
        {
            return false;
        }
        if(!XMLMutationUtils.tagHasContents(tag))
        {
            return false;
        }
        final String contents = XMLMutationUtils.calculateContentsString(tag);
        return XMLUtil.isWhitespace(contents);
    }

}
