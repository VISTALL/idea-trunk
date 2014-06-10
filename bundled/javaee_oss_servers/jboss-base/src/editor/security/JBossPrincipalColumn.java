/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.security;

import com.fuhrer.idea.javaee.util.EditableTreeColumnInfo;
import com.fuhrer.idea.jboss.model.JBossPrincipal;
import com.intellij.openapi.project.Project;

class JBossPrincipalColumn extends EditableTreeColumnInfo<JBossPrincipal> {

    JBossPrincipalColumn(Project project) {
        super(project, JBossPrincipal.class);
    }

    @Override
    protected void storeValue(JBossPrincipal element, String value) {
        element.setValue(value);
    }
}
