/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.editor.security;

import com.fuhrer.idea.glassfish.model.GlassfishPrincipal;
import com.fuhrer.idea.javaee.util.EditableTreeColumnInfo;
import com.intellij.openapi.project.Project;

class GlassfishPrincipalColumn extends EditableTreeColumnInfo<GlassfishPrincipal> {

    GlassfishPrincipalColumn(Project project) {
        super(project, GlassfishPrincipal.class);
    }

    @Override
    protected void storeValue(GlassfishPrincipal element, String value) {
        element.setValue(value);
    }
}
