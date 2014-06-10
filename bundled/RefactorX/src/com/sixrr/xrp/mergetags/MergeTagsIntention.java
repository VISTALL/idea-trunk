package com.sixrr.xrp.mergetags;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.IncorrectOperationException;
import com.sixrr.xrp.intention.Intention;
import com.sixrr.xrp.intention.PsiElementPredicate;
import static com.sixrr.xrp.psi.XMLMutationUtils.*;
import org.jetbrains.annotations.NotNull;

public class MergeTagsIntention extends Intention {

    @NotNull
    public String getText() {
        return "Merge Tags";
    }

    @NotNull
    public String getFamilyName() {
        return "Merge Tags";
    }

    @NotNull
    protected PsiElementPredicate getElementPredicate() {
        return new MergeTagsPredicate();
    }

    protected void processIntention(@NotNull PsiElement element) throws IncorrectOperationException {
        final XmlTag tag = (XmlTag) element;
        final XmlTag nextTag =
                (XmlTag) PsiTreeUtil.skipSiblingsForward(element,
                        new Class[]{PsiWhiteSpace.class, XmlText.class});
        final String newTag;
        if (tagHasContents(tag)) {
            newTag = calculateStartTagString(tag) +
                    calculateContentsString(tag) +
                    calculateContentsString(nextTag) +
                    calculateEndTagString(tag);
            replaceTag(tag, newTag);
        } else if(tagHasContents(nextTag)) {
            newTag = calculateStartTagString(nextTag) +
                    calculateContentsString(tag) +
                    calculateContentsString(nextTag) +
                    calculateEndTagString(nextTag);
            replaceTag(tag, newTag);
        }
        assert nextTag != null;
        nextTag.delete();

    }

}
