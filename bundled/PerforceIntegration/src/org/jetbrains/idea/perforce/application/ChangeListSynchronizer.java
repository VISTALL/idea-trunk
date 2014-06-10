/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.containers.Convertor;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.operations.P4MoveToChangeListOperation;
import org.jetbrains.idea.perforce.operations.VcsOperationLog;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.commandWrappers.DeleteEmptyChangeList;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;

import java.io.File;
import java.util.*;

/**
 * @author yole
 */
@SuppressWarnings({"UnnecessaryFullyQualifiedName"})
public class ChangeListSynchronizer extends AbstractProjectComponent implements JDOMExternalizable, ChangeListListener, ChangeListDecorator {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.ChangeListSynchronizer");
  @NonNls private static final String ELEMENT_CONNECTION = "connection";
  @NonNls private static final String ATTRIBUTE_SERVER = "server";
  @NonNls private static final String ATTRIBUTE_CLIENT = "client";
  @NonNls private static final String ATTRIBUTE_USER = "user";
  @NonNls private static final String ELEMENT_CHANGELIST = "changelist";
  @NonNls private static final String ATTRIBUTE_NAME = "name";
  @NonNls private static final String ATTRIBUTE_NUMBER = "number";

  private final EnsureIdeaEditsApplied myEditsWaiter;

  public static ChangeListSynchronizer getInstance(Project project) {
    return project.getComponent(ChangeListSynchronizer.class);
  }

  public static ChangeListSynchronizer getInstanceChecked(final Project project) {
    return ApplicationManager.getApplication().runReadAction(new Computable<ChangeListSynchronizer>() {
      public ChangeListSynchronizer compute() {
        if (project.isDisposed()) throw new ProcessCanceledException();
        return project.getComponent(ChangeListSynchronizer.class);
      }
    });
  }

  private final PerforceVcs myVcs;
  private PerforceManager myPerforceManager;
  private final ChangeListManager myChangeListManager;
  private final ProjectLevelVcsManager myVcsManager;
  private final PerforceConnectionManager myConnectionManager;
  private String myLastCommittedChangelistName;
  private final Map<ConnectionKey, BidirectionalMap<String, Long>> myChangeListMap = new HashMap<ConnectionKey, BidirectionalMap<String, Long>>();
  private boolean myUpdatingAfterActivation;
  private boolean mySynchronizingName;
  private boolean myDeleteEmptyChangelistsRequested = false;

  private final PerforceActivityListener myActivityListener = new PerforceActivityListener() {
    public void changeListSubmitted(P4Connection connection, long changeListNumber, final long newNumber) {
      if (changeListNumber != -1) {
        processChangeListSubmitted(connection, changeListNumber, newNumber);
      }
    }
  };

  public ChangeListSynchronizer(final Project project, final ChangeListManager changeListManager,
                                final ProjectLevelVcsManager vcsManager) {
    super(project);
    myChangeListManager = changeListManager;
    myVcsManager = vcsManager;
    myConnectionManager = PerforceConnectionManager.getInstance(project);
    myEditsWaiter = new EnsureIdeaEditsApplied(myProject);
    myVcs = PerforceVcs.getInstance(project);
  }

  private void createDefaultChangeList() {
    LocalChangeList result = myChangeListManager.findChangeList(getDefaultChangeListName());
    if (result != null) {
      myChangeListManager.setReadOnly(result.getName(), true);
    }
    else {
      final LocalChangeList changeList = myChangeListManager.addChangeList(getDefaultChangeListName(), "");
      myChangeListManager.setReadOnly(changeList.getName(), true);
    }
  }

  static String getDefaultChangeListName() {
    return VcsBundle.message("changes.default.changlist.name");
  }

  public void startListening() {
    if (myPerforceManager == null) {
      myPerforceManager = PerforceManager.getInstance(myProject);
    }
    createDefaultChangeList();
    myChangeListManager.addChangeListListener(this);
    PerforceSettings.getSettings(myProject).addActivityListener(myActivityListener);
  }

