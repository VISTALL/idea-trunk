package org.jetbrains.idea.perforce.operations;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.openapi.vcs.changes.CurrentContentRevision;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.ActionWithTempFile;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.application.PerforceClient;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * @author yole
 */
public class P4MoveRenameOperation extends VcsOperation {
  private static final int CYCLE = 3;
  private static final int DOUBLE_RENAME = 2;
  private static final int SIMPLE = 1;

  private String myOldPath;
  private String myNewPath;

  public P4MoveRenameOperation() {
  }

  public P4MoveRenameOperation(String changeList, final String oldPath, final String newPath) {
    super(changeList);
    myOldPath = oldPath;
    myNewPath = newPath;
  }

  public String getOldPath() {
    return myOldPath;
  }

  public void setOldPath(final String oldPath) {
    myOldPath = oldPath;
  }

  public String getNewPath() {
    return myNewPath;
  }

  public void setNewPath(final String newPath) {
    myNewPath = newPath;
  }

  public void execute(final Project project) throws VcsException {
    P4File oldP4File = P4File.createInefficientFromLocalPath(myOldPath);
    final P4File newP4File = P4File.createInefficientFromLocalPath(myNewPath);
    final P4Connection oldConnection = PerforceConnectionManager.getInstance(project).getConnectionForFile(oldP4File);
    final P4Connection newConnection = PerforceConnectionManager.getInstance(project).getConnectionForFile(newP4File);
    final PerforceRunner runner = PerforceRunner.getInstance(project);

    final RefreshForVcs refreshWorker = new RefreshForVcs();
    try {
      final String client = runner.getClient(newConnection);
      if (client == null) {  return;  }
      FStat oldfstat = oldP4File.getFstat(project, false);

      // todo 'p4 move' does actual move itself. this means that we should another type of file events listener for P4
      // todo - maybe it is not worth doing?
      /*final ClientVersion clientVersion = PerforceManager.getInstance(project).getClientVersion();

      if (clientVersion != null && clientVersion.supportsMove() && (oldConnection != null) && (newConnection != null) &&
          (oldConnection.getId().equals(newConnection.getId()))) {
        if (! oldfstat.isOpenedOrAdded()) {
          runner.edit(oldP4File, -1);
        }
        runner.move(oldP4File, newP4File, oldConnection);
        return;
      }*/
      final boolean moveWasUsed = (oldfstat.local == FStat.LOCAL_MOVE_ADDING);
      if (moveWasUsed) {
        if (oldfstat.movedFile == null) {
          throw new VcsException("Move/rename error; can not find moved/deleted file for moved/added: " + myOldPath);
        }

        final long changeList = getPerforceChangeList(project, oldP4File);
        runner.revert(oldP4File, true);

        refreshWorker.addDeletedFile(new File(myOldPath));

        oldP4File.clearCache();
        oldP4File.invalidateFstat();

        final P4WhereResult p4WhereResult = runner.where(oldfstat.movedFile, oldConnection);
        myOldPath = p4WhereResult.getLocal();
        oldP4File = P4File.createInefficientFromLocalPath(myOldPath);
        runner.edit(oldP4File, changeList);
        oldfstat = oldP4File.getFstat(project, false);

        // if moving back -> that's all
        if (new File(myOldPath).equals(new File(myNewPath))) return;
      }

      moveUsingBranchCommand(project, oldP4File, newP4File, oldConnection, newConnection, runner, oldfstat);
    }
    finally {
      oldP4File.clearCache();
      newP4File.clearCache();

      refreshWorker.addDeletedFile(new File(myOldPath));
      refreshWorker.refreshFile(new File(myNewPath));
      refreshWorker.run(project);
    }
  }

  private void refreshDeletion(final File oldFile, final Collection<VirtualFile> vfiles) {
    final LocalFileSystem lfs = LocalFileSystem.getInstance();
    if (oldFile.getParentFile() != null) {
      final VirtualFile vf = lfs.refreshAndFindFileByIoFile(oldFile.getParentFile());
      if (vf != null) {
        vfiles.add(vf);
        vf.findChild(oldFile.getName());
      }
    }
  }

