package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.coldFusion.UI.CfmlIcons;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.psi.CfmlCompositeElement;
import com.intellij.coldFusion.model.psi.CfmlFunctionDefinition;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Lera Nikolaenko
 * Date: 19.02.2009
 */
public class CfmlStructureViewElement extends PsiTreeElementBase<PsiElement> {
    protected CfmlStructureViewElement(final PsiElement psiElement) {
        super(psiElement);
    }

    @NotNull
    public Collection<StructureViewTreeElement> getChildrenBase() {
        final PsiElement element = getElement();
        if (element instanceof CfmlFile) {
            CfmlCompositeElement firstChildTag = (CfmlCompositeElement) PsiTreeUtil.getChildOfType(element, CfmlTag.class);
            Collection<StructureViewTreeElement> tags = new LinkedList<StructureViewTreeElement>();
            if (firstChildTag != null) {
                PsiElement nextSibling = firstChildTag;
                do {
                    if (nextSibling instanceof CfmlTag) {
                        tags.add(new CfmlStructureViewElement(nextSibling));
                    }
                } while ((nextSibling = nextSibling.getNextSibling()) != null);
            }
            return tags;
        } else if (element instanceof CfmlTag) {
            if (((CfmlTag) element).getTagName().equals("cfscript")) {
                Collection<StructureViewTreeElement> tags = new LinkedList<StructureViewTreeElement>();
                CfmlCompositeElement functionDef = (CfmlCompositeElement) PsiTreeUtil.getChildOfType(element, CfmlFunctionDefinition.class);
                if (functionDef != null) {
                    PsiElement nextSibling = functionDef;
                    do {
                        if (nextSibling instanceof CfmlFunctionDefinition) {
                            tags.add(new CfmlStructureViewElement(nextSibling));
                        }
                    } while ((nextSibling = nextSibling.getNextSibling()) != null);
                }
                return tags;
            } else {
                PsiElement[] tags;
                tags = PsiTreeUtil.collectElements(element, new PsiElementFilter() {
                    public boolean isAccepted(PsiElement currentElement) {
                        return currentElement instanceof CfmlTag && currentElement.getParent() == element;
                    }
                }/*(CfmlCompositeElement) element).getgetSameTypeChildren()*/);
                return makeCollection(tags);
            }
        }
        return makeCollection(null);
    }

    public String getPresentableText() {
        PsiElement element = getElement();
        if (element instanceof CfmlTag) {
            return ((CfmlTag) element).getTagName();
        } else if (element instanceof CfmlFile) {
            return ((CfmlFile) element).getName();
        }
        return null;
    }

    private Collection<StructureViewTreeElement> makeCollection(PsiElement[] tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        return ContainerUtil.map2List(tags,
                new Function<PsiElement, StructureViewTreeElement>() {
                    public StructureViewTreeElement fun(PsiElement cfmlTag) {
                        return new CfmlStructureViewElement(cfmlTag);
                    }
                });
    }

    @Override
    public String getLocationString() {
        PsiElement element = getElement();
        if (element instanceof CfmlTag) {
            return ((CfmlTag) element).getArgumentsList();
        } /*else if (getElement() instanceof CfscriptFunction) {
            return ((CfscriptFunction) getElement()).getArgumentsDescr();
        }   */
        return null;
    }

    @Override
    public Icon getIcon(boolean open) {
        if (isMethodDefinition()) {
            return CfmlIcons.FUNCTIONTAG_ICON;
        } else if (getElement() instanceof CfmlTag) {
            return CfmlIcons.TAG_ICON;
        }
        return super.getIcon(open);
    }

    @Override
    public boolean canNavigate() {
        return super.canNavigate();    //Overriden method. Auto insertion
    }

    public boolean isMethodDefinition() {
        return ((getElement() instanceof CfmlTag) && "cffunction".equals(((CfmlTag) getElement()).getTagName()))/* ||
                (getElement() instanceof CfscriptFunction)*/;
    }
}