  public void stopListening() {
    myChangeListManager.removeChangeListListener(this);
    PerforceSettings.getSettings(myProject).removeActivityListener(myActivityListener);
  }

  @NotNull
  @NonNls
  public String getComponentName() {
    return "ChangeListSynchronizer";
  }

  public void readExternal(Element element) {
    for(Object connElementObj: element.getChildren(ELEMENT_CONNECTION)) {
      Element connElement = (Element) connElementObj;
      ConnectionKey key = new ConnectionKey(connElement.getAttributeValue(ATTRIBUTE_SERVER),
                                            connElement.getAttributeValue(ATTRIBUTE_CLIENT),
                                            connElement.getAttributeValue(ATTRIBUTE_USER));
      BidirectionalMap<String, Long> map = new BidirectionalMap<String, Long>();
      myChangeListMap.put(key, map);
      for(Object listElementObj: connElement.getChildren(ELEMENT_CHANGELIST)) {
        Element listElement = (Element) listElementObj;
        final String name = listElement.getAttributeValue(ATTRIBUTE_NAME);
        if (!map.containsKey(name)) {
          map.put(name, Long.parseLong(listElement.getAttributeValue(ATTRIBUTE_NUMBER)));
        }
      }
    }
  }

  public void writeExternal(Element element) {
    for(ConnectionKey key: myChangeListMap.keySet()) {
      Element connElement = new Element(ELEMENT_CONNECTION);
      connElement.setAttribute(ATTRIBUTE_SERVER, key.server);
      connElement.setAttribute(ATTRIBUTE_CLIENT, key.client);
      connElement.setAttribute(ATTRIBUTE_USER, key.user);
      element.addContent(connElement);
      Map<String, Long> map = myChangeListMap.get(key);
      for(String name: map.keySet()) {
        Element listElement = new Element(ELEMENT_CHANGELIST);
        listElement.setAttribute(ATTRIBUTE_NAME, name);
        listElement.setAttribute(ATTRIBUTE_NUMBER, map.get(name).toString());
        connElement.addContent(listElement);
      }
    }
  }

  public void changeListAdded(ChangeList list) {
  }

  public void changesRemoved(final Collection<Change> changes, final ChangeList fromList) {
  }

  public void changeListRemoved(final ChangeList list) {
    if (myUpdatingAfterActivation) return;
    if (Comparing.equal(myLastCommittedChangelistName, list.getName())) {
      myLastCommittedChangelistName = null;
    }
    else {
      final String changeListName = list.getName();
      runUnderProgress("Remove P4 changelist", new Runnable() {
        public void run() {
          handleChangeListDelete(changeListName);
        }
      });
    }
  }

  private void handleChangeListDelete(final String changeListName) {
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    for(P4Connection connection: settings.getAllConnections()) {
      final PerforceClient perforceClient = myPerforceManager.getClient(connection);
      if (!isValidClient(perforceClient)) continue;
      Map<String, Long> map = getChangeListMap(perforceClient);
      Long value = map.get(changeListName);
      if (value != null) {
        try {
          final PerforceRunner runner = PerforceRunner.getInstance(myProject);
          final DeleteEmptyChangeList deleteCommand = runner.deleteEmptyChangeList(connection, value.longValue());
          deleteCommand.allowError(DeleteEmptyChangeList.NOT_FOUND);
          deleteCommand.run();
        }
        catch (VcsException e) {
          AbstractVcsHelper.getInstanceChecked(myProject).showError(e, "P4 changelist delete error");
          LOG.info(e);
        }
        map.remove(changeListName);
      }
    }
  }

  private static boolean isValidClient(final PerforceClient perforceClient) {
    return perforceClient.getName() != null && perforceClient.getUserName() != null && perforceClient.getServerPort() != null;
  }

  public void changeListChanged(final ChangeList list) {
  }

