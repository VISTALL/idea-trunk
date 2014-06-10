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

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.changes.ChangeListEditHandler;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ChangeProvider;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.merge.MergeProvider;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.io.ReadOnlyAttributeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.PerforceBundle;
import org.jetbrains.idea.perforce.changesBrowser.PerforceChangeBrowserSettings;
import org.jetbrains.idea.perforce.merge.PerforceMergeProvider;
import org.jetbrains.idea.perforce.operations.P4EditOperation;
import org.jetbrains.idea.perforce.operations.VcsOperation;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.jobs.PerforceJob;

import java.io.IOException;
import java.util.*;

public class PerforceVcs extends AbstractVcs {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceVcs");
  private static final String NAME = "Perforce";
  private static final VcsKey ourKey = createKey(NAME);
  private PerforceCheckinEnvironment myPerforceCheckinEnvironment;
  private PerforceUpdateEnvironment myPerforceUpdateEnvironment;
  private PerforceIntegrateEnvironment myPerforceIntegrateEnvironment;
  private RollbackEnvironment myPerforceRollbackEnvironment;
  private RollbackEnvironment myOfflineRollbackEnvironment;
  private CommittedChangesProvider<PerforceChangeList, PerforceChangeBrowserSettings> myCommittedChangesProvider;

  private final ChangeListManager myChangeListManager;
  private final MyEditFileProvider myMyEditFileProvider;
  private final EditFileProvider myUnavailableEditFileProvider;
  private ChangeProvider myChangeProvider;
  private ChangeProvider myOfflineChangeProvider;
  private PerforceConfigurable myPerforceConfigurable;
  private PerforceVcsHistoryProvider myHistoryProvider;
  private PerforceAnnotationProvider myAnnotationProvider;
  private PerforceDiffProvider myDiffProvider;
  private final PerforceSettings mySettings;

  private final VcsShowConfirmationOption myAddOption;
  private final VcsShowConfirmationOption myRemoveOption;
  private PerforceVFSListener myVFSListener;
  private MergeProvider myMergeProvider;

  private final Set<VirtualFile> myAsyncEditFiles = new HashSet<VirtualFile>();
  private final BackgroundTaskQueue myTaskQueue;

  private final PerforceChangeListEditHandler myChangeListEditHandler;

  private final Map<ConnectionKey, java.util.List<PerforceJob>> myDefaultAssociated;
  private final PerforceExceptionsHotFixer myHotFixer;

