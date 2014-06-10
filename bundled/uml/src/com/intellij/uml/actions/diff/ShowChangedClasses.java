/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.uml.actions.diff;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.LocalChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.uml.UmlClassDiagramFileEditor;
import com.intellij.uml.UmlVirtualFileSystem;
import com.intellij.uml.utils.UmlBundle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class ShowChangedClasses extends AnAction {
  public void update(final AnActionEvent e) {
    final Project project = DataKeys.PROJECT.getData(e.getDataContext());
    boolean enabled = project != null && ProjectLevelVcsManager.getInstance(project).getAllActiveVcss().length > 0;
    e.getPresentation().setVisible(enabled);
    e.getPresentation().setEnabled(enabled);
  }

  public void actionPerformed(AnActionEvent e) {
    final Project project = DataKeys.PROJECT.getData(e.getDataContext());
    final List<LocalChangeList> changeLists = new ArrayList<LocalChangeList>();
    final ChangeList[] selected = VcsDataKeys.CHANGE_LISTS.getData(e.getDataContext());
    if (selected != null && selected.length > 0) {
      for (ChangeList changeList : selected) {
        if (changeList instanceof LocalChangeList) {
          changeLists.add((LocalChangeList)changeList);
        }
      }
    }
    final ChangeListManager listManager = ChangeListManager.getInstance(project);
    if (changeLists.size() == 0) {
      changeLists.addAll(listManager.getChangeLists());
    }
    if (changeLists.size() > 1) {
      String[] names = new String[changeLists.size()];
      for (int i = 0; i < names.length; i++) {
        names[i] = changeLists.get(i).getName();
      }
      final JList jList = new JList(names);
      final PopupChooserBuilder popupBuilder = JBPopupFactory.getInstance().createListPopupBuilder(jList);
      final JBPopup popup = popupBuilder
        .setTitle(UmlBundle.message("select.change.list"))
        .setResizable(false)
        .setMovable(false)
        .setItemChoosenCallback(new Runnable() {
          public void run() {
            final int index = jList.getSelectedIndex();
            if (0 <= index && index < changeLists.size()) {
              showChangesInEditor(changeLists.get(index), project);
            }
          }
        }).createPopup();
      if ("ChangesViewToolbar".equals(e.getPlace())) {
        popup.showUnderneathOf(e.getInputEvent().getComponent());
      } else {
        popup.showInBestPositionFor(e.getDataContext());
      }
    } else {
      final LocalChangeList changeList = changeLists.size() == 1 ? changeLists.get(0) : listManager.getDefaultChangeList();
      showChangesInEditor(changeList, project);
    }
  }

  private static void showChangesInEditor(LocalChangeList changeList, Project project) {
    if (changeList == null) return;
    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(UmlVirtualFileSystem.getFileUrlByChangeList(changeList));

    if (virtualFile != null) {
      UmlVirtualFileSystem.setInitialized(virtualFile);
      final FileEditor[] fileEditors = FileEditorManager.getInstance(project).openFile(virtualFile, true);
      for (FileEditor fileEditor : fileEditors) {
        if (fileEditor instanceof UmlClassDiagramFileEditor) {
          UmlClassDiagramFileEditor editor = (UmlClassDiagramFileEditor)fileEditor;
          editor.getBuilder().getView().fitContent();
        }
      }
    }
  }
}
