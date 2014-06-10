/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.intellij.facet.FacetManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaeeFileDescription<T> extends DomFileDescription<T> {

    private final JavaeeDescriptor descriptor;

    public JavaeeFileDescription(Class<T> type, @NonNls String root, JavaeeDescriptor descriptor) {
        super(type, root);
        this.descriptor = descriptor;
    }

    @Override
    protected void initializeFileDescription() {
    }

    @Override
    public boolean isMyFile(@NotNull XmlFile file, @Nullable Module module) {
        return checkMatchingNamespace(file) && checkMatchingFacet(file);
    }

    private boolean checkMatchingNamespace(XmlFile file) {
        if (file.isValid()) {
            XmlDocument document = file.getDocument();
            if (document != null) {
                XmlTag tag = document.getRootTag();
                return (tag != null) && getRootTagName().equals(tag.getLocalName()) && descriptor.hasNamespace(tag.getNamespace());
            }
        }
        return false;
    }

    private boolean checkMatchingFacet(XmlFile file) {
        Module module = ModuleUtil.findModuleForPsiElement(file);
        return (module != null) && !FacetManager.getInstance(module).getFacetsByType(descriptor.getFacetType()).isEmpty();
    }
}
