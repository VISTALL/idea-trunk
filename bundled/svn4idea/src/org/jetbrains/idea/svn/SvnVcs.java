/**
 * @copyright
 * ====================================================================
 * Copyright (c) 2003-2004 QintSoft.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://subversion.tigris.org/license-1.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 *
 * This software consists of voluntary contributions made by many
 * individuals.  For exact contribution history, see the revision
 * history and logs, available at http://svnup.tigris.org/.
 * ====================================================================
 * @endcopyright
 */
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
package org.jetbrains.idea.svn;

import com.intellij.ide.FrameStateListener;
import com.intellij.ide.FrameStateManager;
import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.annotate.AnnotationProvider;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vcs.checkin.CheckinEnvironment;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsHistoryProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.merge.MergeProvider;
import com.intellij.openapi.vcs.rollback.RollbackEnvironment;
import com.intellij.openapi.vcs.update.UpdateEnvironment;
import com.intellij.openapi.vcs.versionBrowser.ChangeBrowserSettings;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.Convertor;
import com.intellij.util.containers.SoftHashMap;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.svn.actions.ShowPropertiesDiffWithLocalAction;
import org.jetbrains.idea.svn.actions.SvnMergeProvider;
import org.jetbrains.idea.svn.annotate.SvnAnnotationProvider;
import org.jetbrains.idea.svn.checkin.SvnCheckinEnvironment;
import org.jetbrains.idea.svn.dialogs.SvnFormatWorker;
import org.jetbrains.idea.svn.dialogs.WCInfo;
import org.jetbrains.idea.svn.history.LoadedRevisionsCache;
import org.jetbrains.idea.svn.history.SvnChangeList;
import org.jetbrains.idea.svn.history.SvnCommittedChangesProvider;
import org.jetbrains.idea.svn.history.SvnHistoryProvider;
import org.jetbrains.idea.svn.rollback.SvnRollbackEnvironment;
import org.jetbrains.idea.svn.update.SvnIntegrateEnvironment;
import org.jetbrains.idea.svn.update.SvnUpdateEnvironment;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.util.jna.SVNJNAUtil;
import org.tmatesoft.svn.core.internal.wc.SVNAdminUtil;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea14;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.internal.wc.admin.SVNWCAccess;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNDebugLogAdapter;
import org.tmatesoft.svn.util.SVNLogType;

