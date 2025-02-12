package org.jetbrains.idea.svn;

import com.intellij.lifecycle.AtomicSectionsAware;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.ObjectsConvertor;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.InvokeAfterUpdateMode;
import com.intellij.openapi.vcs.changes.VcsDirtyScopeManager;
import com.intellij.openapi.vcs.impl.StringLenComparator;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.containers.Convertor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNURLUtil;
import org.tmatesoft.svn.core.wc.*;

import java.io.File;
import java.util.*;

@State(
  name = "SvnFileUrlMappingImpl",
  storages = {
    @Storage(
      id ="other",
      file = "$WORKSPACE_FILE$"
    )}
)
class SvnFileUrlMappingImpl implements SvnFileUrlMapping, PersistentStateComponent<SvnMappingSavedPart>, ProjectComponent {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.idea.svn.SvnFileUrlMappingImpl");

  private final SvnCompatibilityChecker myChecker;
  private final Object myMonitor = new Object();
  // strictly: what real roots are under what vcs mappings
  private final SvnMapping myMapping;
  // grouped; if there are several mappings one under another, will return the upmost
  private final SvnMapping myMoreRealMapping;
  private final MyRootsHelper myHelper;
  private final Project myProject;
  private final NestedCopiesSink myTempSink;
  private boolean myInitialized;

  private class MyRootsHelper extends ThreadLocalDefendedInvoker<VirtualFile[]> {
    private final ProjectLevelVcsManager myPlVcsManager;

    private MyRootsHelper(final ProjectLevelVcsManager vcsManager) {
      myPlVcsManager = vcsManager;
    }

    protected VirtualFile[] execute() {
      return myPlVcsManager.getRootsUnderVcs(SvnVcs.getInstance(myProject));
    }
  }

  public static SvnFileUrlMappingImpl getInstance(final Project project) {
    return project.getComponent(SvnFileUrlMappingImpl.class);
  }

  private SvnFileUrlMappingImpl(final Project project, final ProjectLevelVcsManager vcsManager) {
    myProject = project;
    myMapping = new SvnMapping();
    myMoreRealMapping = new SvnMapping();
    myHelper = new MyRootsHelper(vcsManager);
    myChecker = new SvnCompatibilityChecker(project);
    myTempSink = new NestedCopiesSink();
  }

  @Nullable
  public SVNURL getUrlForFile(final File file) {
    final RootUrlInfo rootUrlInfo = getWcRootForFilePath(file);
    if (rootUrlInfo == null) {
      return null;
    }

    final String absolutePath = file.getAbsolutePath();
    final String rootAbsPath = rootUrlInfo.getIoFile().getAbsolutePath();
    if (absolutePath.length() < rootAbsPath.length()) {
      // remove last separator from etalon name
      if (absolutePath.equals(rootAbsPath.substring(0, rootAbsPath.length() - 1))) {
        return rootUrlInfo.getAbsoluteUrlAsUrl();
      }
      return null;
    }
    final String relativePath = absolutePath.substring(rootAbsPath.length());
    try {
      return rootUrlInfo.getAbsoluteUrlAsUrl().appendPath(FileUtil.toSystemIndependentName(relativePath), true);
    }
    catch (SVNException e) {
      LOG.info(e);
      return null;
    }
  }

  @Nullable
  public String getLocalPath(final String url) {
    synchronized (myMonitor) {
      final String rootUrl = getUrlRootForUrl(url);
      if (rootUrl == null) {
        return null;
      }
      final RootUrlInfo parentInfo = myMapping.byUrl(rootUrl);
      if (parentInfo == null) {
        return null;
      }

      return fileByUrl(parentInfo.getIoFile().getAbsolutePath(), rootUrl, url).getAbsolutePath();
    }
  }

  public static File fileByUrl(final String parentPath, final String parentUrl, final String childUrl) {
    return new File(parentPath, childUrl.substring(parentUrl.length()));
  }

  @Nullable
  public RootUrlInfo getWcRootForFilePath(final File file) {
    synchronized (myMonitor) {
      final String root = getRootForPath(file);
      if (root == null) {
        return null;
      }

      return myMapping.byFile(root);
    }
  }

  public boolean rootsDiffer() {
    synchronized (myMonitor) {
      return myMapping.isRootsDifferFromSettings();
    }
  }

