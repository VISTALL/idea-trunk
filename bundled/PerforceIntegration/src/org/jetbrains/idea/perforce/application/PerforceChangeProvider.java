package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.text.FilePathHashingStrategy;
import com.intellij.vcsUtil.VcsUtil;
import gnu.trove.THashSet;
import gnu.trove.TObjectLongHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.*;

/**
 * @author max
 */
public class PerforceChangeProvider implements ChangeProvider {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceChangeProvider");

  private final Project myProject;
  private final PerforceRunner myRunner;
  private final LastSuccessfulUpdateTracker myLastSuccessfulUpdateTracker;

  public PerforceChangeProvider(final PerforceVcs vcs) {
    myProject = vcs.getProject();
    myRunner = PerforceRunner.getInstance(myProject);
    myLastSuccessfulUpdateTracker = LastSuccessfulUpdateTracker.getInstance(myProject);
  }

  public void getChanges(final VcsDirtyScope dirtyScope, final ChangelistBuilder builder, final ProgressIndicator progress,
                         final ChangeListManagerGate addGate) throws VcsException {
    LOG.debug("getting changes for scope " + dirtyScope);

    myLastSuccessfulUpdateTracker.updateStarted();
    if (dirtyScope.getRecursivelyDirtyDirectories().size() == 0) {
      final Set<FilePath> filePaths = dirtyScope.getDirtyFiles();
      boolean hasDirectories = false;
      for(FilePath filePath: filePaths) {
        if (filePath.isDirectory()) {
          hasDirectories = true;
          break;
        }
      }
      if (!hasDirectories) {
        getChangesForFiles(filePaths, builder, progress, addGate);
        myLastSuccessfulUpdateTracker.updateSuccessful();
        return;
      }
    }

    final TObjectLongHashMap<String> filesInPerforce = new TObjectLongHashMap<String>(FilePathHashingStrategy.create());
    Set<String> reportedChanges = new THashSet<String>(FilePathHashingStrategy.create());

    Set<P4Connection> connections = new THashSet<P4Connection>();
    final Collection<VirtualFile> roots = dirtyScope.getAffectedContentRoots();
    for (VirtualFile root : roots) {
      final P4Connection connection = PerforceConnectionManager.getInstance(myProject).getConnectionForFile(root);
      if (connection != P4Connection.INVALID) {
        connections.add(connection);
      }
    }

    for (P4Connection connection : connections) {
      processConnection(connection, dirtyScope, builder, filesInPerforce, reportedChanges, progress, addGate);
    }

    if (builder.isUpdatingUnversionedFiles()) {
      final Set<String> localFiles = new THashSet<String>(FilePathHashingStrategy.create());
      final Set<String> writableFiles = new THashSet<String>(FilePathHashingStrategy.create());
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          dirtyScope.iterate(new Processor<FilePath>() {
            public boolean process(FilePath fileOrDir) {
              if (!fileOrDir.isDirectory()) {
                localFiles.add(FileUtil.toSystemDependentName(fileOrDir.getPath()));
                VirtualFile vf = fileOrDir.getVirtualFile();
                if (vf != null && vf.isWritable()) {
                  writableFiles.add(FileUtil.toSystemDependentName(fileOrDir.getPath()));
                }
              }
              return true;
            }
          });
        }
      });

      final Object[] keys = filesInPerforce.keys();
      final Set<String> unversionedFiles = new THashSet<String>(localFiles, FilePathHashingStrategy.create());
      for(Object key: keys) {
        unversionedFiles.remove((String) key);
      }
      unversionedFiles.removeAll(reportedChanges);

      final Set<String> missingFiles = new THashSet<String>(FilePathHashingStrategy.create());
      for(Object key: keys) {
        String fileName = (String) key;
        if (!localFiles.contains(fileName) && !reportedChanges.contains(fileName)) {
          missingFiles.add(fileName);
        }
      }

      writableFiles.removeAll(reportedChanges);
      writableFiles.removeAll(unversionedFiles);

      final ChangeCreator changeCreator = new ChangeCreator(myProject);
      final LocalFileSystem fs = LocalFileSystem.getInstance();
      ApplicationManager.getApplication().runReadAction(new Runnable() {
        public void run() {
          for (String path : unversionedFiles) {
            // support Apple fork (IDEADEV-19059)
            int pos = path.lastIndexOf(File.separatorChar);
            if (pos < path.length()-1 && path.charAt(pos+1) == '%') {
              String basePath = path.substring(0, pos+1) + path.substring(pos+2);
              if (filesInPerforce.containsKey(basePath)) {
                continue;
              }
            }
            final VirtualFile file = fs.findFileByPath(FileUtil.toSystemIndependentName(path));
            if (file != null) {
              builder.processUnversionedFile(file);
            }
          }
          for(String path: writableFiles) {
            final VirtualFile file = fs.findFileByPath(FileUtil.toSystemIndependentName(path));
            if (file != null) {
              processWritableFile(file, builder, changeCreator);
            }
          }
          for (String path : missingFiles) {
            builder.processLocallyDeletedFile(VcsUtil.getFilePathForDeletedFile(path, false));
          }
        }
      });
    }
    myLastSuccessfulUpdateTracker.updateSuccessful();
  }

  private void processWritableFile(final VirtualFile file, final ChangelistBuilder builder, final ChangeCreator changeCreator) {
    boolean asyncEdit = false;
    if (PerforceVcs.getInstance(myProject).isAsyncEditFile(file)) {
      try {
        long revision = myRunner.haveRevision(P4File.create(file));
        if (revision > 0) {
          asyncEdit = true;
          final FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(file);
          builder.processChange(changeCreator.createEditedFileChange(filePath, revision, false), PerforceVcs.getKey());
        }
      }
      catch (VcsException e) {
        asyncEdit = false;
      }
    }
    if (!asyncEdit) {
      builder.processModifiedWithoutCheckout(file);
    }
  }

  private void getChangesForFiles(final Set<FilePath> dirtyFiles, final ChangelistBuilder builder, final ProgressIndicator progress,
                                  final ChangeListManagerGate addGate) throws VcsException {
    final MultiMap<ConnectionKey, PerforceChange> changesToProcess = new MultiMap<ConnectionKey, PerforceChange>();
    final Map<ConnectionKey, P4Connection> connMap = new HashMap<ConnectionKey, P4Connection>();
    final ChangeCreator changeCreator = new ChangeCreator(myProject);

    for(final FilePath filePath: dirtyFiles) {
      progress.checkCanceled();
      if (filePath.isDirectory()) continue;
      final P4Connection connection = ApplicationManager.getApplication().runReadAction(new Computable<P4Connection>() {
        @Nullable
        public P4Connection compute() {
          return PerforceConnectionManager.getInstance(myProject).getConnectionForFile(filePath.getIOFile());
        }
      });
      P4File file = P4File.create(filePath);

      final VirtualFile vf = filePath.getVirtualFile();
      final boolean isWriteable = (vf != null && vf.isWritable());

      PerforceChange change = myRunner.opened(file);
      final File ioFile = filePath.getIOFile();
      boolean fileExists = ioFile.exists();

      if (change == null) {
        boolean haveFile = myRunner.have(file);
        if ((! haveFile) && fileExists) {
          // support Apple fork (IDEADEV-19059)
          if (filePath.getName().startsWith("%")) {
            final FilePath parentPath = filePath.getParentPath();
            if (parentPath != null) {
              File baseFile = new File(parentPath.getIOFile(), filePath.getName().substring(1));
              if (myRunner.have(P4File.create(baseFile))) {
                haveFile = true;
              }
            }
          }
          if (!haveFile) {
            builder.processUnversionedFile(vf);
          }
        }
        else if (vf != null && isWriteable) {
          processWritableFile(vf, builder, changeCreator);
        }
        else if (haveFile && !fileExists) {
          builder.processLocallyDeletedFile(filePath);
        }
      }
      else {
        change.setFile(ioFile);
        
        final ConnectionKey connectionKey = ConnectionKey.create(myProject, connection);
        connMap.put(connectionKey, connection);
        changesToProcess.putValue(connectionKey, change);
      }
    }

    for (Map.Entry<ConnectionKey, P4Connection> entry : connMap.entrySet()) {
      final ConnectionKey connectionKey = entry.getKey();
      final P4Connection connection = entry.getValue();

      final Collection<PerforceChange> changes = changesToProcess.get(connectionKey);

      final LocalPathsSet resolvedWithConflictsMap = myRunner.getResolvedWithConflictsMap(connection, null);
      final ResolvedFilesWrapper resolvedFilesWrapper = new ResolvedFilesWrapper(myRunner.getResolvedFiles(connection, null));

      final PerforceChangeListCalculator changeListCalculator = new PerforceChangeListCalculator(myProject, connection, addGate);

      final OpenedResultProcessor processor =
        new OpenedResultProcessor(connection, changeCreator, builder, resolvedWithConflictsMap, resolvedFilesWrapper,
                                  changeListCalculator);
      processor.process(changes);
    }
  }

  private void processConnection(final P4Connection connection,
                                 final VcsDirtyScope dirtyScope,
                                 final ChangelistBuilder builder,
                                 final TObjectLongHashMap<String> filesInPerforce,
                                 final Set<String> reportedChanges,
                                 final ProgressIndicator progress, final ChangeListManagerGate addGate) throws VcsException {
    progress.checkCanceled();
    collectFilesInPerforce(filesInPerforce, dirtyScope, connection);
    progress.checkCanceled();

    final ChangeCreator changeCreator = new ChangeCreator(myProject);
    changeCreator.setCreationListener(new Consumer<Change>() {
      public void consume(Change change) {
        final ContentRevision afterRevision = change.getAfterRevision();
        if (afterRevision != null) {
          reportedChanges.add(FileUtil.toSystemDependentName(afterRevision.getFile().getPath()));
        }
        final ContentRevision beforeRevision = change.getBeforeRevision();
        if (beforeRevision != null) {
          reportedChanges.add(FileUtil.toSystemDependentName(beforeRevision.getFile().getPath()));
        }
      }
    });

    final Collection<VirtualFile> roots = dirtyScope.getAffectedContentRoots();
    for (VirtualFile root : roots) {
      processChangesUnderRoot(connection, builder, addGate, changeCreator, root);
    }
  }

  private void processChangesUnderRoot(final P4Connection connection, final ChangelistBuilder builder,
                                       final ChangeListManagerGate addGate, final ChangeCreator changeCreator, @NotNull final VirtualFile root)
    throws VcsException {
    final LocalPathsSet resolvedWithConflictsMap = myRunner.getResolvedWithConflictsMap(connection, root);
    final ResolvedFilesWrapper resolvedFilesWrapper = new ResolvedFilesWrapper(myRunner.getResolvedFiles(connection, root));
    final List<PerforceChange> changes = myRunner.getChangesUnder(connection, root);

    final PerforceChangeListCalculator changeListCalculator = new PerforceChangeListCalculator(myProject, connection, addGate);

    final OpenedResultProcessor processor =
      new OpenedResultProcessor(connection, changeCreator, builder, resolvedWithConflictsMap, resolvedFilesWrapper,
                                changeListCalculator);
    processor.process(changes);
  }

  public boolean isModifiedDocumentTrackingRequired() {
    return false;
  }

  public void doCleanup(final List<VirtualFile> files) {
  }

  private void collectFilesInPerforce(TObjectLongHashMap<String> paths, VcsDirtyScope scope, P4Connection connection) throws VcsException {
    for (FilePath root : scope.getRecursivelyDirtyDirectories()) {
      myRunner.have(P4File.create(root), connection, true, paths);
    }

    for (FilePath path : scope.getDirtyFilesNoExpand()) {
      myRunner.have(P4File.create(path), connection, false, paths);
    }
  }
}
