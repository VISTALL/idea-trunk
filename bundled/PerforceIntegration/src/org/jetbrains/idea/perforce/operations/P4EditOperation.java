package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.FStat;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceCachingContentRevision;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;

/**
 * @author yole
 */
public class P4EditOperation extends VcsOperationOnPath {
  @NonNls private static final String CANNOT_FIND_ERROR = "the system cannot find";
  @NonNls private static final String CANNOT_FIND_ERROR_2 = "no such file or directory";

  @SuppressWarnings({"UnusedDeclaration"})
  public P4EditOperation() {
  }

  public P4EditOperation(String changeList, final VirtualFile file) {
    super(changeList, file.getPath());
  }

  public P4EditOperation(String changeList, final String path) {
    super(changeList, path);
  }

  public void execute(final Project project) throws VcsException {
    final Ref<PerforceVcs> vcs = new Ref<PerforceVcs>();
    final Ref<PerforceRunner> runner = new Ref<PerforceRunner>();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        if (project.isDisposed()) return;
        vcs.set(PerforceVcs.getInstance(project));
        runner.set(PerforceRunner.getInstance(project));
      }
    });

    if (vcs.isNull()) return;
    try {
      final P4File p4File = P4File.createInefficientFromLocalPath(myPath);
      FStat p4FStat = vcs.get().getFstatSafe(p4File);
      if (p4FStat == null) return;
      if ((p4FStat.status == FStat.STATUS_NOT_ADDED || p4FStat.status == FStat.STATUS_ONLY_LOCAL) &&
          p4FStat.local != FStat.LOCAL_BRANCHING) {
        throw new VcsException(
          PerforceBundle.message("confirmation.text.auto.edit.file.not.registered.on.server", p4File.getLocalPath()));
      }
      else if (p4FStat.status == FStat.STATUS_DELETED) {
        throw new VcsException(PerforceBundle.message("exception.text.file.deleted.from.server.cannot.edit", p4File.getLocalPath()));
      }
      else if (p4FStat.local != FStat.LOCAL_CHECKED_IN && p4FStat.local != FStat.LOCAL_INTEGRATING &&
               p4FStat.local != FStat.LOCAL_BRANCHING) {
        throw new VcsException(
          PerforceBundle.message("exception.text.file..should.not.be.readonly.cannot.edit", p4File.getLocalPath()));
      }

      long changeListNumber = getPerforceChangeList(project, p4File);
      runner.get().edit(p4File, changeListNumber);
    }
    catch (VcsException e) {
      // check if file was deleted while we were waiting to perform background edit
      final String message = e.getMessage().toLowerCase();
      if (!message.contains(CANNOT_FIND_ERROR) && !message.contains(CANNOT_FIND_ERROR_2)) {
        throw e;
      }
    }
    final FilePath filePath = getFilePath();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        if (!project.isDisposed()) {
          VcsDirtyScopeManager.getInstance(project).fileDirty(filePath);
        }
      }
    });
    VirtualFile vFile = filePath.getVirtualFile();
    if (vFile != null) {
      vFile.refresh(true, false);
      vcs.get().asyncEditCompleted(vFile);
    }
  }

  @Override
  public Change getChange(final Project project) {
    FilePath path = getFilePath();
    ContentRevision beforeRevision = PerforceCachingContentRevision.create(project, path, -1);
    ContentRevision afterRevision = CurrentContentRevision.create(path);
    return new Change(beforeRevision, afterRevision);
  }

  public void prepareOffline() {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(myPath);
    if (vFile != null) {
      PerforceCachingContentRevision.saveCurrentContent(vFile);
    }
  }
}