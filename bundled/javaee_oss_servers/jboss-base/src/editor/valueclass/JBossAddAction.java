/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.editor.valueclass;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossCmpRoot;
import com.fuhrer.idea.jboss.model.JBossValueClass;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CustomShortcutSet;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xml.ui.DomCollectionControl;

import javax.swing.*;
import java.awt.event.*;

class JBossAddAction extends AnAction {

    private final JBossCmpRoot cmp;

    JBossAddAction(JBossCmpRoot cmp, JComponent component) {
        super(JavaeeBundle.getText("GenericAction.add"), null, DomCollectionControl.ADD_ICON);
        this.cmp = cmp;
        registerCustomShortcutSet(new CustomShortcutSet(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0)), component);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(cmp.getModule());
        TreeClassChooserFactory factory = TreeClassChooserFactory.getInstance(cmp.getManager().getProject());
        TreeClassChooser chooser = factory.createNoInnerClassesScopeChooser(JBossBundle.getText("JBossValueClassesEditor.dependent.value.class"), scope, null, null);
        chooser.showDialog();
        final PsiClass selected = chooser.getSelectedClass();
        if (selected != null) {
            new WriteCommandAction<Object>(cmp.getManager().getProject()) {
                @Override
                protected void run(Result<Object> result) throws Throwable {
                    JBossValueClass valueClass = cmp.getDependentValueClasses().addDependentValueClass();
                    valueClass.getClassName().setValue(selected);
                }
            }.execute();
        }
    }
}
