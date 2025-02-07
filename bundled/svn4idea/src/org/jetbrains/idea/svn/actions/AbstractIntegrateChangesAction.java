package org.jetbrains.idea.svn.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesBrowserUseCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.SvnBundle;
import org.jetbrains.idea.svn.integrate.MergerFactory;
import org.jetbrains.idea.svn.integrate.SelectedCommittedStuffChecker;
import org.jetbrains.idea.svn.integrate.SvnIntegrateChangesActionPerformer;

public abstract class AbstractIntegrateChangesAction<T extends SelectedCommittedStuffChecker> extends AnAction implements DumbAware {
  private final boolean myCheckUseCase;

  protected AbstractIntegrateChangesAction(final boolean checkUseCase) {
    myCheckUseCase = checkUseCase;
  }

  @NotNull
  protected abstract MergerFactory createMergerFactory(final T checker);
  @NotNull
  protected abstract T createChecker();

  public final void update(final AnActionEvent e) {
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    final CommittedChangesBrowserUseCase useCase = (CommittedChangesBrowserUseCase) e.getDataContext().
      getData(CommittedChangesBrowserUseCase.CONTEXT_NAME);
    final Presentation presentation = e.getPresentation();

    if ((project == null) || (myCheckUseCase) && ((useCase == null) || (! CommittedChangesBrowserUseCase.COMMITTED.equals(useCase)))) {
      presentation.setEnabled(false);
      presentation.setVisible(false);
      return;
    }

    presentation.setText(SvnBundle.message("action.Subversion.integrate.changes.actionname"));
    presentation.setDescription(SvnBundle.message("action.Subversion.integrate.changes.description"));

    final T checker = createChecker();
    checker.execute(e);

    presentation.setVisible(true);
    presentation.setEnabled(checker.isValid());

    if (presentation.isVisible() && presentation.isEnabled() &&
        ProjectLevelVcsManager.getInstance(project).isBackgroundVcsOperationRunning()) {
      presentation.setEnabled(false);
    }

    updateWithChecker(e, checker);
  }

  protected void updateWithChecker(final AnActionEvent e, SelectedCommittedStuffChecker checker) {
  }

  @Nullable
  protected abstract String getSelectedBranchUrl(SelectedCommittedStuffChecker checker);
  @Nullable
  protected abstract String getSelectedBranchLocalPath(SelectedCommittedStuffChecker checker);
  @Nullable
  protected abstract String getDialogTitle();

  public void actionPerformed(final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Project project = PlatformDataKeys.PROJECT.getData(dataContext);

    final T checker = createChecker();
    checker.execute(e);

    if (! checker.isValid()) {
      Messages.showErrorDialog(SvnBundle.message("action.Subversion.integrate.changes.error.no.available.files.text"),
                               SvnBundle.message("action.Subversion.integrate.changes.messages.title"));
      return;
    }

    final SvnIntegrateChangesActionPerformer changesActionPerformer =
      new SvnIntegrateChangesActionPerformer(project, checker.getSameBranch(), createMergerFactory(checker));

    final String selectedBranchUrl = getSelectedBranchUrl(checker);
    if (selectedBranchUrl == null) {
      SelectBranchPopup.showForBranchRoot(project, checker.getRoot(), changesActionPerformer,
                                          SvnBundle.message("action.Subversion.integrate.changes.select.branch.text"));
    } else {
      changesActionPerformer.onBranchSelected(selectedBranchUrl, getSelectedBranchLocalPath(checker), getDialogTitle());
    }
  }
}
