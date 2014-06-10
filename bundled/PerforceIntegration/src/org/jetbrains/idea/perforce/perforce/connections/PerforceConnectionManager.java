package org.jetbrains.idea.perforce.perforce.connections;

import com.intellij.ProjectTopics;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.EnvironmentUtil;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.perforce.application.PerforceManager;
import org.jetbrains.idea.perforce.application.PerforceVcs;
import org.jetbrains.idea.perforce.perforce.ConnectionId;
import org.jetbrains.idea.perforce.perforce.P4File;
import org.jetbrains.idea.perforce.perforce.PerforceSettings;

import java.io.File;
import java.util.*;

public class PerforceConnectionManager {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.perforce.perforce.connections.PerforceConnectionManager");

  private PerforceSettings mySettings;
  private final LocalFileSystem myLocalFileSystem;
  private final Project myProject;

  private Map<String, P4Connection> myRootToConnectionMap;
  private boolean myInitializingConnections = false;

  private final Map<ConnectionId, P4Connection> myCachedConnections = new HashMap<ConnectionId, P4Connection>();
  @NonNls private static final String P4_CONFIG = "P4CONFIG";
  private final VirtualFileAdapter myVirtualFileListener;
  private MessageBusConnection myMessageBusConnection;

  public PerforceConnectionManager(final Project project, LocalFileSystem localFileSystem) {
    myProject = project;
    myLocalFileSystem = localFileSystem;

    myVirtualFileListener = new VirtualFileAdapter() {
      public void propertyChanged(VirtualFilePropertyEvent event) {
        processFileEvent(event);
      }

      public void contentsChanged(VirtualFileEvent event) {
        processFileEvent(event);
      }

      public void fileCreated(VirtualFileEvent event) {
        processFileEvent(event);
      }

      public void fileDeleted(VirtualFileEvent event) {
        processFileEvent(event);
      }

      public void fileMoved(VirtualFileMoveEvent event) {
        processFileEvent(event);
      }

      public void beforePropertyChange(VirtualFilePropertyEvent event) {
        processFileEvent(event);
      }

      public void beforeContentsChange(VirtualFileEvent event) {
        processFileEvent(event);
      }

      public void beforeFileDeletion(VirtualFileEvent event) {
        processFileEvent(event);
      }

      public void beforeFileMovement(VirtualFileMoveEvent event) {
        processFileEvent(event);
      }
    };
  }

  public static PerforceConnectionManager getInstance(Project project) {
    return ServiceManager.getService(project, PerforceConnectionManager.class);
  }

  public static PerforceConnectionManager getInstanceChecked(final Project project) {
    return ApplicationManager.getApplication().runReadAction(new Computable<PerforceConnectionManager>() {
      public PerforceConnectionManager compute() {
        if (project.isDisposed()) throw new ProcessCanceledException();
        return ServiceManager.getService(project, PerforceConnectionManager.class);
      }
    }); 
  }

  public void refreshConnections(final PerforceSettings settings) {
    if (! doNotUseP4ConfigFile(settings)) {
      myRootToConnectionMap = null;
    }
  }

  @Nullable
  public P4Connection findConnectionById(final ConnectionId id) {
    if (P4Connection.INVALID_CONNECTION_ID.equals(id)) return P4Connection.INVALID;
    
    if (id.myDoNotUseP4Config) {
      return SingletonConnection.getInstance(getProject());
    }
    else if (id.myP4ConfigFileName != null && id.myWorkingDir != null){
      if (!myCachedConnections.containsKey(id)) {
        final P4ConfigFileBasedConnection p4ConfigFileBasedConnection =
          new P4ConfigFileBasedConnection(new File(id.myWorkingDir), id.myP4ConfigFileName);
        if (p4ConfigFileBasedConnection.isValid()) {
          myCachedConnections.put(id, p4ConfigFileBasedConnection);
          return p4ConfigFileBasedConnection;
        }
        else {
          return null;
        }
      }
      else {
        return myCachedConnections.get(id);
      }

    } else {
      return null;
    }
  }

