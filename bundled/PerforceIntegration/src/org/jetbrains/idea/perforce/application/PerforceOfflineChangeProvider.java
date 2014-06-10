package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class PerforceOfflineChangeProvider implements ChangeProvider {
  private final Project myProject;

  public PerforceOfflineChangeProvider(final Project project) {
    myProject = project;
  }

  public void getChanges(VcsDirtyScope dirtyScope, ChangelistBuilder builder, ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    final VcsOperationLog opLog = VcsOperationLog.getInstance(myProject);
    Map<String, String> reopenedPaths = opLog.getReopenedPaths();
    List<LastSuccessfulUpdateTracker.PersistentChangeList> changeLists = LastSuccessfulUpdateTracker.getInstance(myProject).getChangeLists();
    if (changeLists != null) {
      for(LastSuccessfulUpdateTracker.PersistentChangeList changeList: changeLists) {
        for(LastSuccessfulUpdateTracker.ChangedFile file: changeList.files) {
          String changeListName = changeList.name;
          if (file.beforePath != null && reopenedPaths.containsKey(file.beforePath)) {
            changeListName = reopenedPaths.get(file.beforePath);
          }
          else if (file.afterPath != null && reopenedPaths.containsKey(file.afterPath)) {
            changeListName = reopenedPaths.get(file.afterPath);
          }
          if (changeListName == null) {
            continue;
          }

          FilePath beforePath = createFilePath(file.beforePath);
          FilePath afterPath = createFilePath(file.afterPath);
          if (isInScope(dirtyScope, beforePath, afterPath)) {
            ContentRevision beforeRevision = null;
            ContentRevision afterRevision = null;
            if (beforePath != null) {
              beforeRevision = PerforceCachingContentRevision.create(myProject, beforePath,
                                                                     afterPath != null ? afterPath : beforePath,
                                                                     file.beforeRevision);
            }
            if (afterPath != null) {
              afterRevision = CurrentContentRevision.create(afterPath);
            }
            builder.processChangeInList(new Change(beforeRevision, afterRevision), changeListName, PerforceVcs.getKey());
          }
        }
      }
    }

    List<VcsOperation> list = opLog.getPendingOperations();
    for(VcsOperation op: list) {
      Change c = op.getChange(myProject);
      if (c != null && isInScope(dirtyScope, ChangesUtil.getBeforePath(c), ChangesUtil.getAfterPath(c))) {
        builder.processChangeInList(c, op.getChangeList(), PerforceVcs.getKey());
      }
    }
  }

  private static boolean isInScope(final VcsDirtyScope dirtyScope, final FilePath beforePath, final FilePath afterPath) {
    return (beforePath != null && dirtyScope.belongsTo(beforePath)) || (afterPath != null && dirtyScope.belongsTo(afterPath));
  }

  @Nullable
  private static FilePath createFilePath(final String beforePath) {
    if (beforePath == null) return null;
    return VcsContextFactory.SERVICE.getInstance().createFilePathOn(new File(beforePath), false);
  }

  public boolean isModifiedDocumentTrackingRequired() {
    return false;
  }

  public void doCleanup(final List<VirtualFile> files) {
  }
}