  private void moveUsingBranchCommand(Project project, P4File oldP4File, P4File newP4File, P4Connection oldConnection,
                                      P4Connection newConnection,
                                      PerforceRunner runner, FStat oldfstat) throws VcsException {
    boolean fileWasLocallyAdded = oldfstat.local == FStat.LOCAL_ADDING;
    boolean processedDoubleRename = false;
    if (fileWasLocallyAdded) {
      final List<ResolvedFile> resolvedFiles = runner.getResolvedFiles(oldConnection);
      for(ResolvedFile resolvedFile: resolvedFiles) {
        final String resolvedFilePath = FileUtil.toSystemIndependentName(resolvedFile.getLocalFile().toString());
        if (resolvedFilePath.equals(myOldPath)) {
          final PerforceClient perforceClient = PerforceManager.getInstance(project).getClient(oldConnection);
          final File realOldFile = PerforceManager.getFileByDepotName(resolvedFile.getDepotPath(), perforceClient);
          P4File realOldP4File = P4File.create(realOldFile);
          FStat realOldFStat = realOldP4File.getFstat(project, true);
          if (realOldFStat.local == FStat.LOCAL_DELETING) {
            if (new File(myNewPath).equals(realOldFile)) {
              processCycle(project, newP4File, oldP4File);
            }
            else {
              processDoubleRename(project, realOldP4File, newP4File, oldP4File);
            }
            processedDoubleRename = true;
          }
          break;
        }
      }
    }

    if (!processedDoubleRename) {
      boolean fileWasUnderPerforce = oldfstat.status != FStat.STATUS_NOT_ADDED;
      if (fileWasLocallyAdded || !fileWasUnderPerforce || oldConnection != newConnection) {
        if (fileWasLocallyAdded) {
          assureNoFile(project, oldP4File, true, false);
        }
        else {
          runner.assureDel(oldP4File, null);
        }

        createNewFile(project, newP4File);
      }
      else {
        int type = detectType(oldfstat, oldP4File, myNewPath);
        switch (type) {
          case CYCLE:
            processCycle(project, newP4File, oldP4File);
            break;
          case DOUBLE_RENAME:
            processDoubleRename(project, oldfstat.fromFile, newP4File, oldP4File);
            break;
          case SIMPLE:
            processRename(project, newP4File, oldP4File);
            break;
        }
      }
    }
  }

  private static int detectType(FStat oldfstat, P4File oldP4File, String newPath) {
    boolean doubleRename = false;
    P4File realOldP4File = null;
    if (oldfstat.local == FStat.LOCAL_BRANCHING) {
      realOldP4File = oldfstat.fromFile;
      doubleRename = true;
    }
    if (realOldP4File == null) {
      realOldP4File = oldP4File;
      doubleRename = false;
    }

    if (doubleRename) {
      if (new File(newPath).equals(new File(realOldP4File.getLocalPath()))) {
        return CYCLE;
      }
      else {
        return DOUBLE_RENAME;
      }

    }
    else {
      return SIMPLE;
    }

  }

  private static void createNewFile(final Project project, final P4File p4File) throws VcsException {
    final PerforceRunner runner = PerforceRunner.getInstance(project);
    final FStat fstat = p4File.getFstat(project, false);
    if (fstat.status == FStat.STATUS_NOT_IN_CLIENTSPEC) {
      // this is not OK, that's what we DON'T want
      throw new VcsException(PerforceBundle.message("exception.text.cannot.add.file.not.under.any.spec", p4File));
    }
    else if (fstat.status == FStat.STATUS_NOT_ADDED || fstat.status == FStat.STATUS_DELETED) {
      runner.add(p4File);
    }
    else if (fstat.status == FStat.STATUS_ONLY_LOCAL) {
      // I hope this means it is being added
    }
    else {
      // here it EXISTS on server
      if (fstat.local == FStat.LOCAL_DELETING) {
        final String localPath = p4File.getLocalPath();
        final File file = new File(localPath);
        new ActionWithTempFile(file) {
          protected void executeInternal() throws VcsException {
            runner.revert(p4File, false);
            runner.edit(p4File);
          }
        }.execute();
      }
      else if (fstat.local == FStat.LOCAL_CHECKED_IN) {
        final String localPath = p4File.getLocalPath();
        final File file = new File(localPath);
        new ActionWithTempFile(file) {
          protected void executeInternal() throws VcsException {
            runner.sync(p4File, false);
            runner.edit(p4File);
          }
        }.execute();

      }
      else {
        // we hope this means the file is being edited, added, etc.
      }
    }
  }

  private void processRename(final Project project, final P4File newP4File, final P4File oldP4File) throws VcsException {
    final PerforceRunner runner = PerforceRunner.getInstance(project);
    final String newPath = newP4File.getAnyPath();
    final File newFile = new File(newPath);

    final long changeList = getPerforceChangeList(project, newP4File);
    new ActionWithTempFile(newFile) {
      protected void executeInternal() throws VcsException {
        assureNoFile(project, newP4File, false, true);

        //final boolean deleteLocalCopy = !new File(oldP4File.getAnyPath()).isFile();
        runner.integrate(oldP4File, newP4File, changeList);
        runner.edit(newP4File, changeList);
        // todo ??? - check why is it here -> test offline mode
        //if (deleteLocalCopy) {
          runner.assureDel(oldP4File, changeList);
        //}
      }
    }.execute();
  }

