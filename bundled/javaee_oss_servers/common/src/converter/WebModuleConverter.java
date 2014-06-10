/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.converter;

import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class WebModuleConverter extends ResolvingConverter<JavaeeModule> {

    @Override
    @Nullable
    public JavaeeModule fromString(String value, ConvertContext context) {
        if (value != null) {
            for (JavaeeModule module : getVariants(context)) {
                if (value.equals(module.getWeb().getWebUri().getValue())) {
                    return module;
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public String toString(JavaeeModule value, ConvertContext context) {
        return (value != null) ? value.getWeb().getWebUri().getValue() : null;
    }

    @Override
    @NotNull
    public Collection<? extends JavaeeModule> getVariants(ConvertContext context) {
        Collection<JavaeeModule> list = new ArrayList<JavaeeModule>();
        JavaeeApplicationFacet facet = JavaeeFacetUtil.getInstance().getJavaeeFacet(context, JavaeeApplicationFacet.ID);
        if (facet != null) {
            JavaeeApplication root = facet.getRoot();
            if (root != null) {
                for (JavaeeModule module : root.getModules()) {
                    if (module.getWeb().getXmlTag() != null) {
                        list.add(module);
                    }
                }
            }
        }
        return list;
    }
}
