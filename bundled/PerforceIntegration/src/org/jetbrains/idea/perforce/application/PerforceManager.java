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

import com.intellij.ide.FrameStateListener;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.Alarm;
import com.intellij.util.VolatileDoubleCheckedLockedInit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.ClientVersion;
import org.jetbrains.idea.perforce.ServerVersion;
import org.jetbrains.idea.perforce.perforce.*;
import org.jetbrains.idea.perforce.perforce.connections.P4Connection;
import org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager;
import org.jetbrains.idea.perforce.perforce.login.Notifier;
import org.jetbrains.idea.perforce.perforce.login.NotifierImpl;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManager;
import org.jetbrains.idea.perforce.perforce.login.PerforceLoginManagerImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PerforceManager  {
  private final Project myProject;
  private final PerforceLoginManager myLoginManager;
  private final Notifier myLoginNotifier;

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.application.PerforceManager");
  private static final Logger LOG_RELATIVE_PATH = Logger.getInstance("#Log_relative_path");

  private Map<P4Connection, Map<String, List<String>>> myCachedP4Info = null;
  private Map<P4Connection, Map<String, List<String>>> myCachedP4Clients = null;
  private final Map<P4Connection, PerforceClient> myClientMap = new HashMap<P4Connection, PerforceClient>();

  private final MyClientRootsCache myClientRootsCache;
  
  private final VirtualFileAdapter myListener;
  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"}) private long myLastValidTime;

  private Alarm myUpdateAlarm;
  private final Runnable myUpdateRequest;
  private final FrameStateListener myFrameStateListener = new FrameStateListener() {
    public void onFrameDeactivated() {

    }

    public void onFrameActivated() {
      if (perforceIsUsed() && myProject.isInitialized()) {
        addUpdateRequest();
      }
    }
  };
  private boolean myAsynchronousUpdates;

  private final VolatileDoubleCheckedLockedInit<ClientVersion> myClientVersion;
  
  public void sendUpdateRequest(final Runnable runnable) {
    myUpdateAlarm.addRequest(new MyChainedUpdateRequest(runnable), 100);
  }

  public static PerforceManager getInstance(Project project) {
    return ServiceManager.getService(project, PerforceManager.class);
  }

  public static PerforceManager getInstanceChecked(final Project project) {
    return ApplicationManager.getApplication().runReadAction(new Computable<PerforceManager>() {
      public PerforceManager compute() {
        if (project.isDisposed()) throw new ProcessCanceledException();
        return ServiceManager.getService(project, PerforceManager.class);
      }
    });
  }

  public synchronized void clearCache() {
    myLastValidTime = -1;
    refreshData(false);
  }

  private void refreshData(boolean updateOpened) {
    if (perforceIsUsed()) {
      new MyUpdateRequest(updateOpened).run();
    }
  }

  public boolean perforceIsUsed() {
    final AbstractVcs[] activeVcses = ProjectLevelVcsManager.getInstance(myProject).getAllActiveVcss();
    final PerforceVcs perfVcs = PerforceVcs.getInstance(myProject);
    return Arrays.asList(activeVcses).contains(perfVcs);
  }

  public PerforceManager(Project project) {
    myProject = project;
    myLoginNotifier = new NotifierImpl(myProject, this);
    myLoginManager = new PerforceLoginManagerImpl(myProject, this, myLoginNotifier);
    myListener = new VirtualFileAdapter() {
      public void propertyChanged(VirtualFilePropertyEvent event) {
        if (!event.isFromRefresh()) return;
        if (event.getPropertyName().equals(VirtualFile.PROP_WRITABLE)) {
          final boolean wasWritable = ((Boolean)event.getOldValue()).booleanValue();
          if (wasWritable) {
            event.getFile().putUserData(P4File.KEY, null);
          }
        }
      }

      public void contentsChanged(VirtualFileEvent event) {
        if (!event.isFromRefresh()) return;
        if (!event.getFile().isWritable()) {
          event.getFile().putUserData(P4File.KEY, null);
        }
      }
    };

    myUpdateRequest = new MyUpdateRequest(true);
    myClientRootsCache = new MyClientRootsCache();
    myClientVersion = new VolatileDoubleCheckedLockedInit<ClientVersion>() {
      @Nullable
      @Override
      protected ClientVersion createT() {
        final List<P4Connection> allConnections =
          PerforceConnectionManager.getInstance(myProject).getAllConnections(PerforceSettings.getSettings(myProject));
        for (P4Connection connection : allConnections) {
          if (connection != null) {
            return PerforceRunner.getInstance(myProject).getClientVersion(connection);
          }
        }
        return null;
      }
    };
  }

  @Nullable
  public ClientVersion getClientVersion() {
    return myClientVersion.get();
  }

  private void updatePerforceModules() {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        PerforceVcs vcs = PerforceVcs.getInstance(myProject);
        VirtualFile[] contentRoots = ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(vcs);
        for(VirtualFile contentRoot: contentRoots) {
          VcsDirtyScopeManager.getInstance(myProject).dirDirtyRecursively(contentRoot);
        }
      }
    });

    ChangeListManager.getInstance(myProject).scheduleUpdate();
  }

  private void serverDataChanged() {
    myLastValidTime = System.currentTimeMillis();
  }

  public void startListening() {
    myUpdateAlarm = new Alarm(Alarm.ThreadToUse.OWN_THREAD,myProject);
    VirtualFileManager.getInstance().addVirtualFileListener(myListener);
    FrameStateManager.getInstance().addListener(myFrameStateListener);
    myLastValidTime = -1L;
    ((PerforceLoginManagerImpl) myLoginManager).startListening();
  }

  public void stopListening() {
    VirtualFileManager.getInstance().removeVirtualFileListener(myListener);
    ((PerforceLoginManagerImpl) myLoginManager).stopListening();
    myUpdateAlarm.dispose();
    myUpdateAlarm = null;
    FrameStateManager.getInstance().removeListener(myFrameStateListener);
  }

  protected Map<String, List<String>> getCachedInfo(P4Connection connection) {
    return getCachedData(connection, true);
  }

  protected Map<String, List<String>> getCachedClients(P4Connection connection) {
    return getCachedData(connection, false);
  }

  private Map<String, List<String>> getCachedData(final P4Connection connection, boolean requestInfo) {
    synchronized (this) {
      Map<P4Connection, Map<String, List<String>>> map = requestInfo ? myCachedP4Info : myCachedP4Clients;
      if (map != null && map.containsKey(connection)) {
        final Map<String, List<String>> result = map.get(connection);
        if (result != null) {
          return result;
        }
        else {
          return new HashMap<String, List<String>>();
        }
      }
    }

    final MyUpdateRequest request = new MyUpdateRequest(false);
    request.run();
    Map<P4Connection, Map<String, List<String>>> map = requestInfo ? request.newInfo : request.newClients;
    if (map != null && map.containsKey(connection)) {
      return map.get(connection);
    }
    return new HashMap<String, List<String>>();
  }

  private void addUpdateRequest() {
    myUpdateAlarm.cancelRequest(myUpdateRequest);
    myUpdateAlarm.addRequest(myUpdateRequest, 100);
  }

  private PerforceSettings getSettings() {
    return PerforceSettings.getSettings(myProject);
  }


  public String getClientRoot(final P4Connection connection) {
    Map<String, List<String>> clientSpec = getCachedClients(connection);
    final List<String> mainRootValues = clientSpec.get(PerforceRunner.CLIENTSPEC_ROOT);
    final List<String> altRootValues = clientSpec.get(PerforceRunner.CLIENTSPEC_ALTROOTS);

    if (mainRootValues != null) {
      for (String mainRootValue : mainRootValues) {
        if (new File(mainRootValue).isDirectory()) {
          return mainRootValue;
        }
      }
    }

    if (altRootValues != null) {
      for (String altRootValue : altRootValues) {
        if (new File(altRootValue).isDirectory()) {
          return altRootValue;
        }
      }
    }

    return null;
  }

  public long getServerVertionYear(final P4Connection connection) {
    final List<String> serverVersions = getCachedInfo(connection).get(PerforceRunner.SERVER_VERSION);
    if (serverVersions == null || serverVersions.isEmpty()) return -1;
    return OutputMessageParser.parseServerVersion(serverVersions.get(0)).getVersionYear();
  }


  @Nullable
  public ServerVersion getServerVersion(final P4Connection connection) {
    final List<String> serverVersions = getCachedInfo(connection).get(PerforceRunner.SERVER_VERSION);
    if (serverVersions == null || serverVersions.isEmpty()) return null;
    return OutputMessageParser.parseServerVersion(serverVersions.get(0));
  }

  public long getLastValidTime() {
    return myLastValidTime;
  }

  public boolean isUnderPerforceRoot(@NotNull final VirtualFile virtualFile) {
    final P4Connection connection = PerforceSettings.getSettings(myProject).getConnectionForFile(virtualFile);
    final Application application = ApplicationManager.getApplication();
    final boolean[] result = new boolean[1];
    final Runnable runnable = new Runnable() {
      public void run() {
        final String path = getClientRoot(connection);
        if (path != null) {
          final VirtualFile root = LocalFileSystem.getInstance().findFileByIoFile(new File(path));
          if (root != null && ((root == virtualFile) || !VfsUtil.isAncestor(virtualFile, root, false))) {
            result[0] = true;
            return;
          }
        }
        result[0] = false;
      }
    };

    application.runReadAction(runnable);

    return result[0];
  }

  @Nullable
  private static String getRelativePath(String filePath, PerforceClient client) {
    return View.getRelativePath(filePath, client.getName(), client.getViews());
  }

  @Nullable
  public static File getFileByDepotName(final String depotPath, PerforceClient client) throws VcsException {

    int revNumStart = depotPath.indexOf("#");

    final String clientRoot = client.getRoot();
    if (clientRoot == null) {
      throw new VcsException("Failed to retrieve client root");
    }
    final String relativePath;

    if (revNumStart >= 0) {
      relativePath = getRelativePath(depotPath.substring(0, revNumStart), client);

    }
    else {
      relativePath = getRelativePath(depotPath, client);
    }

    if (relativePath == null)  {
      final StringBuffer message = new StringBuffer();
      final List<View> views = client.getViews();
      for (View view : views) {
        message.append('\n');
        message.append("View ");
        message.append(view.toString());
      }
      message.append("Cannot find local file for depot path: ").append(depotPath);
      LOG.info(message.toString());

      return null;
    }

    final File result;
    if (clientRoot.length() > 0) {
      result = new File(clientRoot, relativePath.trim());
    }
    else {
      result = new File(relativePath.trim());
    }
    LOG_RELATIVE_PATH.debug("depot: '" + depotPath + "' result: '" + result + "'");
    return result;
  }

  @NotNull
  public synchronized PerforceClient getClient(final P4Connection connection) {
    PerforceClient client = myClientMap.get(connection);
    if (client == null) {
      client = new PerforceClientImpl(myProject, connection);
      myClientMap.put(connection, client);
    }
    return client;
  }

  public void configurationChanged() {
    clearCache();
    P4File.invalidateFstat(myProject);
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        VcsDirtyScopeManager.getInstance(myProject).markEverythingDirty();
      }
    });
  }

  /**
   * sometimes we will need to test asynchronous updates behaviour
   */
  public void doAsynchronousUpdates() {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      myAsynchronousUpdates = true;
    }
  }

  public void queueUpdateRequest(Runnable runnable) {
    myUpdateAlarm.addRequest(runnable, 0);
  }

  public void queueUpdateRequest(final Runnable runnable, final int delayMillis) {
    myUpdateAlarm.addRequest(runnable, delayMillis);
  }

  private class MyChainedUpdateRequest extends MyUpdateRequest {
    private final Runnable myRunnable;

    private MyChainedUpdateRequest(Runnable runnable) {
      super(true);
      myRunnable = runnable;
    }

    @Override
    protected void onAfterUpdated() {
      myRunnable.run();
    }
  }

  private class MyUpdateRequest implements Runnable {
    private final boolean myUpdateOpened;
    public HashMap<P4Connection, Map<String, List<String>>> newInfo;
    public HashMap<P4Connection, Map<String, List<String>>> newClients;

    public MyUpdateRequest(final boolean updateOpened) {
      myUpdateOpened = updateOpened;
    }

    @SuppressWarnings({"SynchronizeOnThis"})
    public void run() {
      final Alarm alarm = myUpdateAlarm;
      if (alarm == null) return;   // already disposed
      alarm.cancelRequest(this);
      try {
        final Map<P4Connection, Map<String, List<String>>> oldInfo;
        final Map<P4Connection, Map<String, List<String>>> oldClients;
        synchronized (PerforceManager.this) {
          oldInfo = myCachedP4Info;
          oldClients = myCachedP4Clients;
        }

        newInfo = new HashMap<P4Connection, Map<String, List<String>>>();
        newClients = new HashMap<P4Connection, Map<String, List<String>>>();
        if (PerforceSettings.getSettings(myProject).ENABLED) {
          final List<P4Connection> allConnections = getSettings().getAllConnections();
          for (P4Connection connection : allConnections) {
            try {
              final Map<String, List<String>> infoMap = PerforceRunner.getInstance(myProject).getInfo(connection);
              final List<String> client = infoMap.get(PerforceRunner.CLIENT_NAME);
              final Map<String, List<String>> clientMap;
              if (client != null && client.size() > 0) {
                clientMap = PerforceRunner.getInstance(myProject).loadClient(client.get(0), connection);
                convertRoots(clientMap);
              }
              else {
                clientMap = new HashMap<String, List<String>>();
              }
              // the following fields change on every invocation, and changes in these fields should not cause the
              // serverDataChanged notification to be fired
              infoMap.remove(PerforceRunner.CLIENT_ADDRESS);
              infoMap.remove(PerforceRunner.SERVER_DATE);
              newInfo.put(connection, infoMap);
              newClients.put(connection, clientMap);
            }
            catch (VcsException e) {
              newInfo.put(connection, new HashMap<String, List<String>>());
              newClients.put(connection, new HashMap<String, List<String>>());
            }
          }
        }

        synchronized (PerforceManager.this) {
          myCachedP4Info = newInfo;
          myCachedP4Clients = newClients;
        }
        if (!Comparing.equal(oldInfo, newInfo)) {
          serverDataChanged();
        }
        if (!Comparing.equal(oldClients, newClients)) {
          serverDataChanged();
          synchronized(PerforceManager.this) {
            myClientMap.clear();
            myClientRootsCache.clear();
          }
        }

        final ChangeListSynchronizer changeListSynchronizer = ChangeListSynchronizer.getInstance(myProject);
        if (myUpdateOpened && !myProject.isDisposed() && !changeListSynchronizer.updateOpenedFiles()) {
          changeListSynchronizer.requestDeleteEmptyChangeLists();
          updatePerforceModules();
        }
      }
      finally {
        alarm.cancelRequest(this);
        onAfterUpdated();
      }
    }

    private void convertRoots(final Map<String, List<String>> clientSpec) {
      convertRoots(clientSpec, PerforceRunner.CLIENTSPEC_ROOT);
      convertRoots(clientSpec, PerforceRunner.CLIENTSPEC_ALTROOTS);
    }

    private void convertRoots(final Map<String, List<String>> clientSpec, final String key) {
      final List<String> in = clientSpec.get(key);
      if (in == null) return;
      final List<String> out = new ArrayList<String>(in.size());

      for (String s : in) {
        out.add(myClientRootsCache.putGet(s));
      }

      clientSpec.put(key, out);
    }

    protected void onAfterUpdated() {
    }
  }

  public PerforceLoginManager getLoginManager() {
    return myLoginManager;
  }

  public Notifier getLoginNotifier() {
    return myLoginNotifier;
  }

  public String convertP4ParsedPath(final String convertedClientRoot, final String s) {
    final String result = myClientRootsCache.convertPath(convertedClientRoot, s);
    LOG_RELATIVE_PATH.debug("convertion, s: '" + s + "' converted: '" + result + "' convertedRoot: '" + convertedClientRoot + "'");
    return result;
  }

  // to fix a case when user sets "c:\depot" in his client specification instead of "C:\depot" and
  // we don't want to convert to canonical path/file - for each path reported by P4
  private static class MyClientRootsCache {
    private final Map<String, String> myWasIs;
    private final Map<String, String> myIsWas;

    private final Object myLock = new Object();

    private MyClientRootsCache() {
      myWasIs = new HashMap<String, String>();
      myIsWas = new HashMap<String, String>();
    }

    public void clear() {
      // hope there will not be many P4 client roots
      //synchronized (myLock) {
        //myWasIs.clear();
        //myIsWas.clear();
      //}
    }

    private void putPair(final String is, final String was) {
      myIsWas.put(is, was);
      myWasIs.put(was, is);
    }

    String putGet(final String rawClientRoot) {
      synchronized (myLock) {
        if (myWasIs.containsKey(rawClientRoot)) return myWasIs.get(rawClientRoot);
        String converted = rawClientRoot;
        final File root = new File(rawClientRoot);
        try {
          converted = root.getCanonicalPath();
        }
        catch (IOException e) {
          //
        }
        putPair(converted, rawClientRoot);
        return converted;
      }
    }

    // easier then canonical path
    String convertPath(@Nullable final String convertedClientRoot, @NotNull final String s) {
      final String trimmed = s.trim();
      synchronized (myLock) {
        if (convertedClientRoot != null) {
          final String rawRoot = myIsWas.get(convertedClientRoot);
          if ((rawRoot != null) && trimmed.startsWith(rawRoot)) {
            return glueRelativePath(convertedClientRoot, trimmed.substring(rawRoot.length(), trimmed.length()));
          }
          // no info anyway
          return trimmed;
        }
        // check all
        for (String was : myWasIs.keySet()) {
          if (trimmed.startsWith(was)) {
            final String is = myWasIs.get(was);
            return glueRelativePath(is, trimmed.substring(was.length(), trimmed.length()));
          }
        }
        return trimmed;
      }
    }
  }

  private static String glueRelativePath(final String absPath, final String relativePath) {
    if (absPath.endsWith("\\") || absPath.endsWith("/") || relativePath.startsWith("\\") || relativePath.startsWith("/"))
      return absPath + relativePath;
    return absPath + File.separator + relativePath;
  }
}
