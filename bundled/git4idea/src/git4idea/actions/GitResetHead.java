/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package git4idea.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vfs.VirtualFile;
import git4idea.commands.GitHandlerUtil;
import git4idea.commands.GitLineHandler;
import git4idea.i18n.GitBundle;
import git4idea.ui.GitResetDialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * The reset action
 */
public class GitResetHead extends GitRepositoryAction {
  /**
   * {@inheritDoc}
   */
  @NotNull
  protected String getActionName() {
    return GitBundle.getString("reset.action.name");
  }

  /**
   * {@inheritDoc}
   */
  protected void perform(@NotNull Project project,
                         @NotNull List<VirtualFile> gitRoots,
                         @NotNull VirtualFile defaultRoot,
                         Set<VirtualFile> affectedRoots,
                         List<VcsException> exceptions) throws VcsException {
    GitResetDialog d = new GitResetDialog(project, gitRoots, defaultRoot);
    d.show();
    if (!d.isOK()) {
      return;
    }
    GitLineHandler h = d.handler();
    affectedRoots.add(d.getGitRoot());
    GitHandlerUtil.doSynchronously(h, GitBundle.getString("resetting.title"), h.printableCommandLine());
  }
}
