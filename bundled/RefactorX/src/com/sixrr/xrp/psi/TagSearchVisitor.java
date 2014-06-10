package com.sixrr.xrp.psi;

import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementVisitor;
import com.intellij.psi.impl.source.DummyHolderElement;
import com.intellij.psi.xml.XmlTag;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ReturnOfCollectionOrArrayField"})
public class TagSearchVisitor extends XmlRecursiveElementVisitor {
    private final String tagName;
    private final List<XmlTag> tags= new ArrayList<XmlTag>();

    public TagSearchVisitor(String tagName) {
        super();
        this.tagName = tagName;
    }

    @Override public void visitXmlTag(XmlTag tag) {
        final PsiFile psiFile = tag.getContainingFile();
        if(psiFile instanceof DummyHolderElement)
        {
            return;
        }
        super.visitXmlTag(tag);
        final String name = tag.getName();
        if(name!=null && name.equals(tagName))
        {
            tags.add(tag);
        }
    }

    public List<XmlTag> getTags() {
        return tags;
    }
}
