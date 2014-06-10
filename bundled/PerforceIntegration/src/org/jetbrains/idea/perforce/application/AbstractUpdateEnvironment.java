/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package org.jetbrains.idea.perforce.application;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.LineTokenizer;
import com.intellij.openapi.vcs.AbstractVcsHelper;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.update.*;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.perforce.ExecResult;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.*;

abstract class AbstractUpdateEnvironment implements UpdateEnvironment {
  private final static Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.AbstractUpdateEnvironment");
  protected final Project myProject;
  protected final PerforceRunner myRunner;

  public AbstractUpdateEnvironment(final Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
  }

  protected void processOutput(final String output,
                               final UpdatedFiles updatedFiles,
                               final Map<String, String> patternToGroupId,
                               final PerforceClient client) throws VcsException {
    String[] lines = LineTokenizer.tokenize(output, false);
    for (String line : lines) {
      fillUpdateInformationFromTheLine(line, updatedFiles, patternToGroupId, client);
    }
  }

  private void fillUpdateInformationFromTheLine(String line,
                                                UpdatedFiles updatedFiles,
                                                Map<String, String> patternToGroupId,
                                                final PerforceClient client) throws VcsException {
    for (String pattern : patternToGroupId.keySet()) {
      if (processLine(line, updatedFiles, pattern, patternToGroupId.get(pattern), client)) {
        return;
      }
    }
  }

  private boolean processLine(String line, UpdatedFiles updatedFiles,
                              String message, String fileGroupId, final PerforceClient client) throws VcsException {
    int messageStart = line.indexOf(message);
    if (messageStart < 0) return false;
    String depotFilePath = line.substring(0, messageStart).trim();
    int revNumPos = depotFilePath.indexOf('#');
    final File fileByDepotName = PerforceManager.getFileByDepotName(depotFilePath, client);
    final VcsKey vcsKey = PerforceVcs.getKey();
    if (fileByDepotName != null) {
      final FileGroup fileGroup = updatedFiles.getGroupById(fileGroupId);
      if (revNumPos > 0) {
        long revision = Long.parseLong(depotFilePath.substring(revNumPos+1));
        fileGroup.add(fileByDepotName.getPath(), vcsKey, new VcsRevisionNumber.Long(revision));
      }
      else {
        fileGroup.add(fileByDepotName.getPath(), vcsKey, null);
      }
    }
    return true;
  }

  protected PerforceSettings getSettings() {
    return PerforceSettings.getSettings(myProject);
  }

  private void resolveAutomatically(final P4File contentRoot) throws VcsException {
    myRunner.resolveAutomatically(contentRoot);
  }

  @NotNull
  public UpdateSession updateDirectories(@NotNull FilePath[] contentRoots, UpdatedFiles updatedFiles, ProgressIndicator progressIndicator,
                                         @NotNull final Ref<SequentialUpdatesContext> context)
    throws ProcessCanceledException {
    PerforceSettings settings = getSettings();
    final ArrayList<VcsException> vcsExceptions = new ArrayList<VcsException>();


    try {
      final PerforceConnectionManager connectionManager = PerforceConnectionManager.getInstance(myProject);
      final PerforceManager perforceManager = PerforceManager.getInstance(myProject);

      final Map<String, P4Connection> connectionsForRoots = new HashMap<String, P4Connection>();
      for (FilePath root : contentRoots) {
        final P4Connection connection = connectionManager.getConnectionForFile(root.getVirtualFile());
        connectionsForRoots.put(root.getPath(), connection);
      }
      for (FilePath contentRoot : contentRoots) {
        final P4Connection connection = connectionsForRoots.get(contentRoot.getPath());
        if (connection == null) {
          LOG.error("connection should had been found for root " + contentRoot.getPath());
          continue;
        }
        final PerforceClient client = perforceManager.getClient(connection);

        final P4File p4Dir = P4File.create(contentRoot);
        if (isRevertUnchanged(settings)) {
          try {
            myRunner.revertUnchanged(p4Dir);
          }
          catch (VcsException e) {
            vcsExceptions.add(e);
          }
        }

        try {
          final ExecResult execResult = performUpdate(p4Dir, settings);
          processOutput(execResult.getStdout(), updatedFiles, getPatternToGroupId(), client);
          VcsException[] updateExceptions = PerforceRunner.checkErrors(execResult);
          if (updateExceptions.length > 0) {
            Collections.addAll(vcsExceptions, updateExceptions);
          }
          else {
            if (isTryToResolveAutomatically(settings)) {
              resolveAutomatically(p4Dir);
            }
          }
        }
        catch (VcsException e) {
          vcsExceptions.add(e);
        }
      }

      try {
        final List<VirtualFile> filesToResolve = new LinkedList<VirtualFile>();
        for (FilePath root : contentRoots) {
          final P4Connection connection = connectionsForRoots.get(root.getPath());
          if (connection != null && root.getVirtualFile() != null) {
            final List<VirtualFile> files = myRunner.getResolvedWithConflicts(connection, root.getVirtualFile());
            filesToResolve.addAll(filterByServerVersion(files));
          }
        }
        if (! filesToResolve.isEmpty()) {
          ApplicationManager.getApplication().invokeAndWait(new Runnable() {
            public void run() {
              AbstractVcsHelper.getInstance(myProject).showMergeDialog(filesToResolve, new PerforceMergeProvider(myProject));
            }
          }, ModalityState.defaultModalityState());
        }
      }
      catch (VcsException e) {
         //ignore
      }
    }
    finally {
      PerforceSettings.getSettings(myProject).SYNC_FORCE = false;
    }
    return new UpdateSessionAdapter(vcsExceptions, false);
  }

  private static List<VirtualFile> filterByContentRoot(final List<VirtualFile> filesToResolve, final FilePath[] contentRoots) {
    final ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    for(VirtualFile file: filesToResolve) {
      for(FilePath contentRoot: contentRoots) {
        if (VfsUtil.isAncestor(contentRoot.getIOFile(), VfsUtil.virtualToIoFile(file), false) && !result.contains(file)) {
          result.add(file);
          break;
        }
      }
    }
    return result;
  }

  private List<VirtualFile> filterByServerVersion(final Collection<VirtualFile> allFilesToResolve) {
    final PerforceConnectionManager connectionManager = PerforceConnectionManager.getInstance(myProject);
    final ArrayList<VirtualFile> result = new ArrayList<VirtualFile>();
    for (VirtualFile virtualFile : allFilesToResolve) {
      if (PerforceSettings.getSettings(myProject).getServerVersion(connectionManager.getConnectionForFile(virtualFile)) >= 2004) {
        result.add(virtualFile);
      }
    }
    return result;
  }

  protected abstract boolean isTryToResolveAutomatically(PerforceSettings settings);

  protected abstract Map<String, String> getPatternToGroupId();

  protected abstract boolean isRevertUnchanged(PerforceSettings settings);

  protected abstract ExecResult performUpdate(P4File p4Dir, PerforceSettings settings) throws VcsException;
}