  @Nullable
  public RootUrlInfo getWcRootForUrl(final String url) {
    synchronized (myMonitor) {
      final String rootUrl = getUrlRootForUrl(url);
      if (rootUrl == null) {
        return null;
      }

      final RootUrlInfo result = myMapping.byUrl(rootUrl);
      if (result == null) {
        LOG.info("Inconsistent maps for url:" + url + " found root url: " + rootUrl);
        return null;
      }
      return result;
    }
  }

  /**
   * Returns real working copies roots - if there is <Project Root> -> Subversion setting,
   * and there is one working copy, will return one root
   */
  public List<RootUrlInfo> getAllWcInfos() {
    synchronized (myMonitor) {
      // a copy is created inside
      return myMoreRealMapping.getAllCopies();
    }
  }

  public List<VirtualFile> convertRoots(final List<VirtualFile> result) {
    if (myHelper.isInside()) return result;

    synchronized (myMonitor) {
      final List<VirtualFile> cachedRoots = myMapping.getUnderVcsRoots();
      final List<VirtualFile> lonelyRoots = myMapping.getLonelyRoots();
      if (! lonelyRoots.isEmpty()) {
        myChecker.reportNoRoots(lonelyRoots);
      }
      if (cachedRoots.isEmpty()) {
        // todo +-
        return result;
      }
      return cachedRoots;
    }
  }

  public void acceptNestedData(final Set<NestedCopiesBuilder.MyPointInfo> set) {
    myTempSink.add(set);
  }

  private boolean init() {
    synchronized (myMonitor) {
      final boolean result = myInitialized;
      myInitialized = true;
      return result;
    }
  }

  public void realRefresh(final AtomicSectionsAware atomicSectionsAware) {
    final SvnVcs vcs = SvnVcs.getInstance(myProject);
    final VirtualFile[] roots = myHelper.executeDefended();

    final CopiesApplier copiesApplier = new CopiesApplier();
    final CopiesDetector copiesDetector = new CopiesDetector(atomicSectionsAware, vcs, copiesApplier, new Getter<NestedCopiesData>() {
      public NestedCopiesData get() {
        return myTempSink.receive();
      }
    });
    // do not send additional request for nested copies when in init state
    copiesDetector.detectCopyRoots(roots, init());
  }

  private class CopiesApplier {
    public void apply(final SvnVcs vcs, final AtomicSectionsAware atomicSectionsAware, final List<RootUrlInfo> roots,
                      final List<VirtualFile> lonelyRoots) {
      final SvnMapping mapping = new SvnMapping();
      mapping.addAll(roots);
      mapping.reportLonelyRoots(lonelyRoots);

      final SvnMapping groupedMapping = new SvnMapping();
      final List<RootUrlInfo> filtered = new ArrayList<RootUrlInfo>();
      ForNestedRootChecker.filterOutSuperfluousChildren(vcs, roots, filtered);

      groupedMapping.addAll(filtered);

      // apply
      try {
        atomicSectionsAware.enter();
        synchronized (myMonitor) {
          myMapping.copyFrom(mapping);
          myMoreRealMapping.copyFrom(groupedMapping);
        }
      } finally {
        atomicSectionsAware.exit();
      }
      myProject.getMessageBus().syncPublisher(SvnVcs.ROOTS_RELOADED).run();
    }
  }

  private static class CopiesDetector {
    private final AtomicSectionsAware myAtomicSectionsAware;
    private final SvnVcs myVcs;
    private final CopiesApplier myApplier;
    private final List<VirtualFile> myLonelyRoots;
    private final List<RootUrlInfo> myTopRoots;
    private final RepositoryRoots myRepositoryRoots;
    private final Getter<NestedCopiesData> myGate;

    private CopiesDetector(final AtomicSectionsAware atomicSectionsAware, final SvnVcs vcs, final CopiesApplier applier,
                           final Getter<NestedCopiesData> gate) {
      myAtomicSectionsAware = atomicSectionsAware;
      myVcs = vcs;
      myApplier = applier;
      myGate = gate;
      myTopRoots = new ArrayList<RootUrlInfo>();
      myLonelyRoots = new ArrayList<VirtualFile>();
      myRepositoryRoots = new RepositoryRoots(myVcs);
    }