  public PerforceVcs(Project project, ChangeListManager changeListManager) {
    super(project, NAME);
    myChangeListManager = changeListManager;
    mySettings = PerforceSettings.getSettings(myProject);
    myMyEditFileProvider = new MyEditFileProvider();

    myUnavailableEditFileProvider =
      new DisabledFileProvider(PerforceBundle.message("perforce.unavailable.use.file.system.confirmation"), false);

    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(getProject());
    myAddOption = vcsManager.getStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, this);
    myRemoveOption = vcsManager.getStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, this);
    myTaskQueue = new BackgroundTaskQueue(project, "Perforce Operations");

    myChangeListEditHandler = new PerforceChangeListEditHandler();

    myDefaultAssociated = new HashMap<ConnectionKey, List<PerforceJob>>();

    myHotFixer = new PerforceExceptionsHotFixer(project);
  }

  public String getDisplayName() {
    return NAME;
  }

  public Configurable getConfigurable() {
    if (myPerforceConfigurable == null) {
      myPerforceConfigurable = new PerforceConfigurable(myProject);
    }
    return myPerforceConfigurable;
  }

  private <T> T validProvider(T initialValue, T disabledValue) {
    if (!mySettings.ENABLED) {
      return disabledValue;
    }
    else {
      return initialValue;
    }
  }

  private <T> T validProvider(T initialValue) {
    return validProvider(initialValue, null);
  }

  public void directoryMappingChanged() {
    PerforceConnectionManager.getInstance(getProject()).updateConnections();
  }

  @NotNull
  public EditFileProvider getEditFileProvider() {
    if (PerforceSettings.getSettings(myProject).ENABLED &&
        cannotConnectToPerforceServer(PerforceManager.getInstance(getProject()))) {
      return myUnavailableEditFileProvider;
    }
    else {
      return myMyEditFileProvider;
    }
  }

  public CheckinEnvironment getCheckinEnvironment() {
    return validProvider(getOfflineCheckinEnvironment());
  }

  public CheckinEnvironment getOfflineCheckinEnvironment() {
    if (myPerforceCheckinEnvironment == null) {
      myPerforceCheckinEnvironment = new PerforceCheckinEnvironment(myProject);
    }
    return myPerforceCheckinEnvironment;
  }

  @NotNull
  public RollbackEnvironment getRollbackEnvironment() {
    if (PerforceSettings.getSettings(myProject).ENABLED) {
      if (myPerforceRollbackEnvironment == null) {
        myPerforceRollbackEnvironment = new PerforceRollbackEnvironment(myProject);
      }
      return myPerforceRollbackEnvironment;
    }
    else {
      if (myOfflineRollbackEnvironment == null) {
        myOfflineRollbackEnvironment = new PerforceOfflineRollbackEnvironment(myProject);
      }
      return myOfflineRollbackEnvironment;
    }
  }

  private void autoEditVFile(final VirtualFile[] vFiles) {
    for (VirtualFile vFile : vFiles) {
      final P4File p4File = P4File.create(vFile);

      final String complaint = getFileNameComplaint(p4File);
      if (complaint != null) {
        LOG.info(complaint);
        return;
      }
    }

    // check whether it will be under any clientspec

    if (VcsConfiguration.getInstance(myProject).PERFORM_EDIT_IN_BACKGROUND || !PerforceSettings.getSettings(myProject).ENABLED) {
      ApplicationManager.getApplication().runWriteAction(new Runnable() {
        public void run() {
          for (VirtualFile file : vFiles) {
            try {
              ReadOnlyAttributeUtil.setReadOnlyAttribute(file, false);
            }
            catch (IOException e) {
              // ignore - we'll get some message from 'p4 revert'
            }
          }
        }
      });
      synchronized (myAsyncEditFiles) {
        Collections.addAll(myAsyncEditFiles, vFiles);
      }
    }

    List<VcsOperation> operations = new ArrayList<VcsOperation>();
    for(VirtualFile vFile: vFiles) {
      operations.add(new P4EditOperation(myChangeListManager.getDefaultChangeList().getName(), vFile));
    }
    runOrQueue(operations, PerforceBundle.message("progress.title.perforce.edit"), VcsConfiguration.getInstance(myProject).getEditOption());
  }

  // todo this actually can be executed not in queue but in parrallel. CHECK
  public void runOrQueue(final List<VcsOperation> operations, final String title, final PerformInBackgroundOption option) {
    if (mySettings.ENABLED) {
      VcsBackgroundTask<VcsOperation> task = new VcsBackgroundTask<VcsOperation>(myProject, title, option, operations) {
        protected void process(final VcsOperation op) throws VcsException {
          op.execute(myProject);
        }
      };
      myTaskQueue.run(task);
    }
    else {
      List<VcsException> exceptions = new ArrayList<VcsException>();
      for(VcsOperation op: operations) {
        try {
          op.executeOrLog(myProject);
        }
        catch (VcsException e) {
          exceptions.add(e);
        }
      }
      if (!exceptions.isEmpty()) {
        AbstractVcsHelper.getInstance(myProject).showErrors(exceptions, title);
      }
    }
  }

  public FStat getFstatSafe(final P4File p4File) throws VcsException {
    final Ref<PerforceManager> refManager = new Ref<PerforceManager>();
    final Ref<PerforceRunner> refRunner = new Ref<PerforceRunner>();
    // the read action ensures that the project will not get disposed in the middle of execution
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        if (!myProject.isDisposed()) {
          refManager.set(PerforceManager.getInstance(myProject));
          refRunner.set(PerforceRunner.getInstance(myProject));
        }
      }
    });
    if (!refManager.isNull()) {
      return p4File.getFstat(refManager.get(), myChangeListManager, refRunner.get(), true);
    }
    return null;
  }

  public boolean isAsyncEditFile(VirtualFile file) {
    synchronized (myAsyncEditFiles) {
      return myAsyncEditFiles.contains(file);
    }
  }

  public void asyncEditCompleted(VirtualFile file) {
    synchronized (myAsyncEditFiles) {
      myAsyncEditFiles.remove(file);
    }
  }

  public static PerforceVcs getInstance(Project project) {
    return (PerforceVcs)ProjectLevelVcsManager.getInstance(project).findVcsByName(NAME);
  }


  public UpdateEnvironment getUpdateEnvironment() {
    if (myPerforceUpdateEnvironment == null) {
      myPerforceUpdateEnvironment = new PerforceUpdateEnvironment(myProject);
    }
    return validProvider(myPerforceUpdateEnvironment);
  }

  public PerforceSettings getSettings() {
    return mySettings;
  }

  @Nullable
  static public String getFileNameComplaint(P4File file) {
    String filename = file.getLocalPath();
    if (filename.indexOf('#') >= 0 || filename.indexOf('@') >= 0) {
      return PerforceBundle.message("message.text.invalid.character.in.file.name", filename);
    }
    return null;
  }

  private class MyEditFileProvider implements EditFileProvider {
    public void editFiles(VirtualFile[] files) {
      autoEditVFile(files);
    }

    public String getRequestText() {
      return PerforceBundle.message("confirmation.text.open.files.for.edit");
    }

  }

  public boolean fileIsUnderVcs(FilePath filePath) {
    VirtualFile virtualFile = filePath.getVirtualFile();
    if (virtualFile == null) return false;

    if (virtualFile.isDirectory()) {
      if (PerforceConnectionManager.getInstance(myProject).isInitializingConnections()) return true;
      return PerforceManager.getInstance(getProject()).isUnderPerforceRoot(virtualFile);
    }
    else {
      final P4File p4File = P4File.create(virtualFile);

      try {
        return PerforceRunner.getInstance(myProject).have(p4File);
      }
      catch (VcsException e) {
        return false;
      }
    }
  }

  public boolean fileExistsInVcs(FilePath filePath) {
    VirtualFile virtualFile = filePath.getVirtualFile();
    if (virtualFile == null) return false;

    final PerforceManager perforceManager = PerforceManager.getInstance(getProject());

    if (!mySettings.ENABLED || cannotConnectToPerforceServer(perforceManager)) {
      return true;
    }

    if (virtualFile.isDirectory()) {
      return perforceManager.isUnderPerforceRoot(virtualFile);
    }
    else {
      final FileStatus fileStatus = ChangeListManager.getInstance(myProject).getStatus(virtualFile);
      return fileStatus != FileStatus.UNKNOWN && fileStatus != FileStatus.ADDED;
    }
  }

  private boolean cannotConnectToPerforceServer(final PerforceManager perforceManager) {
    final List<P4Connection> allConnections = PerforceSettings.getSettings(myProject).getAllConnections();
    for (P4Connection connection : allConnections) {
      final List<String> clientRoots = perforceManager.getCachedInfo(connection).get(PerforceRunner.CLIENT_ROOT);
      if (clientRoots == null || clientRoots.isEmpty()) return true;
    }
    return false;
  }

  public VcsHistoryProvider getVcsHistoryProvider() {
    if (myHistoryProvider == null) {
      myHistoryProvider = new PerforceVcsHistoryProvider(this);
    }
    return validProvider(myHistoryProvider);
  }

  public VcsHistoryProvider getVcsBlockHistoryProvider() {
    return getVcsHistoryProvider();
  }

  public AnnotationProvider getAnnotationProvider() {
    if (myAnnotationProvider == null) {
      myAnnotationProvider = new PerforceAnnotationProvider(myProject);
    }
    return validProvider(myAnnotationProvider);
  }

  public DiffProvider getDiffProvider() {
    if (myDiffProvider == null) {
      myDiffProvider = new PerforceDiffProvider(myProject);
    }
    return validProvider(myDiffProvider);
  }

  public ChangeProvider getChangeProvider() {
    if (mySettings.ENABLED) {
      if (myChangeProvider == null) {
        myChangeProvider = new PerforceChangeProvider(this);
      }
      return myChangeProvider;
    }
    else {
      if (myOfflineChangeProvider == null) {
        myOfflineChangeProvider = new PerforceOfflineChangeProvider(myProject);
      }
      return myOfflineChangeProvider;
    }
  }

  public VcsShowConfirmationOption getAddOption() {
    return myAddOption;
  }

  public VcsShowConfirmationOption getRemoveOption() {
    return myRemoveOption;
  }

  private class DisabledFileProvider implements EditFileProvider {
    private final String myMessage;
    private final boolean myCanEnableIntegration;

    public DisabledFileProvider(final String message, final boolean canEnableIntegration) {
      myMessage = message;
      myCanEnableIntegration = canEnableIntegration;
    }

    public void editFiles(VirtualFile[] files) throws VcsException {
      final int answer;
      if (myCanEnableIntegration) {
        answer = Messages.showDialog(myProject, myMessage, PerforceBundle.message("message.title.cannot.edit"), new String[]{
          PerforceBundle.message("perforce.edit.clear.using.fs.button"), PerforceBundle.message("action.PerforceEnableIntegration.text"),
          CommonBundle.getCancelButtonText()}, 0, Messages.getWarningIcon());
      }
      else {
        answer =
          Messages.showYesNoDialog(myProject, myMessage, PerforceBundle.message("message.title.cannot.edit"), Messages.getWarningIcon());
      }

      if (answer == 0) {
        for (final VirtualFile file : files) {
          final IOException[] ex = new IOException[1];
          ApplicationManager.getApplication().runWriteAction(new Runnable() {
            public void run() {
              try {
                ReadOnlyAttributeUtil.setReadOnlyAttribute(file, false);
              }
              catch (IOException e) {
                ex[0] = e;
              }
            }
          });
          if (ex[0] != null) {
            throw new VcsException(ex[0]);
          }
        }
      }
      else if (myCanEnableIntegration && answer == 1) {
        PerforceSettings.getSettings(myProject).enable();
        myMyEditFileProvider.editFiles(files);
      }
    }

    public String getRequestText() {
      return PerforceBundle.message("confirmation.text.open.files.for.edit");
    }
  }

  public UpdateEnvironment getIntegrateEnvironment() {
    if (myPerforceIntegrateEnvironment == null) {
      myPerforceIntegrateEnvironment = new PerforceIntegrateEnvironment(myProject);
    }
    return validProvider(myPerforceIntegrateEnvironment);
  }

  public void activate() {
    if (myVFSListener == null) {
      myVFSListener = new PerforceVFSListener(myProject);
    }
    PerforceManager.getInstance(myProject).startListening();
    PerforceConnectionManager.getInstance(myProject).startListening();
    ChangeListSynchronizer.getInstance(myProject).startListening();
  }

  public void deactivate() {
    if (myVFSListener != null) {
      Disposer.dispose(myVFSListener);
      myVFSListener = null;
    }
    ChangeListSynchronizer.getInstance(myProject).stopListening();
    PerforceConnectionManager.getInstance(myProject).stopListening();
    PerforceManager.getInstance(myProject).stopListening();
  }

  @Override
  @Nullable
  public CommittedChangesProvider<PerforceChangeList, PerforceChangeBrowserSettings> getCommittedChangesProvider() {
    if (myCommittedChangesProvider == null) {
      myCommittedChangesProvider = new PerforceCommittedChangesProvider(myProject);
    }
    return myCommittedChangesProvider;
  }

  // todo this actually can be executed not in queue but in parrallel. CHECK
  public void runTask(Task.Backgroundable task) {
    myTaskQueue.run(task);
  }

  @Override
  @Nullable
  public VcsRevisionNumber parseRevisionNumber(final String revisionNumberString) {
    long revision;
    try {
      revision = Long.parseLong(revisionNumberString);
    }
    catch (NumberFormatException ex) {
      return null;
    }
    return new VcsRevisionNumber.Long(revision);
  }

  @Override
  public String getRevisionPattern() {
    return ourIntegerPattern;
  }

  @Override
  public MergeProvider getMergeProvider() {
    if (myMergeProvider == null) {
      myMergeProvider = new PerforceMergeProvider(myProject);
    }
    return myMergeProvider;
  }

  @Override
  public ChangeListEditHandler getEditHandler() {
    return myChangeListEditHandler;
  }

  public Map<ConnectionKey, java.util.List<PerforceJob>> getDefaultAssociated() {
    synchronized (myDefaultAssociated) {
      return new HashMap<ConnectionKey, List<PerforceJob>>(myDefaultAssociated);
    }
  }

  public void setDefaultAssociated(final Map<ConnectionKey, java.util.List<PerforceJob>> jobs) {
    synchronized (myDefaultAssociated) {
      myDefaultAssociated.clear();
      myDefaultAssociated.putAll(jobs);
    }
  }

  public void clearDefaultAssociated() {
    synchronized (myDefaultAssociated) {
      myDefaultAssociated.clear();
    }
  }

  @Override
  public boolean isVersionedDirectory(VirtualFile dir) {
    return false;
  }

  @Override
  public boolean supportsVersionedStateDetection() {
    return false;
  }

  @Override
  public VcsExceptionsHotFixer getVcsExceptionsHotFixer() {
    return myHotFixer;
  }

  public static VcsKey getKey() {
    return ourKey;
  }

  public Collection<Pair<P4Connection, Collection<VirtualFile>>> getRootsByConnections() {
    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(myProject);
    final PerforceConnectionManager connectionManager = PerforceConnectionManager.getInstance(myProject);
    final VirtualFile[] roots = vcsManager.getRootsUnderVcs(this);

    final MultiMap<ConnectionKey, VirtualFile> rootsByConnections = new MultiMap<ConnectionKey, VirtualFile>();
    final Map<ConnectionKey, P4Connection> connMap = new HashMap<ConnectionKey, P4Connection>();
    for (VirtualFile root : roots) {
      final P4Connection connection = connectionManager.getConnectionForFile(root);
      if (connection != null) {
        final ConnectionKey key = ConnectionKey.create(myProject, connection);
        connMap.put(key, connection);
        rootsByConnections.putValue(key, root);
      }
    }

    final Collection<Pair<P4Connection, Collection<VirtualFile>>> result = new LinkedList<Pair<P4Connection, Collection<VirtualFile>>>();
    for (ConnectionKey key : rootsByConnections.keySet()) {
      final P4Connection connection = connMap.get(key);
      result.add(new Pair<P4Connection, Collection<VirtualFile>>(connection, rootsByConnections.get(key)));
    }

    return result;
  }
}
