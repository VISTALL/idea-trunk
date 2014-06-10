/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.jboss.editor.valueclass.JBossValueClassesEditor;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.intellij.openapi.ui.Splitter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

class JBossCmpRootEditor extends JavaeeBaseEditor {

    private final Splitter splitter;

    JBossCmpRootEditor(@NotNull JBossCmpRoot cmp) {
        splitter = createSplitter(new JBossCmpDefaultsEditor(cmp), new JBossValueClassesEditor(cmp), false);
    }

    public JComponent getComponent() {
        return splitter;
    }
}