  private void checkReopenChangesAsync(final Collection<Change> changes, final ChangeList list) {
    // to avoid deadlock
    final Collection<Change> changesToProcess;
    if (changes == null) {
      changesToProcess = list.getChanges();
    }
    else {
      changesToProcess = new ArrayList<Change>();
      for(Change c: changes) {
        if (myChangeListManager.getChangeList(c) != null) {
          changesToProcess.add(c);
        }
      }
    }

    runUnderProgress("Move changes to P4 changelist", new Runnable() {
      public void run() {
        if (myProject.isDisposed()) return;
        LOG.debug("changeListChanged in event dispatch thread: " + list.getName());
        checkReopenChanges(changesToProcess, list);
      }
    });
  }

  public void changeListRenamed(final ChangeList list, final String oldName) {
    if (!mySynchronizingName) {
      runUnderProgress("Change P4 changelist name", new Runnable() {
        public void run() {
          handleChangeListRename(list, oldName);
        }
      });
    }
  }

  public void changeListCommentChanged(final ChangeList list, final String oldComment) {
    if ((!mySynchronizingName) && (! getDefaultChangeListName().equals(list.getName()))) {
      runUnderProgress("Change P4 changelist comment", new Runnable() {
        public void run() {
          handleChangeListRename(list, list.getName());
        }
      });
    }
  }

  // shortcut
  private void runUnderProgress(final String title, final Runnable runnable) {
    final ProgressManager progress = ProgressManager.getInstance();
    final Runnable wrapper = new Runnable() {
      public void run() {
        final ProgressIndicator indicator = progress.getProgressIndicator();
        if (indicator != null) {
          indicator.setIndeterminate(true);
          indicator.setText2(title + ": Waiting for pending P4 commands to complete");
          indicator.checkCanceled();
        }
        myEditsWaiter.ensure(new Runnable() {
          public void run() {
            if (indicator != null) {
              indicator.setText2(title);
              indicator.checkCanceled();
            }
            runnable.run();
          }
        });
      }
    };

    final ProgressIndicator indicator = progress.getProgressIndicator();
    if (((progress.hasProgressIndicator() || progress.hasModalProgressIndicator()) && (indicator != null)) ||
        (! ApplicationManager.getApplication().isDispatchThread())) {
      // can be when invoking notifications after ChangeListManager updated lists
      // just update text
      wrapper.run();
    } else {
      progress.run(new Task.Backgroundable(myProject, title, true, PerformInBackgroundOption.DEAF) {
        public void run(@NotNull final ProgressIndicator indicator) {
          wrapper.run();
        }
      });
    }
  }

  private void handleChangeListRename(final ChangeList list, final String oldName) {
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    Set<ConnectionKey> processedKeys = new HashSet<ConnectionKey>();
    for(P4Connection connection: settings.getAllConnections()) {
      final PerforceClient client = myPerforceManager.getClient(connection);
      if (!isValidClient(client)) continue;
      ConnectionKey key = new ConnectionKey(client);
      if (processedKeys.contains(key)) continue;
      processedKeys.add(key);

      final BidirectionalMap<String, Long> changeListMap = getChangeListMap(client);
      Long number = changeListMap.get(oldName);
      if (number != null) {
        try {
          PerforceRunner.getInstance(myProject).renameChangeList(number.longValue(), getP4Description(list), connection);
        }
        catch(VcsException e) {
          AbstractVcsHelper.getInstanceChecked(myProject).showError(e, "P4 changelist rename error");
          LOG.info(e);
        }
        changeListMap.remove(oldName);
        LOG.assertTrue(!changeListMap.containsValue(number));
        changeListMap.put(list.getName(), number);
      }
    }
  }

  private void checkReopenChanges(final Collection<Change> changesToProcess, final ChangeList inList) {
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);

    if (! settings.ENABLED) {
      // todo: why we don't filter here???
      // todo: and: make transparent
      for(final Change change: changesToProcess) {
        VcsOperationLog.getInstance(myProject).addToLog(new P4MoveToChangeListOperation(change, inList.getName()));
      }
      return;
    }