import javax.swing.event.HyperlinkEvent;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class SvnVcs extends AbstractVcs {
  private final static Logger REFRESH_LOG = Logger.getInstance("#svn_refresh");

  private static final Logger LOG = Logger.getInstance("org.jetbrains.idea.svn.SvnVcs");
  @NonNls public static final String VCS_NAME = "svn";
  private static final VcsKey ourKey = createKey(VCS_NAME);
  private final Map<String, Map<String, Pair<SVNPropertyValue, Trinity<Long, Long, Long>>>> myPropertyCache = new SoftHashMap<String, Map<String, Pair<SVNPropertyValue, Trinity<Long, Long, Long>>>>();

  private final SvnConfiguration myConfiguration;
  private final SvnEntriesFileListener myEntriesFileListener;

  private CheckinEnvironment myCheckinEnvironment;
  private RollbackEnvironment myRollbackEnvironment;
  private UpdateEnvironment mySvnUpdateEnvironment;
  private UpdateEnvironment mySvnIntegrateEnvironment;
  private VcsHistoryProvider mySvnHistoryProvider;
  private AnnotationProvider myAnnotationProvider;
  private DiffProvider mySvnDiffProvider;
  private final VcsShowConfirmationOption myAddConfirmation;
  private final VcsShowConfirmationOption myDeleteConfirmation;
  private EditFileProvider myEditFilesProvider;
  private SvnCommittedChangesProvider myCommittedChangesProvider;
  private final VcsShowSettingOption myCheckoutOptions;

  private ChangeProvider myChangeProvider;
  private MergeProvider myMergeProvider;

  @NonNls public static final String LOG_PARAMETER_NAME = "javasvn.log";
  public static final String pathToEntries = SvnUtil.SVN_ADMIN_DIR_NAME + File.separatorChar + SvnUtil.ENTRIES_FILE_NAME;
  public static final String pathToDirProps = SvnUtil.SVN_ADMIN_DIR_NAME + File.separatorChar + SvnUtil.DIR_PROPS_FILE_NAME;
  private final SvnChangelistListener myChangeListListener;

  private SvnCopiesRefreshManager myCopiesRefreshManager;
  private SvnFileUrlMappingImpl myMapping;
  private final MyFrameStateListener myFrameStateListener;

  public static final Topic<Runnable> ROOTS_RELOADED = new Topic<Runnable>("ROOTS_RELOADED", Runnable.class);
  private VcsListener myVcsListener;

  static {
    SvnHttpAuthMethodsDefaultChecker.check();

    //noinspection UseOfArchaicSystemPropertyAccessors
    final JavaSVNDebugLogger logger = new JavaSVNDebugLogger(Boolean.getBoolean(LOG_PARAMETER_NAME), LOG);
    SVNDebugLog.setDefaultLog(logger);
    SVNAdminAreaFactory.setSelector(new SvnFormatSelector());

    DAVRepositoryFactory.setup();
    SVNRepositoryFactoryImpl.setup();
    FSRepositoryFactory.setup();

    // non-optimized writing is fast enough on Linux/MacOS, and somewhat more reliable
    if (SystemInfo.isWindows) {
      SVNAdminArea14.setOptimizedWritingEnabled(true);
    }

    if (! SVNJNAUtil.isJNAPresent()) {
      LOG.warn("JNA is not found by svnkit library");
    }
  }

  private static Boolean booleanProperty(final String systemParameterName) {
    return Boolean.valueOf(System.getProperty(systemParameterName));
  }

  public SvnVcs(final Project project, MessageBus bus, SvnConfiguration svnConfiguration, final ChangeListManager changeListManager,
                final VcsDirtyScopeManager vcsDirtyScopeManager, final StartupManager startupManager) {
    super(project, VCS_NAME);
    LOG.debug("ct");
    myConfiguration = svnConfiguration;

    dumpFileStatus(FileStatus.ADDED);
    dumpFileStatus(FileStatus.DELETED);
    dumpFileStatus(FileStatus.MERGE);
    dumpFileStatus(FileStatus.MODIFIED);
    dumpFileStatus(FileStatus.NOT_CHANGED);
    dumpFileStatus(FileStatus.UNKNOWN);

    dumpFileStatus(SvnFileStatus.REPLACED);
    dumpFileStatus(SvnFileStatus.EXTERNAL);
    dumpFileStatus(SvnFileStatus.OBSTRUCTED);

    myEntriesFileListener = new SvnEntriesFileListener(project);

    final ProjectLevelVcsManager vcsManager = ProjectLevelVcsManager.getInstance(project);
    myAddConfirmation = vcsManager.getStandardConfirmation(VcsConfiguration.StandardConfirmation.ADD, this);
    myDeleteConfirmation = vcsManager.getStandardConfirmation(VcsConfiguration.StandardConfirmation.REMOVE, this);
    myCheckoutOptions = vcsManager.getStandardOption(VcsConfiguration.StandardOption.CHECKOUT, this);

    if (myProject.isDefault()) {
      myChangeListListener = null;
    }
    else {
      upgradeIfNeeded(bus);

      myChangeListListener = new SvnChangelistListener(myProject, createChangelistClient());

      myVcsListener = new VcsListener() {
        public void directoryMappingChanged() {
          invokeRefreshSvnRoots(true);
        }
      };
    }

    // do one time after project loaded
    startupManager.runWhenProjectIsInitialized(new DumbAwareRunnable() {
      public void run() {
        postStartup();

        // for IDEA, it takes 2 minutes - and anyway this can be done in background, no sence...
        // once it could be mistaken about copies for 2 minutes on start...

        /*if (! myMapping.getAllWcInfos().isEmpty()) {
          invokeRefreshSvnRoots();
          return;
        }
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
          public void run() {
            myCopiesRefreshManager.getCopiesRefresh().ensureInit();
          }
        }, SvnBundle.message("refreshing.working.copies.roots.progress.text"), true, myProject);*/
      }
    });

    myFrameStateListener = new MyFrameStateListener(changeListManager, vcsDirtyScopeManager);
  }

  public void postStartup() {
    if (myProject.isDefault()) return;
    myCopiesRefreshManager = new SvnCopiesRefreshManager(myProject, (SvnFileUrlMappingImpl) getSvnFileUrlMapping());

    invokeRefreshSvnRoots(true);
  }

  public void invokeRefreshSvnRoots(final boolean asynchronous) {
    REFRESH_LOG.debug("refresh: ", new Throwable());
    if (myCopiesRefreshManager != null) {
      if (asynchronous) {
        myCopiesRefreshManager.getCopiesRefresh().asynchRequest();
      } else {
        ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
          public void run() {
            myCopiesRefreshManager.getCopiesRefresh().synchRequest();
          }
        }, SvnBundle.message("refreshing.working.copies.roots.progress.text"), true, myProject);
      }
    }
  }

  @Override
  public boolean checkImmediateParentsBeforeCommit() {
    return true;
  }

  private void upgradeIfNeeded(final MessageBus bus) {
      final MessageBusConnection connection = bus.connect();
      connection.subscribe(ChangeListManagerImpl.LISTS_LOADED, new LocalChangeListsLoadedListener() {
        public void processLoadedLists(final List<LocalChangeList> lists) {
          SvnConfiguration.SvnSupportOptions supportOptions = null;
          try {
            ChangeListManager.getInstanceChecked(myProject).setReadOnly(SvnChangeProvider.ourDefaultListName, true);

            supportOptions = myConfiguration.getSupportOptions();

            upgradeToRecentVersion(supportOptions);
            if (! supportOptions.changeListsSynchronized()) {
              processChangeLists(lists);
            }
          }
          catch (ProcessCanceledException e) {
            //
          } finally {
            if (supportOptions != null) {
              supportOptions.upgrade();
            }
          }

          connection.disconnect();
        }
      });
  }

  public void processChangeLists(final List<LocalChangeList> lists) {
    final ProjectLevelVcsManager plVcsManager = ProjectLevelVcsManager.getInstanceChecked(myProject);
    plVcsManager.startBackgroundVcsOperation();
    try {
      final SVNChangelistClient client = createChangelistClient();
      for (LocalChangeList list : lists) {
        if (! list.isDefault()) {
          final Collection<Change> changes = list.getChanges();
          for (Change change : changes) {
            correctListForRevision(plVcsManager, change.getBeforeRevision(), client, list.getName());
            correctListForRevision(plVcsManager, change.getAfterRevision(), client, list.getName());
          }
        }
      }
    }
    finally {
      final Application appManager = ApplicationManager.getApplication();
      if (appManager.isDispatchThread()) {
        appManager.executeOnPooledThread(new Runnable() {
          public void run() {
            plVcsManager.stopBackgroundVcsOperation();
          }
        });
      } else {
        plVcsManager.stopBackgroundVcsOperation();
      }
    }
  }

  private void correctListForRevision(final ProjectLevelVcsManager plVcsManager, final ContentRevision revision,
                                      final SVNChangelistClient client, final String name) {
    if (revision != null) {
      final FilePath path = revision.getFile();
      final AbstractVcs vcs = plVcsManager.getVcsFor(path);
      if ((vcs != null) && VCS_NAME.equals(vcs.getName())) {
        try {
          client.doAddToChangelist(new File[] {path.getIOFile()}, SVNDepth.EMPTY, name, null);
        }
        catch (SVNException e) {
          // left in default list
        }
      }
    }
  }

  private final static String UPGRADE_SUBVERSION_FORMAT = "Subversion";

  private void upgradeToRecentVersion(final SvnConfiguration.SvnSupportOptions supportOptions) {
    if (! supportOptions.upgradeTo16Asked()) {
      final SvnWorkingCopyChecker workingCopyChecker = new SvnWorkingCopyChecker();

      if (workingCopyChecker.upgradeNeeded()) {

        Notifications.Bus.notify(new Notification(UPGRADE_SUBVERSION_FORMAT, SvnBundle.message("upgrade.format.to16.question.title"),
                                                  "Old format Subversion working copies <a href=\"\">could be upgraded to version 1.6</a>.",
                                                  NotificationType.INFORMATION, new NotificationListener() {
            public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent event) {
              final int upgradeAnswer = Messages.showYesNoDialog(SvnBundle.message("upgrade.format.to16.question.text",
                  SvnBundle.message("label.where.svn.format.can.be.changed.text", SvnBundle.message("action.show.svn.map.text"))),
                  SvnBundle.message("upgrade.format.to16.question.title"), Messages.getWarningIcon());
              if (DialogWrapper.OK_EXIT_CODE == upgradeAnswer) {
                workingCopyChecker.doUpgrade();
              }

              notification.expire();
            }
          }));
      }
    }
  }

  @Override
  public void activate() {
    if (! myProject.isDefault()) {
      ChangeListManager.getInstance(myProject).addChangeListListener(myChangeListListener);
      ProjectLevelVcsManager.getInstance(myProject).addVcsListener(myVcsListener);
    }
    
    SvnApplicationSettings.getInstance().svnActivated();
    VirtualFileManager.getInstance().addVirtualFileListener(myEntriesFileListener);
    // this will initialize its inner listener for committed changes upload
    LoadedRevisionsCache.getInstance(myProject);
    FrameStateManager.getInstance().addListener(myFrameStateListener);
  }

  @Override
  public void deactivate() {
    FrameStateManager.getInstance().removeListener(myFrameStateListener);

    if (myVcsListener != null) {
      ProjectLevelVcsManager.getInstance(myProject).removeVcsListener(myVcsListener);
    }
    
    VirtualFileManager.getInstance().removeVirtualFileListener(myEntriesFileListener);
    SvnApplicationSettings.getInstance().svnDeactivated();
    new DefaultSVNRepositoryPool(null, null).shutdownConnections(true);
    if (myCommittedChangesProvider != null) {
      myCommittedChangesProvider.deactivate();
    }
    if (myChangeListListener != null && (! myProject.isDefault())) {
      ChangeListManager.getInstance(myProject).removeChangeListListener(myChangeListListener);
    }
  }

  public VcsShowConfirmationOption getAddConfirmation() {
    return myAddConfirmation;
  }

  public VcsShowConfirmationOption getDeleteConfirmation() {
    return myDeleteConfirmation;
  }

  public VcsShowSettingOption getCheckoutOptions() {
    return myCheckoutOptions;
  }

  public EditFileProvider getEditFileProvider() {
    if (myEditFilesProvider == null) {
      myEditFilesProvider = new SvnEditFileProvider(this);
    }
    return myEditFilesProvider;
  }

  @NotNull
  public ChangeProvider getChangeProvider() {
    if (myChangeProvider == null) {
      myChangeProvider = new SvnChangeProvider(this);
    }
    return myChangeProvider;
  }

  public SVNRepository createRepository(String url) throws SVNException {
    SVNRepository repos = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
    repos.setAuthenticationManager(myConfiguration.getAuthenticationManager(myProject));
    repos.setTunnelProvider(myConfiguration.getOptions(myProject));
    return repos;
  }

  public SVNRepository createRepository(SVNURL url) throws SVNException {
    SVNRepository repos = SVNRepositoryFactory.create(url);
    repos.setAuthenticationManager(myConfiguration.getAuthenticationManager(myProject));
    repos.setTunnelProvider(myConfiguration.getOptions(myProject));
    return repos;
  }

  public SVNUpdateClient createUpdateClient() {
    return new SVNUpdateClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNStatusClient createStatusClient() {
    return new SVNStatusClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNWCClient createWCClient() {
    return new SVNWCClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNCopyClient createCopyClient() {
    return new SVNCopyClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNMoveClient createMoveClient() {
    return new SVNMoveClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNLogClient createLogClient() {
    return new SVNLogClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNCommitClient createCommitClient() {
    return new SVNCommitClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNDiffClient createDiffClient() {
    return new SVNDiffClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNChangelistClient createChangelistClient() {
    return new SVNChangelistClient(myConfiguration.getAuthenticationManager(myProject), myConfiguration.getOptions(myProject));
  }

  public SVNWCAccess createWCAccess() {
    final SVNWCAccess access = SVNWCAccess.newInstance(null);
    access.setOptions(myConfiguration.getOptions(myProject));
    return access;
  }

  public ISVNOptions getSvnOptions() {
    return myConfiguration.getOptions(myProject);
  }

  public ISVNAuthenticationManager getSvnAuthenticationManager() {
    return myConfiguration.getAuthenticationManager(myProject);
  }

  void dumpFileStatus(FileStatus fs) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("FileStatus:" + fs.getText() + " " + fs.getColor() + " " + " " + fs.getClass().getName());
    }
  }

  public UpdateEnvironment getIntegrateEnvironment() {
    if (mySvnIntegrateEnvironment == null) {
      mySvnIntegrateEnvironment = new SvnIntegrateEnvironment(this);
    }
    return mySvnIntegrateEnvironment;
  }

  public UpdateEnvironment getUpdateEnvironment() {
    if (mySvnUpdateEnvironment == null) {
      mySvnUpdateEnvironment = new SvnUpdateEnvironment(this);
    }
    return mySvnUpdateEnvironment;
  }

  public String getDisplayName() {
    LOG.debug("getDisplayName");
    return "Subversion";
  }

  public Configurable getConfigurable() {
    LOG.debug("createConfigurable");
    return new SvnConfigurable(myProject);
  }


  public SvnConfiguration getSvnConfiguration() {
    return myConfiguration;
  }

  public static SvnVcs getInstance(Project project) {
    return (SvnVcs) ProjectLevelVcsManager.getInstance(project).findVcsByName(VCS_NAME);
  }

  @NotNull
  public CheckinEnvironment getCheckinEnvironment() {
    if (myCheckinEnvironment == null) {
      myCheckinEnvironment = new SvnCheckinEnvironment(this);
    }
    return myCheckinEnvironment;
  }

  @NotNull
  public RollbackEnvironment getRollbackEnvironment() {
    if (myRollbackEnvironment == null) {
      myRollbackEnvironment = new SvnRollbackEnvironment(this);
    }
    return myRollbackEnvironment;
  }

  public VcsHistoryProvider getVcsHistoryProvider() {
    // no heavy state, but it would be useful to have place to keep state in -> do not reuse instance
    return new SvnHistoryProvider(this);
  }

  public VcsHistoryProvider getVcsBlockHistoryProvider() {
    return getVcsHistoryProvider();
  }

  public AnnotationProvider getAnnotationProvider() {
    if (myAnnotationProvider == null) {
      myAnnotationProvider = new SvnAnnotationProvider(this);
    }
    return myAnnotationProvider;
  }

  public SvnEntriesFileListener getSvnEntriesFileListener() {
    return myEntriesFileListener;
  }

  public DiffProvider getDiffProvider() {
    if (mySvnDiffProvider == null) {
      mySvnDiffProvider = new SvnDiffProvider(this);
    }
    return mySvnDiffProvider;
  }

  private Trinity<Long, Long, Long> getTimestampForPropertiesChange(final File ioFile, final boolean isDir) {
    final File dir = isDir ? ioFile : ioFile.getParentFile();
    final String relPath = SVNAdminUtil.getPropPath(ioFile.getName(), isDir ? SVNNodeKind.DIR : SVNNodeKind.FILE, false);
    final String relPathBase = SVNAdminUtil.getPropBasePath(ioFile.getName(), isDir ? SVNNodeKind.DIR : SVNNodeKind.FILE, false);
    final String relPathRevert = SVNAdminUtil.getPropRevertPath(ioFile.getName(), isDir ? SVNNodeKind.DIR : SVNNodeKind.FILE, false);
    return new Trinity<Long, Long, Long>(new File(dir, relPath).lastModified(), new File(dir, relPathBase).lastModified(),
                                         new File(dir, relPathRevert).lastModified());
  }

  private boolean trinitiesEqual(final Trinity<Long, Long, Long> t1, final Trinity<Long, Long, Long> t2) {
    if (t2.first == 0 && t2.second == 0 && t2.third == 0) return false;
    return t1.equals(t2);
  }

  @Nullable
  public SVNPropertyValue getPropertyWithCaching(final VirtualFile file, final String propName) throws SVNException {
    Map<String, Pair<SVNPropertyValue, Trinity<Long, Long, Long>>> cachedMap = myPropertyCache.get(keyForVf(file));
    final Pair<SVNPropertyValue, Trinity<Long, Long, Long>> cachedValue = (cachedMap == null) ? null : cachedMap.get(propName);

    final File ioFile = new File(file.getPath());
    final Trinity<Long, Long, Long> tsTrinity = getTimestampForPropertiesChange(ioFile, file.isDirectory());

    if (cachedValue != null) {
      // zero means that a file was not found
      if (trinitiesEqual(cachedValue.getSecond(), tsTrinity)) {
        return cachedValue.getFirst();
      }
    }

    final SVNPropertyData value = createWCClient().doGetProperty(ioFile, propName, SVNRevision.WORKING, SVNRevision.WORKING);
    final SVNPropertyValue propValue = (value == null) ? null : value.getValue();

    if (cachedMap == null) {
      cachedMap = new HashMap<String, Pair<SVNPropertyValue, Trinity<Long, Long, Long>>>();
      myPropertyCache.put(keyForVf(file), cachedMap);
    }

    cachedMap.put(propName, new Pair<SVNPropertyValue, Trinity<Long, Long, Long>>(propValue, tsTrinity));

    return propValue;
  }

  public boolean fileExistsInVcs(FilePath path) {
    File file = path.getIOFile();
    SVNStatus status;
    try {
      status = createStatusClient().doStatus(file, false);
      if (status != null) {
        final SVNStatusType statusType = status.getContentsStatus();
        if (statusType == SVNStatusType.STATUS_ADDED) {
          return status.isCopied();
        }
        return !(status.getContentsStatus() == SVNStatusType.STATUS_UNVERSIONED ||
                 status.getContentsStatus() == SVNStatusType.STATUS_IGNORED ||
                 status.getContentsStatus() == SVNStatusType.STATUS_OBSTRUCTED);
      }
    }
    catch (SVNException e) {
      //
    }
    return false;
  }

  public boolean fileIsUnderVcs(FilePath path) {
    final ChangeListManager clManager = ChangeListManager.getInstance(myProject);
    return (! SvnStatusUtil.isIgnoredInAnySense(clManager, path.getVirtualFile())) && (! clManager.isUnversioned(path.getVirtualFile()));
  }

  private static File getEntriesFile(File file) {
    return file.isDirectory() ? new File(file, pathToEntries) : new File(file.getParentFile(), pathToEntries);
  }

  private static File getDirPropsFile(File file) {
    return new File(file, pathToDirProps);
  }

  @Nullable
  public SVNInfo getInfo(final VirtualFile file) {
    try {
      SVNWCClient wcClient = new SVNWCClient(getSvnAuthenticationManager(), getSvnOptions());
      SVNInfo info = wcClient.doInfo(new File(file.getPath()), SVNRevision.WORKING);
      if (info == null || info.getRepositoryRootURL() == null) {
        info = wcClient.doInfo(new File(file.getPath()), SVNRevision.HEAD);
      }
      return info;
    }
    catch (SVNException e) {
      return null;
    }
  }

  public static class SVNStatusHolder {

    private final SVNStatus myValue;
    private final long myEntriesTimestamp;
    private final long myFileTimestamp;
    private final boolean myIsLocked;

    public SVNStatusHolder(long entriesStamp, long fileStamp, SVNStatus value) {
      myValue = value;
      myEntriesTimestamp = entriesStamp;
      myFileTimestamp = fileStamp;
      myIsLocked = value != null && value.isLocked();
    }

    public long getEntriesTimestamp() {
      return myEntriesTimestamp;
    }

    public long getFileTimestamp() {
      return myFileTimestamp;
    }

    public boolean isLocked() {
      return myIsLocked;
    }

    public SVNStatus getStatus() {
      return myValue;
    }
  }

  public static class SVNInfoHolder {

    private final SVNInfo myValue;
    private final long myEntriesTimestamp;
    private final long myFileTimestamp;

    public SVNInfoHolder(long entriesStamp, long fileStamp, SVNInfo value) {
      myValue = value;
      myEntriesTimestamp = entriesStamp;
      myFileTimestamp = fileStamp;
    }

    public long getEntriesTimestamp() {
      return myEntriesTimestamp;
    }

    public long getFileTimestamp() {
      return myFileTimestamp;
    }

    public SVNInfo getInfo() {
      return myValue;
    }
  }

  private static class JavaSVNDebugLogger extends SVNDebugLogAdapter {
    private final boolean myLoggingEnabled;
    private final Logger myLog;
    @NonNls public static final String TRACE_LOG_PARAMETER_NAME = "javasvn.log.trace";

    public JavaSVNDebugLogger(boolean loggingEnabled, Logger log) {
      myLoggingEnabled = loggingEnabled;
      myLog = log;
    }

    public void log(final SVNLogType logType, final Throwable th, final Level logLevel) {
      if (myLoggingEnabled) {
        myLog.info(th);
      }
    }

    public void log(final SVNLogType logType, final String message, final Level logLevel) {
      if (myLoggingEnabled) {
        myLog.info(message);
      }
    }

    public void log(final SVNLogType logType, final String message, final byte[] data) {
      if (myLoggingEnabled) {
        if (data != null) {
          try {
            myLog.info(message + "\n" + new String(data, "UTF-8"));
          }
          catch (UnsupportedEncodingException e) {
            myLog.info(message + "\n" + new String(data));
          }
        } else {
          myLog.info(message);
        }
      }
    }
  }

  public FileStatus[] getProvidedStatuses() {
    return new FileStatus[]{SvnFileStatus.EXTERNAL,
      SvnFileStatus.OBSTRUCTED,
      SvnFileStatus.REPLACED};
  }


  @Override @NotNull
  public CommittedChangesProvider<SvnChangeList, ChangeBrowserSettings> getCommittedChangesProvider() {
    if (myCommittedChangesProvider == null) {
      myCommittedChangesProvider = new SvnCommittedChangesProvider(myProject);
    }
    return myCommittedChangesProvider;
  }

  @Nullable
  @Override
  public VcsRevisionNumber parseRevisionNumber(final String revisionNumberString) {
    final SVNRevision revision = SVNRevision.parse(revisionNumberString);
    if (revision.equals(SVNRevision.UNDEFINED)) {
      return null;
    }
    return new SvnRevisionNumber(revision);
  }

  @Override
  public String getRevisionPattern() {
    return ourIntegerPattern;
  }

  @Override
  public boolean isVersionedDirectory(final VirtualFile dir) {
    return SvnUtil.seemsLikeVersionedDir(dir);
  }

  @NotNull
  public SvnFileUrlMapping getSvnFileUrlMapping() {
    if (myMapping == null) {
      myMapping = SvnFileUrlMappingImpl.getInstance(myProject);
    }
    return myMapping;
  }

  /**
   * Returns real working copies roots - if there is <Project Root> -> Subversion setting,
   * and there is one working copy, will return one root
   */
  public List<WCInfo> getAllWcInfos() {
    final SvnFileUrlMapping urlMapping = getSvnFileUrlMapping();

    final List<RootUrlInfo> infoList = urlMapping.getAllWcInfos();
    final List<WCInfo> infos = new ArrayList<WCInfo>();
    for (RootUrlInfo info : infoList) {
      final File file = info.getIoFile();
      infos.add(new WCInfo(file.getAbsolutePath(), info.getAbsoluteUrlAsUrl(),
                           SvnFormatSelector.getWorkingCopyFormat(file), info.getRepositoryUrl(), SvnUtil.isWorkingCopyRoot(file)));
    }
    return infos;
  }

  private class SvnWorkingCopyChecker {
    private List<WCInfo> myAllWcInfos;

    public boolean upgradeNeeded() {
      myAllWcInfos = getAllWcInfos();
      for (WCInfo info : myAllWcInfos) {
        if (! WorkingCopyFormat.ONE_DOT_SIX.equals(info.getFormat())) {
          return true;
        }
      }
      return false;
    }

    public void doUpgrade() {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          final SvnFormatWorker formatWorker = new SvnFormatWorker(myProject, WorkingCopyFormat.ONE_DOT_SIX, myAllWcInfos);
          // additionally ask about working copies with roots above the project root
          formatWorker.checkForOutsideCopies();
          if (formatWorker.haveStuffToConvert()) {
            ProgressManager.getInstance().run(formatWorker);
          }
        }
      });
    }
  }

  @Override
  public RootsConvertor getCustomConvertor() {
    return getSvnFileUrlMapping();
  }

  @Override
  public MergeProvider getMergeProvider() {
    if (myMergeProvider == null) {
      myMergeProvider = new SvnMergeProvider(myProject);
    }
    return myMergeProvider;
  }

  @Override
  public List<AnAction> getAdditionalActionsForLocalChange() {
    return Arrays.<AnAction>asList(new ShowPropertiesDiffWithLocalAction());
  }

  public void pathChanged(final File from, final File to) throws SVNException {
    myChangeListListener.pathChanged(from, to);
  }

  private String keyForVf(final VirtualFile vf) {
    return vf.getUrl();
  }

  @Override
  public boolean allowsNestedRoots() {
    return SvnConfiguration.getInstance(myProject).DETECT_NESTED_COPIES;
  }

  @Override
  public List<VirtualFile> filterUniqueRoots(final List<VirtualFile> in) {
    if (in.size() <= 1) return in;
    
    final List<RootUrlPair> infos = new ArrayList<RootUrlPair>(in.size());
    final SvnFileUrlMappingImpl mapping = (SvnFileUrlMappingImpl) getSvnFileUrlMapping();
    for (VirtualFile vf : in) {
      final File ioFile = new File(vf.getPath());
      final SVNURL url = mapping.getUrlForFile(ioFile);
      if (url == null) continue;
      /*if ((info == null) || (! ioFile.getAbsolutePath().equals(info.getIoFile().getAbsolutePath()))) {
        // we get one of roots there, there shouldn't be other paths
        continue;
      }*/
      infos.add(new MyPair(vf, url.toString()));
    }
    final List<RootUrlPair> filtered = new ArrayList<RootUrlPair>(infos.size());
    ForNestedRootChecker.filterOutSuperfluousChildren(this, infos, filtered);

    return ObjectsConvertor.convert(filtered, new Convertor<RootUrlPair, VirtualFile>() {
      public VirtualFile convert(RootUrlPair o) {
        return o.getVirtualFile();
      }
    });
  }

  private static class MyPair implements RootUrlPair {
    private final VirtualFile myFile;
    private final String myUrl;

    private MyPair(VirtualFile file, String url) {
      myFile = file;
      myUrl = url;
    }

    public VirtualFile getVirtualFile() {
      return myFile;
    }

    public String getUrl() {
      return myUrl;
    }
  }

  private static class MyFrameStateListener implements FrameStateListener {
    private final ChangeListManager myClManager;
    private final VcsDirtyScopeManager myDirtyScopeManager;

    private MyFrameStateListener(ChangeListManager clManager, VcsDirtyScopeManager dirtyScopeManager) {
      myClManager = clManager;
      myDirtyScopeManager = dirtyScopeManager;
    }

    public void onFrameDeactivated() {
    }

    public void onFrameActivated() {
      final List<VirtualFile> folders = ((ChangeListManagerImpl)myClManager).getLockedFolders();
      if (! folders.isEmpty()) {
        myDirtyScopeManager.filesDirty(null, folders);
      }
    }
  }

  public static VcsKey getKey() {
    return ourKey;
  }
}