  public List<P4Connection> getAllConnections(final PerforceSettings perforceSettings) {
    if (doNotUseP4ConfigFile(perforceSettings)) {
      LOG.debug("getAllConnections(): not using p4config, returning singleton connections");
      return Collections.singletonList((P4Connection)getSingleton());
    }
    else {
      Map<String, P4Connection> rootToConnectionMap = ensureRootConnections();
      final Set<P4Connection> result = new HashSet<P4Connection>();
      for (P4Connection connection : rootToConnectionMap.values()) {
        if (connection != P4Connection.INVALID) {
          result.add(connection);
        }
      }
      return new ArrayList<P4Connection>(result);
    }
  }

  @NotNull
  private Map<String, P4Connection> ensureRootConnections() {
    // to avoid race conditions when invoked from another thread (IDEADEV-5603), return actual map instance
    Map<String, P4Connection> result = myRootToConnectionMap;
    if (result == null) {
      LOG.debug("ensureRootConnections() initializing map");
      final Application application = ApplicationManager.getApplication();
      result = application.runReadAction(new Computable<Map<String, P4Connection>>() {
        public Map<String, P4Connection> compute() {
          initRootConnections();
          return myRootToConnectionMap;
        }
      });
    }
    else {
      LOG.debug("ensureRootConnections() returning cached map");
    }
    return result;
  }

  private void initRootConnections() {
    final PerforceVcs vcs = PerforceVcs.getInstance(getProject());
    myRootToConnectionMap = new HashMap<String, P4Connection>();
    final String fileName = getP4ConfigFileName();
    if (LOG.isDebugEnabled()) {
      LOG.debug("initRootConnections(): p4config filename=" + fileName);
    }
    LOG.assertTrue(fileName != null);
    VirtualFile[] contentRoots;
    myInitializingConnections = true;
    try {
      contentRoots = ProjectLevelVcsManager.getInstance(myProject).getRootsUnderVcs(vcs);
    }
    finally {
      myInitializingConnections = false;
    }
    for(VirtualFile contentRoot: contentRoots) {
      P4Connection rootConnection = createRootConnection(contentRoot, fileName);
      final ConnectionId id = rootConnection.getId();
      if (myCachedConnections.containsKey(id)) {
        rootConnection = myCachedConnections.get(id);
      }
      myCachedConnections.put(id, rootConnection);
      myRootToConnectionMap.put(keyForVirtualFile(contentRoot), rootConnection);
    }
  }

  public boolean isInitializingConnections() {
    return myInitializingConnections;
  }

  private Project getProject() {
    return myProject;
  }

