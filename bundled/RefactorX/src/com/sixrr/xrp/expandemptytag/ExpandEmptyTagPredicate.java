package com.sixrr.xrp.expandemptytag;

import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.sixrr.xrp.intention.PsiElementPredicate;
import com.sixrr.xrp.psi.XMLMutationUtils;

class ExpandEmptyTagPredicate implements PsiElementPredicate {
    public boolean satisfiedBy(PsiElement element) {
        if (!(element instanceof XmlTag)) {
            return false;
        }
        if(!XMLMutationUtils.tagIsWellFormed((XmlTag) element))
        {
            return false;
        }
        return !XMLMutationUtils.tagHasContents((XmlTag) element);
    }
}
