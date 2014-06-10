/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.intellij.util.xml.ui.CommittablePanel;

import javax.swing.*;
import java.awt.*;

abstract class GeronimoRootEditor extends JavaeeBaseEditor {

    private final JPanel panel = new JPanel(new BorderLayout());

    GeronimoRootEditor(GeronimoCommonRoot root) {
        CommittablePanel editor = getRootEditor(root);
        addComponent(editor);
        panel.add(editor.getComponent(), BorderLayout.NORTH);
    }

    public JComponent getComponent() {
        return panel;
    }

    void addMainComponent(JComponent component) {
        panel.add(component, BorderLayout.CENTER);
    }

    private CommittablePanel getRootEditor(GeronimoCommonRoot root) {
        if (GeronimoUtil.isGeronimo10(root)) {
            return new GeronimoRootEditor10(root);
        } else {
            return new GeronimoRootEditor11(root);
        }
    }
}