    public void detectCopyRoots(final VirtualFile[] roots, final boolean clearState) {
      final Getter<Boolean> cancelGetter = new Getter<Boolean>() {
        public Boolean get() {
          return myAtomicSectionsAware.shouldExitAsap();
        }
      };

      for (final VirtualFile vcsRoot : roots) {
        final List<Real> foundRoots = ForNestedRootChecker.getAllNestedWorkingCopies(vcsRoot, myVcs, false, cancelGetter);
        if (foundRoots.isEmpty()) {
          myLonelyRoots.add(vcsRoot);
        }
        // filter out bad(?) items
        for (Real foundRoot : foundRoots) {
          final SVNURL repoRoot = foundRoot.getInfo().getRepositoryRootURL();
          if (repoRoot == null) {
            LOG.info("Error: cannot find repository URL for versioned folder: " + foundRoot.getFile().getPath());
          } else {
            myRepositoryRoots.register(repoRoot);
            myTopRoots.add(new RootUrlInfo(repoRoot, foundRoot.getInfo().getURL(),
                                       SvnFormatSelector.getWorkingCopyFormat(foundRoot.getInfo().getFile()), foundRoot.getFile(), vcsRoot));
          }
        }
      }

      if (! SvnConfiguration.getInstance(myVcs.getProject()).DETECT_NESTED_COPIES) {
        myApplier.apply(myVcs, myAtomicSectionsAware, myTopRoots, myLonelyRoots);
      } else {
        addNestedRoots(clearState);
      }
    }

    private void addNestedRoots(final boolean clearState) {
      final List<VirtualFile> basicVfRoots = ObjectsConvertor.convert(myTopRoots, new Convertor<RootUrlInfo, VirtualFile>() {
        public VirtualFile convert(final RootUrlInfo real) {
          return real.getVirtualFile();
        }
      });

      final ChangeListManager clManager = ChangeListManager.getInstance(myVcs.getProject());

      if (clearState) {
        // clear what was reported before (could be for currently-not-existing roots)
        myGate.get();
      }
      clManager.invokeAfterUpdate(new Runnable() {
        public void run() {
          final List<RootUrlInfo> nestedRoots = new ArrayList<RootUrlInfo>();
          
          for (NestedCopiesBuilder.MyPointInfo info : myGate.get().getSet()) {
            if (NestedCopyType.external.equals(info.getType())) {
              try {
                final SVNStatus svnStatus = SvnUtil.getStatus(myVcs, new File(info.getFile().getPath()));
                if (svnStatus.getURL() == null) continue;
                info.setUrl(svnStatus.getURL());
                info.setFormat(WorkingCopyFormat.getInstance(svnStatus.getWorkingCopyFormat()));
              }
              catch (Exception e) {
                continue;
              }
            }
            for (RootUrlInfo topRoot : myTopRoots) {
              if (VfsUtil.isAncestor(topRoot.getVirtualFile(), info.getFile(), true)) {
                final SVNURL repoRoot = myRepositoryRoots.ask(info.getUrl());
                if (repoRoot != null) {
                  nestedRoots.add(new RootUrlInfo(repoRoot, info.getUrl(), info.getFormat(), info.getFile(), topRoot.getRoot()));
                }
                break;
              }
            }
          }

          myTopRoots.addAll(nestedRoots);
          myApplier.apply(myVcs, myAtomicSectionsAware, myTopRoots, myLonelyRoots);
        }
      }, InvokeAfterUpdateMode.SILENT_CALLBACK_POOLED, null, new Consumer<VcsDirtyScopeManager>() {
        public void consume(VcsDirtyScopeManager vcsDirtyScopeManager) {
          if (clearState) {
            vcsDirtyScopeManager.filesDirty(null, basicVfRoots);
          }
        }
      }, null);
    }
  }

  private static SVNStatus getExternalItemStatus(final SvnVcs vcs, final File file) {
    final SVNStatusClient statusClient = vcs.createStatusClient();
    try {
      if (file.isDirectory()) {
        return statusClient.doStatus(file, false);
      } else {
        final File parent = file.getParentFile();
        if (parent != null) {
          statusClient.setFilesProvider(new ISVNStatusFileProvider() {
            public Map getChildrenFiles(File parent) {
              return Collections.singletonMap(file.getAbsolutePath(), file);
            }
          });
          final Ref<SVNStatus> refStatus = new Ref<SVNStatus>();
          statusClient.doStatus(parent, SVNRevision.WORKING, SVNDepth.FILES, false, true, false, false, new ISVNStatusHandler() {
            public void handleStatus(final SVNStatus status) throws SVNException {
              if (file.equals(status.getFile())) {
                refStatus.set(status);
              }
            }
          }, null);
          return refStatus.get();
        }
      }
    }
    catch (SVNException e) {
      //
    }
    return null;
  }