    final PerforceChangeGatherer<RenameGroup> gatherer =
      new PerforceChangeGatherer<RenameGroup>(myProject, new Convertor<P4Connection, RenameGroup>() {
        public RenameGroup convert(P4Connection o) {
          return new RenameGroup(o);
        }
      });
    final List<Change> changesUnderPerforce = new LinkedList<Change>();
    for(final Change change: changesToProcess) {
      AbstractVcs vcs = ApplicationManager.getApplication().runReadAction(new Computable<AbstractVcs>() {
        public AbstractVcs compute() {
          return ChangesUtil.getVcsForChange(change, myProject);
        }
      });
      if (vcs != null && PerforceVcs.getKey().equals(vcs.getKeyInstanceMethod())) {
        changesUnderPerforce.add(change);
      }
    }

    try {
      gatherer.execute(changesUnderPerforce);

      final PerforceRunner runner = PerforceRunner.getInstance(myProject);

      final Map<ConnectionKey, RenameGroup> connectionMap = gatherer.getByConnectionMap();
      for (Map.Entry<ConnectionKey, RenameGroup> entry : connectionMap.entrySet()) {
        final ConnectionKey key = entry.getKey();
        final P4Connection connection = entry.getValue().getConnection();
        final long changeListNumber = findOrCreatePerforceChangeList(connection, inList);
        runner.reopen(connection, entry.getValue().getChanges(), changeListNumber);
      }
    }
    catch (VcsException e) {
      LOG.info(e);
    }
  }

  private static class RenameGroup implements PerforceChangesForConnection {
    private final P4Connection myConnection;
    private final List<PerforceChange> myChanges;

    private RenameGroup(final P4Connection connection) {
      myConnection = connection;
      myChanges = new LinkedList<PerforceChange>();
    }

    public P4Connection getConnection() {
      return myConnection;
    }

    public void addChanges(final Collection<PerforceChange> changes) {
      myChanges.addAll(changes);
    }

    public List<PerforceChange> getChanges() {
      return myChanges;
    }
  }

  public void changesMoved(Collection<Change> changes, ChangeList fromList, ChangeList toList) {
    if (!Comparing.equal(fromList.getName(), myLastCommittedChangelistName) && !myUpdatingAfterActivation) {
      /*final LocalChangeListImpl toListImpl = (LocalChangeListImpl) toList;
      if (toListImpl.getEditHandler() == null) {
        toListImpl.setComment(myEditHandler.correctCommentWhenInstalled(toListImpl.getName(), toListImpl.getComment()));
        toListImpl.setEditHandler(myEditHandler);
        // for list inside change list manager, will be installed
      }*/
      checkReopenChangesAsync(changes, toList);
    }
  }

  public long findOrCreatePerforceChangeList(final P4Connection connection, final ChangeList list) throws VcsException {
    if (list.getName().equals(getDefaultChangeListName())) {
      return -1;
    }
    final PerforceClient client = myPerforceManager.getClient(connection);
    if (!isValidClient(client)) {
      return -1;
    }
    BidirectionalMap<String, Long> nameToChangelistMap = getChangeListMap(client);
    if (!nameToChangelistMap.containsKey(list.getName())) {
      return createListInPerforce(list, connection);
    }
    return nameToChangelistMap.get(list.getName()).longValue();
  }

  private long createListInPerforce(final ChangeList list, final P4Connection connection) throws VcsException {
    final PerforceClient client = myPerforceManager.getClient(connection);
    if (!isValidClient(client)) {
      return -1;
    }
    BidirectionalMap<String, Long> nameToChangelistMap = getChangeListMap(client);

    String description = getP4Description(list);
    long changeListNumber = PerforceRunner.getInstance(myProject).createChangeList(description, connection, null);
    LOG.assertTrue(!nameToChangelistMap.containsValue(changeListNumber));
    nameToChangelistMap.put(list.getName(), changeListNumber);
    return changeListNumber;
  }

  private static String getP4Description(final ChangeList list) {
    String description = list.getComment().trim();
    if (description.length() == 0) description = list.getName();
    return description;
  }

  @NotNull
  private BidirectionalMap<String, Long> getChangeListMap(final PerforceClient client) {
    ConnectionKey key = new ConnectionKey(client);
    BidirectionalMap<String, Long> nameToChangelistMap = myChangeListMap.get(key);
    if (nameToChangelistMap == null) {
      nameToChangelistMap = new BidirectionalMap<String, Long>();
      myChangeListMap.put(key, nameToChangelistMap);
    }
    return nameToChangelistMap;
  }

  public void defaultListChanged(final ChangeList oldDefaultList, ChangeList newDefaultList) {
  }

  public void unchangedFileStatusChanged() {
  }

  public void changeListUpdateDone() {
    // if the activation detected that a complete update was required, it will leave myUpdatingAfterActivation as true and
    // queue an async update => we need to reset the flag here
    myUpdatingAfterActivation = false;
    if (myDeleteEmptyChangelistsRequested) {
      myDeleteEmptyChangelistsRequested = true;
      checkDeleteAllEmptyChangelists();
    }
  }

  private void processChangeListSubmitted(final P4Connection connection, final long changeListNumber, final long newNumber) {
    LOG.debug("processChangeListSubmitted: changeListNumber=" + changeListNumber + ", newNumber=" + newNumber);
    final LocalChangeList changeList = findChangeList(connection, changeListNumber);
    if (changeList == null) {
      LOG.debug("Failed to find submitted changelist by name");
      return;
    }
    final String changeListName = changeList.getName();
    BidirectionalMap<String, Long> changeListMap = getChangeListMap(myPerforceManager.getClient(connection));
    if (newNumber == -1) {
      changeListMap.remove(changeListName);
      if (changeList == null || !changeList.isDefault()) {
        // the changelist is going to be deleted, and we shouldn't handle subsequent changesMoved notifications as attempts to move changes
        myLastCommittedChangelistName = changeListName;
      }
    }
    else {
      LOG.debug("updated changeListMap");
      LOG.assertTrue(!changeListMap.containsValue(newNumber));
      changeListMap.put(changeListName, newNumber);
    }
  }

  @Nullable
  private String findChangeListName(final P4Connection connection, final long changeListNumber) {
    return findChangeListName(connection, changeListNumber, null);
  }

  // todo here we should invalidate changes???
  @Nullable
  private String findChangeListName(final P4Connection connection, final long changeListNumber, final String modifyTo) {
    final PerforceClient perforceClient = myPerforceManager.getClient(connection);
    if (!isValidClient(perforceClient)) {
      return null;
    }
    BidirectionalMap<String, Long> changeListMap = getChangeListMap(perforceClient);
    String changeListName = null;
    final List<String> list = changeListMap.getKeysByValue(changeListNumber);
    if (list != null) {
      LOG.assertTrue(list.size() <= 1);
      if (list.size() > 0) {
        changeListName = list.get(0);
/*        if ((modifyTo != null) && (! modifyTo.equals(changeListName))) {
          // remove old
          changeListMap.remove(changeListName);
          // let it be added where appropriate
          return null;
        }*/
      }
    }
    return changeListName;
  }

  @Nullable
  public String getListNameAndCorrect(final P4Connection connection, final long changeListNumber, final String p4name) {
    if (changeListNumber == -1) {
      return getDefaultChangeListName();
    }
    final String name = findChangeListName(connection, changeListNumber, p4name);
    if (name == null) {
      LOG.debug("Couldn't find name " + p4name + " for changelist with number " + changeListNumber);
    }
    return name;
  }

  @Nullable
  public String getListName(final P4Connection connection, final long changeListNumber) {
    if (changeListNumber == -1) {
      return getDefaultChangeListName();
    }
    final String name = findChangeListName(connection, changeListNumber);
    if (name == null) {
      LOG.debug("Couldn't find name for changelist with number " + changeListNumber);
    }
    return name;
  }

  @Nullable
  public LocalChangeList findChangeList(final P4Connection connection, final long changeListNumber) {
    if (changeListNumber == -1) {
      return myChangeListManager.findChangeList(getDefaultChangeListName());
    }
    String name = findChangeListName(connection, changeListNumber);
    if (name == null) {
      LOG.debug("Couldn't find name for changelist with number " + changeListNumber);
      return null;
    }
    return myChangeListManager.findChangeList(name);
  }

  public Pair<String, String> createChangeListByDescription(final P4Connection connection,
                                                  String changeListDescription,
                                                  final long changeListNumber) {
    final PerforceClient perforceClient = myPerforceManager.getClient(connection);
    if (!isValidClient(perforceClient)) {
      return new Pair<String, String>(myChangeListManager.getDefaultListName(), "");
    }
    ConnectionKey key = new ConnectionKey(perforceClient);
    BidirectionalMap<String, Long> changeListMap = myChangeListMap.get(key);

    final PerforceNameCommentConvertor convertor = PerforceNameCommentConvertor.fromNative(changeListDescription);
    
    changeListMap.put(convertor.getIdeaName(), changeListNumber);
    return new Pair<String, String>(convertor.getIdeaName(), convertor.getIdeaComment());
  }

  public void queueUpdateOpenedFiles() {
    myPerforceManager.queueUpdateRequest(new Runnable() {
      public void run() {
        updateOpenedFiles();
      }
    });
  }

  /**
   * Runs the 'p4 opened' command to see if any updates to the changes tree are required.
   *
   * @return true if all changes have been processed, false if some changes cannot be processed and a complete
   * Changes view update is required.
   */
  public boolean updateOpenedFiles() {
    if (myProject.isDisposed()) return true;
    myUpdatingAfterActivation = true;
    if (myPerforceManager == null) {
      myPerforceManager = PerforceManager.getInstance(myProject);
    }

    final VirtualFile[] roots = myVcsManager.getRootsUnderVcs(myVcs);
    for (VirtualFile vcsRoot : roots) {
      final P4Connection connection = myConnectionManager.getConnectionForFile(vcsRoot);
      if (connection == null) continue;
      final PerforceClient client = myPerforceManager.getClient(connection);
      if (!isValidClient(client)) continue;
      
      final ConnectionKey key = new ConnectionKey(client);

      final List<PerforceChangeList> pendingChangeLists;
      try {
        pendingChangeLists = PerforceRunner.getInstance(myProject).getPendingChangeListsUnderRoot(connection, vcsRoot);
      }
      catch (VcsException e) {
        LOG.info(e);
        continue;
      }
      final List<PerforceChange> opened;
      try {
        opened = PerforceRunner.getInstance(myProject).openedUnderRoot(connection, vcsRoot);
      }
      catch (VcsException e) {
        if (e.getMessage() != null && e.getMessage().length() > 0) {
          LOG.info(e);
        }
        continue;
      }
      for(PerforceChange openedFile: opened) {
        // todo check what inside
        if (!updateOpenedFile(connection, client, openedFile, pendingChangeLists)) return false;
      }

      checkDeleteEmptyChangeLists(pendingChangeLists, key);
    }

    LOG.debug("updateOpenedFiles(): All changes matched successfully");
    myUpdatingAfterActivation = false;
    return true;
  }

  public void requestDeleteEmptyChangeLists() {
    myDeleteEmptyChangelistsRequested = true;
  }

  private void checkDeleteAllEmptyChangelists() {
    final PerforceSettings settings = PerforceSettings.getSettings(myProject);
    for(P4Connection connection: settings.getAllConnections()) {
      final PerforceClient client = myPerforceManager.getClient(connection);
      if (!isValidClient(client)) continue;
      ConnectionKey key = new ConnectionKey(client);

      final List<PerforceChangeList> pendingChangeLists;
      try {
        pendingChangeLists = PerforceRunner.getInstance(myProject).getPendingChangeLists(connection);
      }
      catch (VcsException e) {
        LOG.info(e);
        continue;
      }

      checkDeleteEmptyChangeLists(pendingChangeLists, key);
    }
  }

  private void checkDeleteEmptyChangeLists(final List<PerforceChangeList> pendingChangeLists, final ConnectionKey key) {
    BidirectionalMap<String, Long> nameToChangelistMap = myChangeListMap.get(key);
    if (nameToChangelistMap != null) {
      Set<String> names = new HashSet<String>(nameToChangelistMap.keySet());
      for(String name: names) {
        final LocalChangeList list = myChangeListManager.findChangeList(name);
        final Long number = nameToChangelistMap.get(name);
        if (list != null && number != null && !isPendingChangeList(number.longValue(), pendingChangeLists)) {
          removeChangeListIfEmpty(list);
          nameToChangelistMap.remove(name);
        }
      }
    }
  }

  private void removeChangeListIfEmpty(final LocalChangeList list) {
    if (list.isDefault() || list.isReadOnly()) {
      return;
    }
    for(final Change c: list.getChanges()) {
      final AbstractVcs vcsForChange = ApplicationManager.getApplication().runReadAction(new Computable<AbstractVcs>() {
        public AbstractVcs compute() {
          return ChangesUtil.getVcsForChange(c, myProject);
        }
      });
      if (!(vcsForChange instanceof PerforceVcs)) {
        // changelist still contains non-P4 changes - keep
        return;
      }
    }
    myChangeListManager.removeChangeList(list.getName());
  }

  private static boolean isPendingChangeList(final long number, final List<PerforceChangeList> list) {
    for(PerforceChangeList cl: list) {
      if (cl.getNumber() == number) return true;
    }
    return false;
  }

  private boolean updateOpenedFile(final P4Connection connection, final PerforceClient client,
                                   final PerforceChange fileChange, final List<PerforceChangeList> pendingChangeLists) {
    final File file;
    try {
      file = PerforceManager.getFileByDepotName(P4File.unescapeWildcards(fileChange.getDepotPath()), client);
    }
    catch (VcsException e) {
      LOG.info(e);
      return false;
    }

    if (file == null) {//cannot find local mapping for depot path
      return false;
    }

    // ignore changes for files not under content roots
    boolean isUnderProjectVcs = ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
      public Boolean compute() {
        VirtualFile parentFile = LocalFileSystem.getInstance().findFileByIoFile(file.getParentFile());
        if (parentFile == null) return false;
        final AbstractVcs vcs = myVcsManager.getVcsFor(parentFile);
        if (vcs == null || (! PerforceVcs.getKey().equals(vcs.getKeyInstanceMethod()))) return false;
        return true;
      }
    }).booleanValue();
    if (!isUnderProjectVcs) return true;

    LocalChangeList changeList = findChangeList(connection, fileChange.getChangeList());
    if (changeList == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("updateOpenedFile(): could not find changelist for number " + fileChange.getChangeList());
      }
      return false;
    }
    if (fileChange.getChangeList() != -1) {
      synchronizeChangeListName(client, changeList, fileChange.getChangeList(), pendingChangeLists);
    }
    for(Change change: changeList.getChanges()) {
      if (change.affectsFile(file)) {
        return true;
      }
    }

    List<LocalChangeList> oldChangeLists = myChangeListManager.getChangeLists();
    for(LocalChangeList oldChangeList: oldChangeLists) {
      if (oldChangeList.equals(changeList)) continue;
      for(Change change: oldChangeList.getChanges()) {
        if (change.affectsFile(file)) {
          myChangeListManager.moveChangesTo(changeList, new Change[] { change });
          return true;
        }
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("updateOpenedFile(): could not find changelist for change " + fileChange);
    }
    return false;
  }

  private void synchronizeChangeListName(final PerforceClient client, final LocalChangeList changeList, long number, final List<PerforceChangeList> pendingChangeLists) {
    final String changeListName = changeList.getName();

    for(PerforceChangeList perforceChangeList: pendingChangeLists) {
      if (perforceChangeList.getNumber() == number) {
        String description = perforceChangeList.getComment();
        if (!Comparing.equal(changeListName, description) && !Comparing.equal(changeList.getComment(), description)) {
          mySynchronizingName = true;
          final String newName;
          try {
            description = description.trim();
            String firstLine = getFirstLine(description);

            if (changeList.getComment().length() == 0) {
              if (firstLine != null) {
                newName = firstLine;
                myChangeListManager.editName(changeListName, firstLine);
                myChangeListManager.editComment(firstLine, description);
              }
              else {
                newName = description;
                myChangeListManager.editName(changeListName, description);
              }
            }
            else if (changeList.getComment() != null && Comparing.equal(changeListName, getFirstLine(changeList.getComment()))) {
              if (firstLine == null) {
                newName = description;
                myChangeListManager.editName(changeListName, description);
                myChangeListManager.editComment(description, "");
              } else {
                newName = firstLine;
                myChangeListManager.editName(changeListName, firstLine);
                myChangeListManager.editComment(firstLine, description);
              }
            }
            else {
              newName = changeListName;
              myChangeListManager.editComment(changeListName, description);
            }
          }
          finally {
            mySynchronizingName = false;
          }

          if (myChangeListManager.findChangeList(newName) != null && (! newName.equals(changeListName))) {
            final BidirectionalMap<String, Long> changeListMap = getChangeListMap(client);
            changeListMap.remove(changeListName);
            LOG.assertTrue(!changeListMap.containsValue(number));
            changeListMap.put(newName, number);
          }
        }
        break;
      }
    }
  }

  @Nullable
  private static String getFirstLine(final String description) {
    String firstLine = null;
    int pos = description.indexOf("\n");
    if (pos >= 0) {
      firstLine = description.substring(0, pos).trim() + "...";
    }
    return firstLine;
  }

  public long getListNumber(final ConnectionKey key, final LocalChangeList changeList) {
    if (changeList.getName().equals(getDefaultChangeListName())) return -1;

    final BidirectionalMap<String, Long> map = myChangeListMap.get(key);
    if (map != null) {
      final Long number = map.get(changeList.getName());
      return number == null ? -1 : number;
    }
    return -1;
  }

  public void decorateChangeList(LocalChangeList changeList, ColoredTreeCellRenderer cellRenderer, boolean selected, boolean expanded,
                                 boolean hasFocus) {
    if (changeList.getName().equals(getDefaultChangeListName())) return;
    for(BidirectionalMap<String, Long> map: myChangeListMap.values()) {
      Long value = map.get(changeList.getName());
      if (value != null) {
        //noinspection HardCodedStringLiteral
        cellRenderer.append(" - Perforce #" + value.toString(), SimpleTextAttributes.GRAY_ATTRIBUTES);
        break;
      }
    }
  }

  public long getActiveChangeListNumber(final P4Connection connection) throws VcsException {
    return findOrCreatePerforceChangeList(connection, myChangeListManager.getDefaultChangeList());
  }

  @Nullable
  public Long getChangeListNumber(final P4Connection connection, final ChangeList changeList) {
    if (changeList.getName().equals(getDefaultChangeListName())) return -1L;
    final PerforceClient perforceClient = myPerforceManager.getClient(connection);
    if (!isValidClient(perforceClient)) {
      LOG.debug("getChangeListNumber() returning null because description is not valid");
      return null;
    }
    final BidirectionalMap<String, Long> map = getChangeListMap(perforceClient);
    Long value = map.get(changeList.getName());
    if (value != null) return value.longValue();
    return null;
  }
}
