/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.loadgroup;

import com.fuhrer.idea.javaee.util.EditableTreeColumnInfo;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.openapi.project.Project;

class JBossLoadGroupColumn extends EditableTreeColumnInfo<JBossLoadGroup> {

    JBossLoadGroupColumn(Project project) {
        super(project, JBossLoadGroup.class);
    }

    @Override
    protected void storeValue(JBossLoadGroup element, String value) {
        element.getLoadGroupName().setValue(value);
    }
}
