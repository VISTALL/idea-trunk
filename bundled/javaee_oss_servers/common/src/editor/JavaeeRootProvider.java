/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.editor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.model.xml.JavaeeDomModelElement;
import com.intellij.javaee.module.view.AbstractJavaeeFileEditorProvider;
import com.intellij.util.xml.ui.*;
import org.jetbrains.annotations.NotNull;

public abstract class JavaeeRootProvider<T extends JavaeeDomModelElement, F extends JavaeeFacet> extends AbstractJavaeeFileEditorProvider<T, F> {

    private final double weight;

    protected JavaeeRootProvider(Class<T> type, FacetTypeId<F> facet, double weight) {
        super(type, facet);
        this.weight = weight;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    protected String getTitle() {
        return JavaeeBundle.getText("Editor.title", JavaeeIntegration.getInstance().getName());
    }

    @Override
    @NotNull
    protected PerspectiveFileEditor createEditor(@NotNull T root, @NotNull F facet) {
        CaptionComponent caption = new CaptionComponent(getTitle(), JavaeeIntegration.getInstance().getBigIcon());
        caption = DomUIFactory.getDomUIFactory().addErrorPanel(caption, root);
        return DomFileEditor.createDomFileEditor(caption.getText(), root, caption, createPanel(root, facet));
    }

    protected abstract CommittablePanel createPanel(@NotNull T root, @NotNull F facet);
}
