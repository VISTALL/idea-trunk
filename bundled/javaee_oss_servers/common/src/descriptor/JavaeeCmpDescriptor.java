/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.facet.JavaeeFacet;

class JavaeeCmpDescriptor extends JavaeeDescriptor {

    JavaeeCmpDescriptor() {
        super(JavaeeBundle.getIcon("/resources/cmp.png"));
    }

    @Override
    String getTitle(JavaeeIntegration integration) {
        return JavaeeBundle.getText("CmpDescriptor.title", integration.getName());
    }

    @Override
    FacetTypeId<? extends JavaeeFacet> getFacetType() {
        return EjbFacet.ID;
    }
}
