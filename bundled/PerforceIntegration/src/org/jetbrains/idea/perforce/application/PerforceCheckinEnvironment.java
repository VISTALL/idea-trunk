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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.VcsConfiguration;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.operations.P4AddOperation;
import org.jetbrains.idea.perforce.operations.P4DeleteOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.perforce.PerforceAbstractChange;
import org.jetbrains.idea.perforce.perforce.PerforceChange;
import org.jetbrains.idea.perforce.perforce.PerforceRunner;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;
import org.jetbrains.idea.perforce.perforce.commandWrappers.DeleteEmptyChangeList;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceCheckinComponent;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceJob;

import java.util.*;

public class PerforceCheckinEnvironment implements CheckinEnvironment{
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceCheckinEnvironment");

  private final Project myProject;
  private final PerforceRunner myRunner;
  private final PerforceVcs myVcs;

  public PerforceCheckinEnvironment(Project project) {
    myProject = project;
    myRunner = PerforceRunner.getInstance(project);
    myVcs = PerforceVcs.getInstance(project);
  }

  @Nullable
  public RefreshableOnComponent createAdditionalOptionsPanel(final CheckinProjectPanel panel) {
    return PerforceSettings.getSettings(myProject).USE_PERFORCE_JOBS ? new PerforceCheckinComponent(myProject) : null;
  }

  public String getDefaultMessageFor(FilePath[] filesToCheckin) {
    return null;
  }

  public String getHelpId() {
    return null;
  }

  public String getCheckinOperationName() {
    return PerforceBundle.message("operation.name.submit");
  }

  public List<VcsException> commit(List<Change> incomingChanges, String preparedComment) {
    return commit(incomingChanges, preparedComment, null);
  }

  public List<VcsException> commit(List<Change> incomingChanges, String preparedComment, Object parameters) {
    if (preparedComment == null) {
      preparedComment = PerforceBundle.message("default.non.empty.comment");
    }

    ArrayList<VcsException> vcsExceptions = new ArrayList<VcsException>();
    try {
      final PerforceChangeGatherer<SubmitJob> gatherer = new PerforceChangeGatherer<SubmitJob>(myProject, new Convertor<P4Connection, SubmitJob>() {
        public SubmitJob convert(P4Connection connection) {
          return new SubmitJob(connection);
        }
      });
      gatherer.execute(incomingChanges);

      final Map<ConnectionKey, SubmitJob> map = gatherer.getByConnectionMap();
      if (map.isEmpty()) {
        vcsExceptions.add(new VcsException(PerforceBundle.message("exception.text.nothing.found.to.submit")));
      } else {
        for (Map.Entry<ConnectionKey, SubmitJob> entry : map.entrySet()) {
          entry.getValue().submit(preparedComment, (List<PerforceJob>) parameters);
        }
      }

      LOG.info("updating opened files after commit");
      ChangeListSynchronizer.getInstance(myProject).queueUpdateOpenedFiles();
    }
    catch (VcsException e) {
      vcsExceptions.add(e);
    } finally{
      PerforceManager.getInstance(myProject).clearCache();
    }
    return vcsExceptions;
  }

  private class SubmitJob implements PerforceChangesForConnection {
    private final P4Connection myConnection;
    private final List<PerforceChange> myChanges = new ArrayList<PerforceChange>();

    public SubmitJob(final P4Connection connection) {
      myConnection = connection;
    }

    public void addChanges(final Collection<PerforceChange> changes) {
      myChanges.addAll(changes);
    }

    public P4Connection getConnection() {
      return myConnection;
    }

    public void submit(String comment, final List<PerforceJob> p4jobs) throws VcsException {
      if (myChanges.size() == 0) return;
      long changeListID = createSingleChangeListForConnection();
      myRunner.submitForConnection(myConnection, myChanges, changeListID, comment, p4jobs);
      if (changeListID == -1) {
        myVcs.clearDefaultAssociated();
      }
    }

    private long createSingleChangeListForConnection() throws VcsException {
      final MultiMap<Long, PerforceChange> byListMap = new MultiMap<Long, PerforceChange>();
      for (PerforceChange change : myChanges) {
        byListMap.putValue(change.getChangeList(), change);
      }

      if (byListMap.size() == 0) return -1; //???
      if (byListMap.size() == 1) return byListMap.keySet().iterator().next();

      for (Long number : byListMap.keySet()) {
        if (number == -1) continue;
        myRunner.reopen(myConnection, (List<PerforceChange>) byListMap.get(number), -1);
        
        final DeleteEmptyChangeList deleteCommand = myRunner.deleteEmptyChangeList(myConnection, number);
        deleteCommand.allowError(DeleteEmptyChangeList.NOT_EMPTY);
        deleteCommand.run();
      }
      return -1;
    }
  }

  public List<VcsException> scheduleMissingFileForDeletion(final List<FilePath> files) {
    final VcsBackgroundTask<FilePath> task = new VcsBackgroundTask<FilePath>(myProject, "Removing Files",
                                                                             VcsConfiguration.getInstance(myProject).getAddRemoveOption(),
                                                                             files) {
      protected void process(final FilePath item) throws VcsException {
        new P4DeleteOperation(ChangeListManager.getInstance(myProject).getDefaultChangeList().getName(), item).executeOrLog(myProject);
      }
    };
    PerforceVcs.getInstance(myProject).runTask(task);
    return Collections.emptyList();
  }

  public List<VcsException> scheduleUnversionedFilesForAddition(final List<VirtualFile> files) {
    String activeChangeList = ChangeListManager.getInstance(myProject).getDefaultChangeList().getName();
    final List<VcsOperation> ops = new ArrayList<VcsOperation>();
    for(VirtualFile file: files) {
      ops.add(new P4AddOperation(activeChangeList, file));
    }
    PerforceVcs.getInstance(myProject).runOrQueue(ops, PerforceBundle.message("progress.title.running.perforce.commands"),
                                                  VcsConfiguration.getInstance(myProject).getAddRemoveOption());
    return Collections.emptyList();
  }

  public boolean keepChangeListAfterCommit(ChangeList changeList) {
    return false;
  }

  static <T extends PerforceAbstractChange> List<T> collectChanges(List<T> changes, Collection<FilePath> paths) {
    ArrayList<T> result = new ArrayList<T>();
    for (FilePath file : paths) {
      T change = findChange(changes, file);
      if (change != null) {
        result.add(change);
      }
    }
    return result;
  }

  @Nullable
  private static <T extends PerforceAbstractChange> T findChange(List<T> changes, FilePath file) {
    for (T change : changes) {
      if (change.getFile().equals(file.getIOFile())) return change;
    }
    return null;
  }
}
