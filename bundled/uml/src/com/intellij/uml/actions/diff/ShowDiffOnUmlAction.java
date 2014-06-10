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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.history.VcsFileRevision;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Konstantin Bulenkov
 */
public class ShowDiffOnUmlAction extends AnAction {
  public void actionPerformed(AnActionEvent e) {
    final VcsFileRevision[] revisions = getRevisions(e);
    final Project project = DataKeys.PROJECT.getData(e.getDataContext());
    if (revisions == null || project == null) return;
    final VcsFileRevision vcsFileRevision = revisions[0];
    try {
      vcsFileRevision.loadContent();
      final byte[] content = vcsFileRevision.getContent();
      System.out.println(new String(content));
    }
    catch (VcsException e1) {
      e1.printStackTrace();
    }
    catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(getRevisions(e) != null);
  }

  @Nullable
  private static VcsFileRevision[] getRevisions(AnActionEvent e) {
    final VcsFileRevision[] revisions = VcsDataKeys.VCS_FILE_REVISIONS.getData(e.getDataContext());
    return revisions == null || revisions.length == 0 || revisions.length > 2 ? null : revisions;
  }
}
