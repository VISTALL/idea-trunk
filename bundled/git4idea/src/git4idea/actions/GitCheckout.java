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
import git4idea.checkout.GitCheckoutDialog;
import git4idea.commands.GitHandlerUtil;
import git4idea.commands.GitLineHandler;
import git4idea.commands.GitSimpleHandler;
import git4idea.i18n.GitBundle;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Git "checkout" action
 */
public class GitCheckout extends GitRepositoryAction {

  /**
   * {@inheritDoc}
   */
  @Override
  @NotNull
  protected String getActionName() {
    return GitBundle.getString("checkout.action.name");
  }

  /**
   * {@inheritDoc}
   */
  protected void perform(@NotNull final Project project,
                         @NotNull final List<VirtualFile> gitRoots,
                         @NotNull final VirtualFile defaultRoot,
                         final Set<VirtualFile> affectedRoots,
                         final List<VcsException> exceptions) throws VcsException {
    GitCheckoutDialog dialog = new GitCheckoutDialog(project, gitRoots, defaultRoot);
    dialog.show();
    if (!dialog.isOK()) {
      return;
    }
    GitSimpleHandler branch = dialog.createBranchHandler();
    if (branch != null) {
      branch.run();
    }
    GitLineHandler checkout = dialog.checkoutHandler();
    affectedRoots.add(dialog.gitRoot());
    try {
      GitHandlerUtil.doSynchronously(checkout, GitBundle.message("checking.out", dialog.getSourceBranch()), "git checkout");
    }
    finally {
      exceptions.addAll(checkout.errors());
    }
  }
}