  private P4Connection createRootConnection(final VirtualFile file, final String p4ConfigFileName) {
    if (file == null) {
      return P4Connection.INVALID;
    }
    else {
      VirtualFile workingDir = findWorkingDir(file, p4ConfigFileName);
      if (workingDir == null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("createRootConnection() couldn't find p4config for module content entry " + file.getPresentableName());
        }
        // todo : rather the way for connection set in settings should be checked
        return getSingleton();
      }
      else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("createRootConnection() found p4config for module content entry " + workingDir.getPresentableName() + " at " +
            workingDir.getPresentableName() + ", name " + p4ConfigFileName);
        }
        return new P4ConfigFileBasedConnection(new File(workingDir.getPath()), p4ConfigFileName);
      }

    }
  }

  public static String getP4ConfigFileName() {
    final String valueFromEnv = EnvironmentUtil.getEnviromentProperties().get(P4_CONFIG);
    if (valueFromEnv == null) {
      return System.getProperty(P4_CONFIG);
    }
    else {
      return valueFromEnv;
    }

  }

  private static VirtualFile findWorkingDir(final VirtualFile parent, final String p4ConfigFileName) {
    final VirtualFile[] children = parent.getChildren();
    for (VirtualFile virtualFile : children) {
      if (!virtualFile.isDirectory()) {
        if (FileUtil.pathsEqual(p4ConfigFileName, virtualFile.getName())) {
          return parent;
        }
      }
    }

    if (parent.getParent() == null) {
      return null;
    }
    else {
      return findWorkingDir(parent.getParent(), p4ConfigFileName);
    }
  }

  private SingletonConnection getSingleton() {
    return SingletonConnection.getInstance(getProject());
  }

  @Nullable
  private VirtualFile findNearestLiveParentFor(File ioFile) {
    do {
      VirtualFile parent = myLocalFileSystem.findFileByIoFile(ioFile);
      if (parent != null) return parent;
      ioFile = ioFile.getParentFile();
      if (ioFile == null) return null;
    }
    while (true);
  }

  @Nullable
  public P4Connection getConnectionForFile(File file) {
    if (doNotUseP4ConfigFile(getSettings())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("getConnectionForFile(" + file + ") returning singleton");
      }
      return getSingleton();
    }
    final VirtualFile vFile = findNearestLiveParentFor(file);
    if (vFile == null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("getConnectionForFile(" + file + ") found no live parent");
      }
      return null;
    }
    return getConnectionForFile(vFile);
  }

  @Nullable
  public P4Connection getConnectionForFile(P4File file) {
    return getConnectionForFile(new File(file.getLocalPath()));
  }

  public P4Connection getConnectionForFile(final VirtualFile file) {
    if (doNotUseP4ConfigFile(getSettings())) {
      return getSingleton();
    }
    else {
      final Map<String, P4Connection> connectionMap = ensureRootConnections();
      final VirtualFile contentRoot = getContentRootFor(file);
      final P4Connection result = contentRoot == null ? null : connectionMap.get(keyForVirtualFile(contentRoot));
      if (result == null) {
        return P4Connection.INVALID;
      }
      else {
        return result;
      }
    }
  }

  public boolean isSingletonConnectionUsed() {
    return doNotUseP4ConfigFile(getSettings());
  }

  private static boolean doNotUseP4ConfigFile(final PerforceSettings settings) {
    return !settings.useP4CONFIG || getP4ConfigFileName() == null;
  }

  private VirtualFile getContentRootFor(final VirtualFile file) {
    return ApplicationManager.getApplication().runReadAction(new Computable<VirtualFile>(){
      @Nullable
      public VirtualFile compute() {
        return ProjectLevelVcsManager.getInstance(myProject).getVcsRootFor(file);
      }
    });
  }

  public void startListening() {
    myMessageBusConnection = getProject().getMessageBus().connect();
    myMessageBusConnection.subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
      public void beforeRootsChange(ModuleRootEvent event) {
      }

      public void rootsChanged(ModuleRootEvent event) {
        myRootToConnectionMap = null;
      }
    });

    VirtualFileManager.getInstance().addVirtualFileListener(myVirtualFileListener);
  }

  public void stopListening() {
    if (myMessageBusConnection != null) {
      myMessageBusConnection.disconnect();
      myMessageBusConnection = null;
    }
    VirtualFileManager.getInstance().removeVirtualFileListener(myVirtualFileListener);
  }

  private void processFileEvent(final VirtualFileEvent event) {
    if (Comparing.equal(getP4ConfigFileName(), event.getFileName())) {
      LOG.debug("received virtual file event on p4config file");
      myRootToConnectionMap = null;
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          PerforceManager.getInstance(getProject()).configurationChanged();
        }
      });
    }
  }

  private String keyForVirtualFile(final VirtualFile vf) {
    return vf.getUrl();
  }

  public void updateConnections() {
    myRootToConnectionMap = null;
  }

  public PerforceSettings getSettings() {
    if (mySettings == null) {
      mySettings = PerforceSettings.getSettings(myProject);
    }
    return mySettings;
  }
}
