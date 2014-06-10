/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.model.GlassfishPrincipal;
import com.fuhrer.idea.javaee.JavaeeBundle;
import com.intellij.javaee.module.view.nodes.JavaeeObjectDescriptor;

import javax.swing.*;

class GlassfishPrincipalNode extends JavaeeObjectDescriptor<GlassfishPrincipal> {

    GlassfishPrincipalNode(GlassfishSecurityRoleNode parent, GlassfishPrincipal element) {
        super(element, parent, null);
    }

    @Override
    protected String getNewNodeText() {
        return getElement().getValue();
    }

    @Override
    protected Icon getNewOpenIcon() {
        return JavaeeBundle.getIcon("/fileTypes/custom.png");
    }

    @Override
    protected Icon getNewClosedIcon() {
        return getNewOpenIcon();
    }
}
