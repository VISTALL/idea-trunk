package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnVcs;
import org.jetbrains.idea.svn.ignore.IgnoreInfoGetter;
import org.jetbrains.idea.svn.ignore.SvnPropertyService;

public class RemoveFromIgnoreListAction extends BasicAction {
  private String myActionName;
  private final boolean myUseCommonExtension;
  private final IgnoreInfoGetter myInfoGetter;

  public RemoveFromIgnoreListAction(final boolean useCommonExtension, final IgnoreInfoGetter getter) {
    myUseCommonExtension = useCommonExtension;
    myInfoGetter = getter;
  }

  public void setActionText(final String name) {
    myActionName = name;
  }

  protected String getActionName(final AbstractVcs vcs) {
    return SvnBundle.message("action.name.undo.ignore.files");
  }

  public void update(final AnActionEvent e) {
    final Presentation presentation = e.getPresentation();
    presentation.setVisible(true);
    presentation.setEnabled(true);

    presentation.setText(myActionName);
    presentation.setDescription(SvnBundle.message("action.Subversion.UndoIgnore.description"));
  }

  protected boolean isEnabled(final Project project, final SvnVcs vcs, final VirtualFile file) {
    return true;
  }

  @Override
  protected void doVcsRefresh(Project project, VirtualFile file) {
    final VcsDirtyScopeManager vcsDirtyScopeManager = VcsDirtyScopeManager.getInstance(project);
    if (file.isDirectory()) {
      vcsDirtyScopeManager.dirDirtyRecursively(file);
    } else {
      vcsDirtyScopeManager.fileDirty(file);
    }
  }

  protected boolean needsFiles() {
    return true;
  }

  protected void perform(final Project project, final SvnVcs activeVcs, final VirtualFile file, final DataContext context)
      throws VcsException {

  }

  protected void batchPerform(final Project project, final SvnVcs activeVcs, final VirtualFile[] file, final DataContext context)
      throws VcsException {
    SvnPropertyService.doRemoveFromIgnoreProperty(activeVcs, project, myUseCommonExtension, file, myInfoGetter);
  }

  protected boolean isBatchAction() {
    return true;
  }
}
