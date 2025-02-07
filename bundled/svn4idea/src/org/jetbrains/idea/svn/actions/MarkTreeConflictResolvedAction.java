package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.BackgroundFromStartOption;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.svn.ConflictedSvnChange;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.SvnVcs;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MarkTreeConflictResolvedAction extends AnAction {
  private final static String myText = SvnBundle.message("action.mark.tree.conflict.resolved.text");

  @Override
  public void update(AnActionEvent e) {
    final MyChecker checker = new MyChecker(e);
    e.getPresentation().setVisible(checker.isEnabled());
    e.getPresentation().setEnabled(checker.isEnabled());
    e.getPresentation().setText(myText);
  }

  private static class MyChecker {
    private boolean myEnabled;
    private ConflictedSvnChange myChange;
    private final Project myProject;

    public MyChecker(final AnActionEvent e) {
      final DataContext dc = e.getDataContext();
      myProject = PlatformDataKeys.PROJECT.getData(dc);
      final Change[] changes = VcsDataKeys.CHANGE_LEAD_SELECTION.getData(dc);

      if ((myProject == null) || (changes == null) || (changes.length != 1)) return;

      final Change change = changes[0];
      myEnabled = (change instanceof ConflictedSvnChange) && ((ConflictedSvnChange) change).getConflictState().isTree();
      if (myEnabled) {
        myChange = (ConflictedSvnChange) change;
      }
    }

    public boolean isEnabled() {
      return myEnabled;
    }

    public ConflictedSvnChange getChange() {
      return myChange;
    }

    public Project getProject() {
      return myProject;
    }
  }

  public void actionPerformed(AnActionEvent e) {
    final MyChecker checker = new MyChecker(e);
    if (! checker.isEnabled()) return;

    final String markText = SvnBundle.message("action.mark.tree.conflict.resolved.confirmation.title");
    final int result = Messages.showYesNoDialog(checker.getProject(),
                                                SvnBundle.message("action.mark.tree.conflict.resolved.confirmation.text"), markText,
                                                Messages.getQuestionIcon());
    if (result == DialogWrapper.OK_EXIT_CODE) {
      final Ref<VcsException> exception = new Ref<VcsException>();
      ProgressManager.getInstance().run(new Task.Backgroundable(checker.getProject(), markText, true, BackgroundFromStartOption.getInstance()) {
        public void run(@NotNull ProgressIndicator indicator) {
          final ConflictedSvnChange change = checker.getChange();
          final FilePath path = change.getTreeConflictMarkHolder();
          final SVNWCClient client = SvnVcs.getInstance(checker.getProject()).createWCClient();
          try {
            client.doResolve(path.getIOFile(), SVNDepth.EMPTY, false, false, true, SVNConflictChoice.MERGED);
          }
          catch (SVNException e1) {
            exception.set(new VcsException(e1));
          }
          VcsDirtyScopeManager.getInstance(checker.getProject()).filePathsDirty(getDistinctFiles(change), null);
        }
      });
      if (! exception.isNull()) {
        AbstractVcsHelper.getInstance(checker.getProject()).showError(exception.get(), markText);
      }
    }
  }

  private Collection<FilePath> getDistinctFiles(final Change change) {
    final List<FilePath> result = new ArrayList<FilePath>(2);
    if (change.getBeforeRevision() != null) {
      result.add(change.getBeforeRevision().getFile());
    }
    if (change.getAfterRevision() != null) {
      if ((change.getBeforeRevision() == null) ||
          ((change.getBeforeRevision() != null) && (change.isMoved() || change.isRenamed()))) {
        result.add(change.getAfterRevision().getFile());
      }
    }
    return result;
  }
}