  private static class RepositoryRoots {
    private final SvnVcs myVcs;
    private final Set<SVNURL> myRoots;

    private RepositoryRoots(final SvnVcs vcs) {
      myVcs = vcs;
      myRoots = new HashSet<SVNURL>();
    }

    public void register(final SVNURL url) {
      myRoots.add(url);
    }

    public SVNURL ask(final SVNURL url) {
      for (SVNURL root : myRoots) {
        if (root.equals(SVNURLUtil.getCommonURLAncestor(root, url))) {
          return root;
        }
      }
      final SVNURL newRoot = SvnUtil.getRepositoryRoot(myVcs, url);
      if (newRoot != null) {
        myRoots.add(newRoot);
      }
      return newRoot;
    }
  }

  @Nullable
  public String getUrlRootForUrl(final String currentUrl) {
    for (String url : myMapping.getUrls()) {
      if (SVNPathUtil.isAncestor(url, currentUrl)) {
        return url;
      }
    }
    return null;
  }

  @Nullable
  public String getRootForPath(final File currentPath) {
    String convertedPath = currentPath.getAbsolutePath();
    convertedPath = (currentPath.isDirectory() && (! convertedPath.endsWith(File.separator))) ? convertedPath + File.separator :
        convertedPath;
    final List<String> paths;
    synchronized (myMonitor) {
      paths = new ArrayList<String>(myMapping.getFileRoots());
    }
    Collections.sort(paths, StringLenComparator.getDescendingInstance());

    for (String path : paths) {
      if (FileUtil.startsWith(convertedPath, path)) {
        return path;
      }
    }
    return null;
  }

  public VirtualFile[] getNotFilteredRoots() {
    return myHelper.executeDefended();
  }

  public boolean isEmpty() {
    synchronized (myMonitor) {
      return myMapping.isEmpty();
    }
  }

  public SvnMappingSavedPart getState() {
    final SvnMappingSavedPart result = new SvnMappingSavedPart();

    final SvnMapping mapping = new SvnMapping();
    final SvnMapping realMapping = new SvnMapping();
    synchronized (myMonitor) {
      mapping.copyFrom(myMapping);
      realMapping.copyFrom(myMoreRealMapping);
    }

    for (RootUrlInfo info : mapping.getAllCopies()) {
      result.add(convert(info));
    }
    for (RootUrlInfo info : realMapping.getAllCopies()) {
      result.addReal(convert(info));
    }
    return result;
  }

  private SvnCopyRootSimple convert(final RootUrlInfo info) {
    final SvnCopyRootSimple copy = new SvnCopyRootSimple();
    copy.myVcsRoot = FileUtil.toSystemDependentName(info.getRoot().getPath());
    copy.myCopyRoot = info.getIoFile().getAbsolutePath();
    return copy;
  }

  public void loadState(final SvnMappingSavedPart state) {
    final SvnMapping mapping = new SvnMapping();
    final SvnMapping realMapping = new SvnMapping();

    try {
      fillMapping(mapping, state.getMappingRoots());
      fillMapping(realMapping, state.getMoreRealMappingRoots());
    } catch (ProcessCanceledException e) {
      throw e;
    } catch (Throwable t) {
      LOG.info(t);
      return;
    }

    synchronized (myMonitor) {
      myMapping.copyFrom(mapping);
      myMoreRealMapping.copyFrom(realMapping);
    }
  }

  private void fillMapping(final SvnMapping mapping, final List<SvnCopyRootSimple> list) {
    final LocalFileSystem lfs = LocalFileSystem.getInstance();
    
    for (SvnCopyRootSimple simple : list) {
      final VirtualFile copyRoot = lfs.findFileByIoFile(new File(simple.myCopyRoot));
      final VirtualFile vcsRoot = lfs.findFileByIoFile(new File(simple.myVcsRoot));

      if (copyRoot == null || vcsRoot == null) continue;

      final SvnVcs vcs = SvnVcs.getInstance(myProject);
      final SVNInfo svnInfo = vcs.getInfo(copyRoot);
      if ((svnInfo == null) || (svnInfo.getRepositoryRootURL() == null)) continue;

      mapping.add(new RootUrlInfo(svnInfo.getRepositoryRootURL(), svnInfo.getURL(),
                                  SvnFormatSelector.getWorkingCopyFormat(svnInfo.getFile()), copyRoot, vcsRoot));
    }
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }

  @NotNull
  public String getComponentName() {
    return "SvnFileUrlMappingImpl";
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }
}
