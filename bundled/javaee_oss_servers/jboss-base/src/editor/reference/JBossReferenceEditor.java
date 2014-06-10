/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.reference;

import com.fuhrer.idea.javaee.editor.JavaeeBaseEditor;
import com.fuhrer.idea.javaee.editor.JavaeeSection;
import com.fuhrer.idea.javaee.editor.JavaeeSectionView;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossReferenceHolder;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class JBossReferenceEditor extends JavaeeBaseEditor {

    private final JPanel panel = new JPanel();

    public JBossReferenceEditor(JndiEnvironmentRefsGroup source, JBossReferenceHolder target) {
        panel.setBorder(BorderFactory.createTitledBorder(JBossBundle.getText("JBossReferenceEditor.title")));
        JavaeeSection<?>[] sections = {
                new JBossEjbRefSection(source, target),
                new JBossEjbLocalRefSection(source, target),
                new JBossResourceRefSection(source, target),
                new JBossResourceEnvRefSection(source, target),
                new JBossMessageDestinationRefSection(source, target),
        };
        JavaeeSectionView view = new JavaeeSectionView(source.getManager().getProject(), JBossBundle.getText("JBossReferenceEditor.empty"), sections);
        Border outer = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        Border inner = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black);
        view.getComponent().setBorder(BorderFactory.createCompoundBorder(outer, inner));
        addContent(panel, view);
    }

    public JComponent getComponent() {
        return panel;
    }
}