  private static void processDoubleRename(final Project project, final P4File realOldP4File, final P4File newP4File, final P4File oldP4File) throws VcsException {
    final PerforceRunner runner = PerforceRunner.getInstance(project);
    final FStat realOldfstat = realOldP4File.getFstat(project, false);
    if (realOldfstat.status == FStat.STATUS_DELETED) {
      throw new VcsException(PerforceBundle.message("exception.text.cannot.move.original.deleted"));
    }
    new ActionWithTempFile(new File(newP4File.getAnyPath())) {
      protected void executeInternal() throws VcsException {
        assureNoFile(project, newP4File, false, false);
        runner.revert(oldP4File, true);
        runner.revert(realOldP4File, true);
        runner.integrate(realOldP4File, newP4File);
        runner.edit(newP4File);
      }
    }.execute();
    runner.assureDel(oldP4File, null);
    runner.assureDel(realOldP4File, null);
  }

  private static void processCycle(final Project project, final P4File newP4File, final P4File oldP4File) throws VcsException {
    final PerforceRunner runner = PerforceRunner.getInstance(project);
    final FStat newfstat = newP4File.getFstat(project, true);
    if (newfstat.local == FStat.LOCAL_DELETING) {
      new ActionWithTempFile(new File(newP4File.getAnyPath())) {
        protected void executeInternal() throws VcsException {
          runner.revert(newP4File, false);
          runner.edit(newP4File);
        }
      }.execute();
    }
    else if (newfstat.status == FStat.STATUS_NOT_ADDED || newfstat.status == FStat.STATUS_ONLY_LOCAL) {
      createNewFile(project, newP4File);
    }
    else {
      throw new VcsException(PerforceBundle.message("exception.text.cannot.rename"));
    }
    runner.assureDel(oldP4File, null);
  }

  private static void assureNoFile(final Project project, final P4File p4File, final boolean canBeEdit, final boolean canBeDeleting) throws VcsException {
    final PerforceRunner runner = PerforceRunner.getInstance(project);
    final FStat fstat = p4File.getFstat(project, false);
    if (fstat.status == FStat.STATUS_NOT_ADDED || fstat.status == FStat.STATUS_NOT_IN_CLIENTSPEC || fstat.status == FStat.STATUS_DELETED) {
      // this is OK, that's what we want
    }
    else {
      if (fstat.status == FStat.STATUS_ONLY_LOCAL) {
        runner.revert(p4File, false);
      }
      // todo check afterwards: whether LOCAL_MOVE_DELETING should also be put here
      else if (canBeDeleting && fstat.local == FStat.LOCAL_DELETING) {
        runner.revert(p4File, canBeEdit);
        runner.edit(p4File);
      }
      else {
        if (canBeEdit) {
          runner.revert(p4File, true);
          runner.edit(p4File);
        }
        else {
          throw new VcsException(PerforceBundle.message("exception.text.cannot.assure.no.file.being.on.server", p4File));
        }
      }
    }
  }

  @Override
  public Change getChange(final Project project) {
    final VcsContextFactory factory = VcsContextFactory.SERVICE.getInstance();
    FilePath beforePath = factory.createFilePathOn(new File(myOldPath), false);
    FilePath afterPath = factory.createFilePathOn(new File(myNewPath), false);
    ContentRevision beforeRevision = PerforceCachingContentRevision.create(project, beforePath, afterPath, -1);
    ContentRevision afterRevision = CurrentContentRevision.create(afterPath);
    return new Change(beforeRevision, afterRevision);
  }

  @Override
  public VcsOperation checkMerge(final VcsOperation oldOp) {
    if (oldOp instanceof P4AddOperation) {
      String oldPath = ((P4AddOperation) oldOp).getPath();
      if (FileUtil.pathsEqual(oldPath, myOldPath)) {
        return new P4AddOperation(myChangeList, myNewPath);
      }
    }
    else if (oldOp instanceof P4MoveRenameOperation) {
      final P4MoveRenameOperation moveOp = (P4MoveRenameOperation)oldOp;
      if (FileUtil.pathsEqual(moveOp.getNewPath(), myOldPath)) {
        if (FileUtil.pathsEqual(moveOp.getOldPath(), myNewPath)) {
          return new P4EditOperation(myChangeList, myNewPath);
        }
        return new P4MoveRenameOperation(myChangeList, moveOp.getOldPath(), myNewPath);
      }
    }
    else if (oldOp instanceof P4EditOperation) {
      String oldPath = ((P4EditOperation) oldOp).getPath();
      if (FileUtil.pathsEqual(oldPath, myOldPath)) {
        return this;
      }
    }
    return super.checkMerge(oldOp);
  }

  @Override
  public void prepareOffline() {
    VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(myOldPath);
    if (vFile != null) {
      PerforceCachingContentRevision.saveCurrentContent(vFile);
    }
  }
}
