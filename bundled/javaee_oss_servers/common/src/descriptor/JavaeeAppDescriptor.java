/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.facet.JavaeeFacet;

class JavaeeAppDescriptor extends JavaeeDescriptor {

    JavaeeAppDescriptor() {
        super(JavaeeBundle.getIcon("/resources/app.png"));
    }

    @Override
    String getTitle(JavaeeIntegration integration) {
        return JavaeeBundle.getText("AppDescriptor.title", integration.getName());
    }

    @Override
    FacetTypeId<? extends JavaeeFacet> getFacetType() {
        return JavaeeApplicationFacet.ID;
    }
}
