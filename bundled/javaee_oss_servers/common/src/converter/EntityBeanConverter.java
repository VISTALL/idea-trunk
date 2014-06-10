/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.converter;

import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.model.xml.ejb.EjbJar;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class EntityBeanConverter extends ResolvingConverter<EntityBean> {

    @Override
    @Nullable
    public EntityBean fromString(String value, ConvertContext context) {
        return ElementPresentationManager.findByName(getVariants(context), value);
    }

    @Override
    @Nullable
    public String toString(EntityBean value, ConvertContext context) {
        return (value != null) ? value.getEjbName().getValue() : null;
    }

    @Override
    @NotNull
    public Collection<EntityBean> getVariants(ConvertContext context) {
        EjbFacet facet = JavaeeFacetUtil.getInstance().getJavaeeFacet(context, EjbFacet.ID);
        if (facet != null) {
            EjbJar root = facet.getXmlRoot();
            if (root != null) {
                return root.getEnterpriseBeans().getEntities();
            }
        }
        return Collections.emptyList();
    }
}
