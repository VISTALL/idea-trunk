/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.web.facet.WebFacet;
import org.jetbrains.annotations.NonNls;

class JavaeeWebDescriptor extends JavaeeDescriptor {

    JavaeeWebDescriptor() {
        super(JavaeeBundle.getIcon("/resources/web.png"));
    }

    @Override
    String getTitle(JavaeeIntegration integration) {
        return JavaeeBundle.getText("WebDescriptor.title", integration.getName());
    }

    @Override
    FacetTypeId<? extends JavaeeFacet> getFacetType() {
        return WebFacet.ID;
    }

    @Override
    @NonNls
    String getPath() {
        return "WEB-INF";
    }
}
