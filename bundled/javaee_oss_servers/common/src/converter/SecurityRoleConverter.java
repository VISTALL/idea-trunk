/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.converter;

import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.facet.JavaeeFacetUtil;
import com.intellij.javaee.model.xml.SecurityRole;
import com.intellij.javaee.model.xml.application.JavaeeApplication;
import com.intellij.javaee.model.xml.ejb.EjbJar;
import com.intellij.javaee.model.xml.web.WebApp;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class SecurityRoleConverter extends ResolvingConverter<SecurityRole> {

    @Override
    @Nullable
    public SecurityRole fromString(String value, ConvertContext context) {
        return ElementPresentationManager.findByName(getVariants(context), value);
    }

    @Override
    @Nullable
    public String toString(SecurityRole value, ConvertContext context) {
        return (value != null) ? value.getRoleName().getValue() : null;
    }

    @Override
    @NotNull
    public Collection<SecurityRole> getVariants(ConvertContext context) {
        Collection<SecurityRole> variants = Collections.emptyList();
        JavaeeFacet facet = JavaeeFacetUtil.getInstance().getJavaeeFacet(context);
        if (facet == null) {
            // ignore...
        } else if (JavaeeApplicationFacet.ID.equals(facet.getTypeId())) {
            JavaeeApplication root = ((JavaeeApplicationFacet) facet).getRoot();
            if (root != null) {
                variants = root.getSecurityRoles();
            }
        } else if (EjbFacet.ID.equals(facet.getTypeId())) {
            EjbJar root = ((EjbFacet) facet).getXmlRoot();
            if (root != null) {
                variants = root.getAssemblyDescriptor().getSecurityRoles();
            }
        } else if (WebFacet.ID.equals(facet.getTypeId())) {
            WebApp root = ((WebFacet) facet).getRoot();
            if (root != null) {
                variants = root.getSecurityRoles();
            }
        }
        return variants;
    }
}
