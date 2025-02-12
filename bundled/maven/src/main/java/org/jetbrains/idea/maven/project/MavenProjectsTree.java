package org.jetbrains.idea.maven.project;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.apache.maven.artifact.Artifact;
import org.jetbrains.annotations.TestOnly;
import org.jetbrains.idea.maven.embedder.MavenConsole;
import org.jetbrains.idea.maven.embedder.MavenEmbedderWrapper;
import org.jetbrains.idea.maven.utils.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

public class MavenProjectsTree {
  private static final String STORAGE_VERSION = MavenProjectsTree.class.getSimpleName() + ".5";

  private final Object myStateLock = new Object();
  private final ReentrantReadWriteLock myStructureLock = new ReentrantReadWriteLock();
  private final Lock myStructureReadLock = myStructureLock.readLock();
  private final Lock myStructureWriteLock = myStructureLock.writeLock();

  // TODO replace with sets
  private volatile List<String> myManagedFilesPaths = new ArrayList<String>();
  private volatile List<String> myIgnoredFilesPaths = new ArrayList<String>();
  private volatile List<String> myIgnoredFilesPatterns = new ArrayList<String>();
  private volatile Pattern myIgnoredFilesPatternsCache;

  private volatile List<String> myActiveProfiles = new ArrayList<String>();
  private volatile List<String> myTemporarilyRemovedProfiles = new ArrayList<String>();

  private final List<MavenProject> myRootProjects = new ArrayList<MavenProject>();

  private final Map<MavenProject, MavenProjectTimestamp> myTimestamps = new THashMap<MavenProject, MavenProjectTimestamp>();
  private final Map<VirtualFile, MavenProject> myVirtualFileToProjectMapping = new THashMap<VirtualFile, MavenProject>();
  private final Map<MavenId, MavenProject> myMavenIdToProjectMapping = new THashMap<MavenId, MavenProject>();
  private final Map<MavenId, VirtualFile> myMavenIdToFileMapping = new THashMap<MavenId, VirtualFile>();
  private final Map<MavenProject, List<MavenProject>> myAggregatorToModuleMapping = new THashMap<MavenProject, List<MavenProject>>();
  private final Map<MavenProject, MavenProject> myModuleToAggregatorMapping = new THashMap<MavenProject, MavenProject>();

  private final List<Listener> myListeners = ContainerUtil.createEmptyCOWList();

  private final MavenProjectReaderProjectLocator myProjectLocator = new MavenProjectReaderProjectLocator() {
    public VirtualFile findProjectFile(MavenId coordinates) {
      MavenProject project = findProject(coordinates);
      return project == null ? null : project.getFile();
    }
  };

  public static MavenProjectsTree read(File file) throws IOException {
    FileInputStream fs = new FileInputStream(file);
    DataInputStream in = new DataInputStream(fs);

    MavenProjectsTree result = new MavenProjectsTree();
    try {
      try {
        if (!STORAGE_VERSION.equals(in.readUTF())) return null;
        result.myManagedFilesPaths = readList(in);
        result.myIgnoredFilesPaths = readList(in);
        result.myIgnoredFilesPatterns = readList(in);
        result.myActiveProfiles = readList(in);
        result.myRootProjects.addAll(readProjectsRecursively(in, result));
      }
      catch (Throwable e) {
        IOException ioException = new IOException();
        ioException.initCause(e);
        throw ioException;
      }
    }
    finally {
      in.close();
      fs.close();
    }
    return result;
  }

  private static List<String> readList(DataInputStream in) throws IOException {
    int count = in.readInt();
    List<String> result = new ArrayList<String>(count);
    while (count-- > 0) {
      result.add(in.readUTF());
    }
    return result;
  }

  private static List<MavenProject> readProjectsRecursively(DataInputStream in,
                                                            MavenProjectsTree tree) throws IOException {
    int count = in.readInt();
    List<MavenProject> result = new ArrayList<MavenProject>(count);
    while (count-- > 0) {
      MavenProject project = MavenProject.read(in);
      MavenProjectTimestamp timestamp = MavenProjectTimestamp.read(in);
      List<MavenProject> modules = readProjectsRecursively(in, tree);
      if (project != null) {
        result.add(project);
        tree.myTimestamps.put(project, timestamp);
        tree.myVirtualFileToProjectMapping.put(project.getFile(), project);
        tree.myMavenIdToProjectMapping.put(project.getMavenId(), project);
        tree.myMavenIdToFileMapping.put(project.getMavenId(), project.getFile());
        tree.myAggregatorToModuleMapping.put(project, modules);
        for (MavenProject eachModule : modules) {
          tree.myModuleToAggregatorMapping.put(eachModule, project);
        }
      }
    }
    return result;
  }

  public void save(File file) throws IOException {
    synchronized (myStateLock) {
      readLock();
      try {
        file.getParentFile().mkdirs();
        FileOutputStream fs = new FileOutputStream(file);
        DataOutputStream out = new DataOutputStream(fs);
        try {
          out.writeUTF(STORAGE_VERSION);
          writeList(out, myManagedFilesPaths);
          writeList(out, myIgnoredFilesPaths);
          writeList(out, myIgnoredFilesPatterns);
          writeList(out, myActiveProfiles);
          writeProjectsRecursively(out, myRootProjects);
        }
        finally {
          out.close();
          fs.close();
        }
      }
      finally {
        readUnlock();
      }
    }
  }

  private static void writeList(DataOutputStream out, List<String> list) throws IOException {
    out.writeInt(list.size());
    for (String each : list) {
      out.writeUTF(each);
    }
  }

  private void writeProjectsRecursively(DataOutputStream out, List<MavenProject> list) throws IOException {
    out.writeInt(list.size());
    for (MavenProject each : list) {
      each.write(out);
      myTimestamps.get(each).write(out);
      writeProjectsRecursively(out, getModules(each));
    }
  }

  public List<String> getManagedFilesPaths() {
    synchronized (myStateLock) {
      return new ArrayList<String>(myManagedFilesPaths);
    }
  }

  public void resetManagedFilesPathsAndProfiles(List<String> paths, List<String> profiles) {
    synchronized (myStateLock) {
      myManagedFilesPaths = new ArrayList<String>(paths);
    }
    setActiveProfiles(profiles);
  }

  @TestOnly
  public void resetManagedFilesAndProfiles(List<VirtualFile> files, List<String> profiles) {
    resetManagedFilesPathsAndProfiles(MavenUtil.collectPaths(files), profiles);
  }

  public void addManagedFilesWithProfiles(List<VirtualFile> files, List<String> profiles) {
    List<String> newFiles;
    List<String> newProfiles;
    synchronized (myStateLock) {
      newFiles = ContainerUtil.concat(myManagedFilesPaths, MavenUtil.collectPaths(files));
      newProfiles = ContainerUtil.concat(myActiveProfiles, profiles);
    }

    resetManagedFilesPathsAndProfiles(newFiles, newProfiles);
  }

  public void removeManagedFiles(List<VirtualFile> files) {
    synchronized (myStateLock) {
      myManagedFilesPaths.removeAll(MavenUtil.collectPaths(files));
    }
  }

  public List<VirtualFile> getExistingManagedFiles() {
    List<VirtualFile> result = new ArrayList<VirtualFile>();
    for (String path : getManagedFilesPaths()) {
      VirtualFile f = LocalFileSystem.getInstance().findFileByPath(path);
      if (f != null) result.add(f);
    }
    return result;
  }

  public List<String> getIgnoredFilesPaths() {
    synchronized (myStateLock) {
      return new ArrayList<String>(myIgnoredFilesPaths);
    }
  }

  public void setIgnoredFilesPaths(final List<String> paths) {
    doChangeIgnoreStatus(new Runnable() {
      public void run() {
        myIgnoredFilesPaths = new ArrayList<String>(paths);
      }
    });
  }

  public boolean getIgnoredState(MavenProject project) {
    synchronized (myStateLock) {
      return myIgnoredFilesPaths.contains(project.getPath());
    }
  }

  public void setIgnoredState(List<MavenProject> projects, boolean ignored) {
    setIgnoredState(projects, ignored, null);
  }

  public void setIgnoredState(List<MavenProject> projects, boolean ignored, Object message) {
    doSetIgnoredState(projects, ignored, message);
  }

  private void doSetIgnoredState(List<MavenProject> projects, final boolean ignored, Object message) {
    final List<String> paths = MavenUtil.collectPaths(MavenUtil.collectFiles(projects));
    doChangeIgnoreStatus(new Runnable() {
      public void run() {
        if (ignored) {
          myIgnoredFilesPaths.addAll(paths);
        }
        else {
          myIgnoredFilesPaths.removeAll(paths);
        }
      }
    }, message);
  }

  public List<String> getIgnoredFilesPatterns() {
    synchronized (myStateLock) {
      return new ArrayList<String>(myIgnoredFilesPatterns);
    }
  }

  public void setIgnoredFilesPatterns(final List<String> patterns) {
    doChangeIgnoreStatus(new Runnable() {
      public void run() {
        myIgnoredFilesPatternsCache = null;
        myIgnoredFilesPatterns = new ArrayList<String>(patterns);
      }
    });
  }

  private void doChangeIgnoreStatus(Runnable runnable) {
    doChangeIgnoreStatus(runnable, null);
  }

  private void doChangeIgnoreStatus(Runnable runnable, Object message) {
    List<MavenProject> ignoredBefore;
    List<MavenProject> ignoredAfter;

    synchronized (myStateLock) {
      ignoredBefore = getIgnoredProjects();
      runnable.run();
      ignoredAfter = getIgnoredProjects();
    }

    List<MavenProject> ignored = new ArrayList<MavenProject>(ignoredAfter);
    ignored.removeAll(ignoredBefore);

    List<MavenProject> unignored = new ArrayList<MavenProject>(ignoredBefore);
    unignored.removeAll(ignoredAfter);

    if (ignored.isEmpty() && unignored.isEmpty()) return;

    fireProjectsIgnoredStateChanged(ignored, unignored, message);
  }

  private List<MavenProject> getIgnoredProjects() {
    List<MavenProject> result = new ArrayList<MavenProject>();
    for (MavenProject each : getProjects()) {
      if (isIgnored(each)) result.add(each);
    }
    return result;
  }

  public boolean isIgnored(MavenProject project) {
    String path = project.getPath();
    synchronized (myStateLock) {
      return myIgnoredFilesPaths.contains(path) || matchesIgnoredFilesPatterns(path);
    }
  }

  private boolean matchesIgnoredFilesPatterns(String path) {
    synchronized (myStateLock) {
      if (myIgnoredFilesPatternsCache == null) {
        myIgnoredFilesPatternsCache = Pattern.compile(Strings.translateMasks(myIgnoredFilesPatterns));
      }
      return myIgnoredFilesPatternsCache.matcher(path).matches();
    }
  }

  public List<String> getActiveProfiles() {
    synchronized (myStateLock) {
      return new ArrayList<String>(myActiveProfiles);
    }
  }

  public void setActiveProfiles(List<String> activeProfiles) {
    synchronized (myStateLock) {
      myActiveProfiles = new ArrayList<String>(activeProfiles);
    }
    fireProfilesChanged(activeProfiles);
  }

  private void updateActiveProfiles() {
    List<String> availableProfiles = getAvailableProfiles();

    synchronized (myStateLock) {
      List<String> removedProfiles = new ArrayList<String>(myActiveProfiles);
      removedProfiles.removeAll(availableProfiles);
      myTemporarilyRemovedProfiles.addAll(removedProfiles);

      List<String> restoredProfiles = new ArrayList<String>(myTemporarilyRemovedProfiles);
      restoredProfiles.retainAll(availableProfiles);
      myTemporarilyRemovedProfiles.removeAll(restoredProfiles);

      myActiveProfiles.removeAll(removedProfiles);
      myActiveProfiles.addAll(restoredProfiles);
    }
  }

  public void updateAll(boolean force, MavenGeneralSettings generalSettings, MavenProgressIndicator process, Object message) {
    List<VirtualFile> managedFiles = getExistingManagedFiles();
    List<String> activeProfiles = getActiveProfiles();

    MavenProjectReader projectReader = new MavenProjectReader();
    update(managedFiles, true, force, activeProfiles, projectReader, generalSettings, process, message);

    List<VirtualFile> obsoleteFiles = getRootProjectsFiles();
    obsoleteFiles.removeAll(managedFiles);
    delete(projectReader, obsoleteFiles, activeProfiles, generalSettings, process, message);
  }

  public void update(Collection<VirtualFile> files,
                     boolean force,
                     MavenGeneralSettings generalSettings,
                     MavenProgressIndicator process,
                     Object message) {
    update(files, false, force, getActiveProfiles(), new MavenProjectReader(), generalSettings, process, message);
  }

  private void update(Collection<VirtualFile> files,
                      boolean recursive,
                      boolean force,
                      List<String> activeProfiles,
                      MavenProjectReader projectReader,
                      MavenGeneralSettings generalSettings,
                      MavenProgressIndicator process,
                      Object message) {
    if (files.isEmpty()) return;

    UpdateContext updateContext = new UpdateContext();
    Stack<MavenProject> updateStack = new Stack<MavenProject>();

    for (VirtualFile each : files) {
      MavenProject mavenProject = findProject(each);
      if (mavenProject == null) {
        doAdd(each, recursive, activeProfiles, updateContext, updateStack, projectReader, generalSettings, process);
      }
      else {
        doUpdate(mavenProject,
                 findAggregator(mavenProject),
                 false,
                 recursive,
                 force,
                 activeProfiles,
                 updateContext,
                 updateStack,
                 projectReader,
                 generalSettings,
                 process);
      }
    }

    updateActiveProfiles();
    updateContext.fireUpdatedIfNecessary(message);
  }

  private void doAdd(final VirtualFile f,
                     boolean recursuve,
                     List<String> activeProfiles,
                     UpdateContext updateContext,
                     Stack<MavenProject> updateStack,
                     MavenProjectReader reader,
                     MavenGeneralSettings generalSettings,
                     MavenProgressIndicator process) {
    MavenProject newMavenProject = new MavenProject(f);

    MavenProject intendedAggregator = null;
    for (MavenProject each : getProjects()) {
      if (each.getExistingModuleFiles().contains(f)) {
        intendedAggregator = each;
        break;
      }
    }

    doUpdate(newMavenProject,
             intendedAggregator,
             true,
             recursuve,
             false,
             activeProfiles,
             updateContext,
             updateStack,
             reader,
             generalSettings,
             process);
  }

  private void doUpdate(MavenProject mavenProject,
                        MavenProject aggregator,
                        boolean isNew,
                        boolean recursive,
                        boolean force,
                        List<String> activeProfiles,
                        UpdateContext updateContext,
                        Stack<MavenProject> updateStack,
                        MavenProjectReader reader,
                        MavenGeneralSettings generalSettings,
                        MavenProgressIndicator process) {
    if (updateStack.contains(mavenProject)) {
      MavenLog.LOG.info("Recursion detected in " + mavenProject.getFile());
      return;
    }
    updateStack.push(mavenProject);

    process.setText(ProjectBundle.message("maven.reading.pom", mavenProject.getPath()));
    process.setText2("");

    List<MavenProject> prevModules = getModules(mavenProject);
    Set<MavenProject> prevInheritors = isNew
                                       ? new THashSet<MavenProject>()
                                       : findInheritors(mavenProject);

    MavenProjectTimestamp timestamp = calculateTimestamp(mavenProject, activeProfiles, generalSettings);
    boolean isChanged = force || !timestamp.equals(myTimestamps.get(mavenProject));

    MavenProjectChanges changes = force ? MavenProjectChanges.ALL : MavenProjectChanges.NONE;
    if (isChanged) {
      writeLock();
      try {
        if (!isNew) {
          myMavenIdToProjectMapping.remove(mavenProject.getMavenId());
          myMavenIdToFileMapping.remove(mavenProject.getMavenId());
        }
      }
      finally {
        writeUnlock();
      }
      MavenId oldParentId = mavenProject.getParentId();
      changes = changes.mergedWith(mavenProject.read(generalSettings, activeProfiles, reader, myProjectLocator));

      writeLock();
      try {
        myVirtualFileToProjectMapping.put(mavenProject.getFile(), mavenProject);
        myMavenIdToProjectMapping.put(mavenProject.getMavenId(), mavenProject);
        myMavenIdToFileMapping.put(mavenProject.getMavenId(), mavenProject.getFile());
      }
      finally {
        writeUnlock();
      }

      if (!Comparing.equal(oldParentId, mavenProject.getParentId())) {
        // ensure timestamp reflects actual parent's timestamp
        timestamp = calculateTimestamp(mavenProject, activeProfiles, generalSettings);
      }
      myTimestamps.put(mavenProject, timestamp);
    }

    boolean reconnected = isNew;
    if (isNew) {
      connect(aggregator, mavenProject);
    }
    else {
      reconnected = reconnect(aggregator, mavenProject);
    }

    if (isChanged || reconnected) {
      updateContext.update(mavenProject, changes);
    }

    List<VirtualFile> existingModuleFiles = mavenProject.getExistingModuleFiles();
    List<MavenProject> modulesToRemove = new ArrayList<MavenProject>();
    List<MavenProject> modulesToBecomeRoots = new ArrayList<MavenProject>();

    for (MavenProject each : prevModules) {
      VirtualFile moduleFile = each.getFile();
      if (!existingModuleFiles.contains(moduleFile)) {
        if (isManagedFile(moduleFile)) {
          modulesToBecomeRoots.add(each);
        }
        else {
          modulesToRemove.add(each);
        }
      }
    }
    for (MavenProject each : modulesToRemove) {
      removeModule(mavenProject, each);
      doDelete(mavenProject, each, updateContext);
      prevInheritors.removeAll(updateContext.deletedProjects);
    }

    for (MavenProject each : modulesToBecomeRoots) {
      if (reconnect(null, each)) updateContext.update(each, MavenProjectChanges.NONE);
    }

    for (VirtualFile each : existingModuleFiles) {
      MavenProject module = findProject(each);
      boolean isNewModule = module == null;
      if (isNewModule) {
        module = new MavenProject(each);
      }
      else {
        MavenProject currentAggregator = findAggregator(module);
        if (currentAggregator != null && currentAggregator != mavenProject) {
          MavenLog.LOG.info("Module " + each + " is already included into " + mavenProject.getFile());
          continue;
        }
      }

      if (isChanged || isNewModule || recursive) {
        doUpdate(module,
                 mavenProject,
                 isNewModule,
                 recursive,
                 recursive ? force : false, // do not force update modules if only this project was requested to be updated
                 activeProfiles,
                 updateContext,
                 updateStack,
                 reader,
                 generalSettings,
                 process);
      }
      else {
        if (reconnect(mavenProject, module)) {
          updateContext.update(module, MavenProjectChanges.NONE);
        }
      }
    }

    Set<MavenProject> allInheritors = findInheritors(mavenProject);
    allInheritors.addAll(prevInheritors);
    for (MavenProject each : allInheritors) {
      doUpdate(each,
               findAggregator(each),
               false,
               false, // no need to go recursively in case of inheritance, only when updating modules
               false,
               activeProfiles,
               updateContext,
               updateStack,
               reader,
               generalSettings,
               process);
    }

    updateStack.pop();
  }

  private MavenProjectTimestamp calculateTimestamp(final MavenProject mavenProject,
                                                   final List<String> activeProfiles,
                                                   final MavenGeneralSettings generalSettings) {
    long pomTimestamp = getFileTimestamp(mavenProject.getFile());
    MavenProject parent = findParent(mavenProject);
    long parentLastReadStamp = parent == null ? -1 : parent.getLastReadStamp();
    VirtualFile profilesXmlFile = mavenProject.getProfilesXmlFile();
    long profilesTimestamp = getFileTimestamp(profilesXmlFile);

    long userSettingsTimestamp = getFileTimestamp(generalSettings.getEffectiveUserSettingsFile());
    long globalSettingsTimestamp = getFileTimestamp(generalSettings.getEffectiveGlobalSettingsFile());

    int profilesHashCode = new THashSet<String>(activeProfiles).hashCode();

    return new MavenProjectTimestamp(pomTimestamp,
                                     parentLastReadStamp,
                                     profilesTimestamp,
                                     userSettingsTimestamp,
                                     globalSettingsTimestamp,
                                     profilesHashCode);
  }

  private long getFileTimestamp(VirtualFile file) {
    if (file == null) return -1;
    return file.getTimeStamp();
  }

  public boolean isManagedFile(VirtualFile moduleFile) {
    return isManagedFile(moduleFile.getPath());
  }

  public boolean isManagedFile(String path) {
    for (String each : getManagedFilesPaths()) {
      if (FileUtil.pathsEqual(each, path)) return true;
    }
    return false;
  }

  public boolean isPotentialProject(String path) {
    if (isManagedFile(path)) return true;

    for (MavenProject each : getProjects()) {
      if (FileUtil.pathsEqual(path, each.getPath())) return true;
      if (each.getModulePaths().contains(path)) return true;
    }
    return false;
  }

  public void delete(List<VirtualFile> files,
                     MavenGeneralSettings generalSettings,
                     MavenProgressIndicator process,
                     Object message) {
    delete(new MavenProjectReader(), files, getActiveProfiles(), generalSettings, process, message);
  }

  private void delete(MavenProjectReader projectReader,
                      List<VirtualFile> files,
                      List<String> activeProfiles,
                      MavenGeneralSettings generalSettings,
                      MavenProgressIndicator process,
                      Object message) {
    if (files.isEmpty()) return;

    UpdateContext updateContext = new UpdateContext();
    Stack<MavenProject> updateStack = new Stack<MavenProject>();

    Set<MavenProject> inheritorsToUpdate = new THashSet<MavenProject>();
    for (VirtualFile each : files) {
      MavenProject mavenProject = findProject(each);
      if (mavenProject == null) return;

      inheritorsToUpdate.addAll(findInheritors(mavenProject));
      doDelete(findAggregator(mavenProject), mavenProject, updateContext);
    }
    inheritorsToUpdate.removeAll(updateContext.deletedProjects);

    for (MavenProject each : inheritorsToUpdate) {
      doUpdate(each, null, false, false, false, activeProfiles, updateContext, updateStack, projectReader, generalSettings, process);
    }

    updateActiveProfiles();
    updateContext.fireUpdatedIfNecessary(message);
  }

  private void doDelete(MavenProject aggregator, MavenProject project, UpdateContext updateContext) {
    for (MavenProject each : getModules(project)) {
      if (isManagedFile(each.getPath())) {
        if (reconnect(null, each)) {
          updateContext.update(each, MavenProjectChanges.NONE);
        }
      }
      else {
        doDelete(project, each, updateContext);
      }
    }

    writeLock();
    try {
      if (aggregator != null) {
        removeModule(aggregator, project);
      }
      else {
        myRootProjects.remove(project);
      }
      myTimestamps.remove(project);
      myVirtualFileToProjectMapping.remove(project.getFile());
      myMavenIdToProjectMapping.remove(project.getMavenId());
      myMavenIdToFileMapping.remove(project.getMavenId());
      myAggregatorToModuleMapping.remove(project);
      myModuleToAggregatorMapping.remove(project);
    }
    finally {
      writeUnlock();
    }

    updateContext.deleted(project);
  }

  private void connect(MavenProject newAggregator, MavenProject project) {
    writeLock();
    try {
      if (newAggregator != null) {
        addModule(newAggregator, project);
      }
      else {
        myRootProjects.add(project);
      }
    }
    finally {
      writeUnlock();
    }
  }

  private boolean reconnect(MavenProject newAggregator, MavenProject project) {
    MavenProject prevAggregator = findAggregator(project);

    if (prevAggregator == newAggregator) return false;

    writeLock();
    try {
      if (prevAggregator != null) {
        removeModule(prevAggregator, project);
      }
      else {
        myRootProjects.remove(project);
      }

      if (newAggregator != null) {
        addModule(newAggregator, project);
      }
      else {
        myRootProjects.add(project);
      }
    }
    finally {
      writeUnlock();
    }

    return true;
  }

  public List<String> getAvailableProfiles() {
    Set<String> result = new THashSet<String>();
    for (MavenProject each : getProjects()) {
      result.addAll(each.getProfilesIds());
    }
    return new ArrayList<String>(result);
  }

  public boolean hasProjects() {
    readLock();
    try {
      return !myRootProjects.isEmpty();
    }
    finally {
      readUnlock();
    }
  }

  public List<MavenProject> getRootProjects() {
    readLock();
    try {
      return new ArrayList<MavenProject>(myRootProjects);
    }
    finally {
      readUnlock();
    }
  }

  public List<VirtualFile> getRootProjectsFiles() {
    return MavenUtil.collectFiles(getRootProjects());
  }

  public List<MavenProject> getProjects() {
    readLock();
    try {
      return new ArrayList<MavenProject>(myVirtualFileToProjectMapping.values());
    }
    finally {
      readUnlock();
    }
  }

  public List<MavenProject> getNonIgnoredProjects() {
    readLock();
    try {
      List<MavenProject> result = new ArrayList<MavenProject>();
      for (MavenProject each : myVirtualFileToProjectMapping.values()) {
        if (!isIgnored(each)) result.add(each);
      }
      return result;
    }
    finally {
      readUnlock();
    }
  }

  public List<VirtualFile> getProjectsFiles() {
    readLock();
    try {
      return new ArrayList<VirtualFile>(myVirtualFileToProjectMapping.keySet());
    }
    finally {
      readUnlock();
    }
  }

  public MavenProject findProject(VirtualFile f) {
    readLock();
    try {
      return myVirtualFileToProjectMapping.get(f);
    }
    finally {
      readUnlock();
    }
  }

  public MavenProject findProject(MavenId id) {
    readLock();
    try {
      return myMavenIdToProjectMapping.get(id);
    }
    finally {
      readUnlock();
    }
  }

  public MavenProject findProject(MavenArtifact artifact) {
    return findProject(artifact.getMavenId());
  }

  public MavenProject findProject(Artifact artifact) {
    return findProject(new MavenId(artifact));
  }

  private Map<MavenId, VirtualFile> getProjectIdToFileMapping() {
    readLock();
    try {
      return new THashMap<MavenId, VirtualFile>(myMavenIdToFileMapping);
    }
    finally {
      readUnlock();
    }
  }

  public MavenProject findAggregator(MavenProject project) {
    readLock();
    try {
      return myModuleToAggregatorMapping.get(project);
    }
    finally {
      readUnlock();
    }
  }

  public List<MavenProject> getModules(MavenProject aggregator) {
    readLock();
    try {
      List<MavenProject> modules = myAggregatorToModuleMapping.get(aggregator);
      return modules == null
             ? Collections.<MavenProject>emptyList()
             : new ArrayList<MavenProject>(modules);
    }
    finally {
      readUnlock();
    }
  }

  private void addModule(MavenProject aggregator, MavenProject module) {
    writeLock();
    try {
      List<MavenProject> modules = myAggregatorToModuleMapping.get(aggregator);
      if (modules == null) {
        modules = new ArrayList<MavenProject>();
        myAggregatorToModuleMapping.put(aggregator, modules);
      }
      modules.add(module);

      myModuleToAggregatorMapping.put(module, aggregator);
    }
    finally {
      writeUnlock();
    }
  }

  private void removeModule(MavenProject aggregator, MavenProject module) {
    writeLock();
    try {
      List<MavenProject> modules = myAggregatorToModuleMapping.get(aggregator);
      if (modules == null) return;
      modules.remove(module);
      myModuleToAggregatorMapping.remove(module);
    }
    finally {
      writeUnlock();
    }
  }

  private MavenProject findParent(MavenProject project) {
    return findProject(project.getParentId());
  }

  private Set<MavenProject> findInheritors(MavenProject project) {
    Set<MavenProject> result = new THashSet<MavenProject>();
    MavenId id = project.getMavenId();

    for (MavenProject each : getProjects()) {
      if (each == project) continue;
      if (id.equals(each.getParentId())) result.add(each);
    }
    return result;
  }

  public List<MavenProject> getDependentProjects(MavenProject project) {
    MavenId projectId = project.getMavenId();
    List<MavenProject> result = new ArrayList<MavenProject>();
    for (MavenProject eachProject : getProjects()) {
      if (eachProject == project) continue;
      for (MavenArtifact eachDependency : eachProject.getDependencies()) {
        if (eachDependency.getMavenId().equals(projectId)) {
          result.add(eachProject);
          break;
        }
      }
    }
    return result;
  }

  public void resolve(MavenProject mavenProject,
                      MavenGeneralSettings generalSettings,
                      MavenEmbeddersManager embeddersManager,
                      MavenConsole console,
                      MavenProgressIndicator process,
                      Object message) throws MavenProcessCanceledException {
    MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(MavenEmbeddersManager.EmbedderKind.FOR_DEPENDENCIES_RESOLVE);
    embedder.customizeForResolve(getProjectIdToFileMapping(), console, process);

    try {
      process.checkCanceled();
      process.setText(ProjectBundle.message("maven.resolving.pom", mavenProject.getDisplayName()));
      process.setText2("");
      Pair<MavenProjectChanges, org.apache.maven.project.MavenProject> resolveResult = mavenProject.resolve(generalSettings,
                                                                                                            embedder,
                                                                                                            new MavenProjectReader(),
                                                                                                            myProjectLocator);

      fireProjectResolved(Pair.create(mavenProject, resolveResult.first), resolveResult.second, message);
    }
    finally {
      embeddersManager.release(embedder);
    }
  }

  public void resolvePlugins(MavenProject mavenProject,
                             org.apache.maven.project.MavenProject nativeMavenProject,
                             MavenEmbeddersManager embeddersManager,
                             MavenConsole console,
                             MavenProgressIndicator process) throws MavenProcessCanceledException {
    MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(MavenEmbeddersManager.EmbedderKind.FOR_PLUGINS_RESOLVE);
    embedder.customizeForResolve(console, process);
    embedder.clearCachesFor(mavenProject);

    try {
      for (MavenPlugin each : mavenProject.getPlugins()) {
        process.checkCanceled();
        process.setText(ProjectBundle.message("maven.downloading.pom.plugins", mavenProject.getDisplayName()));
        embedder.resolvePlugin(each, nativeMavenProject);
      }
      firePluginsResolved(mavenProject);
    }
    finally {
      embeddersManager.release(embedder);
    }
  }

  public void resolveFolders(final MavenProject mavenProject,
                             final MavenImportingSettings importingSettings,
                             final MavenEmbeddersManager embeddersManager,
                             final MavenConsole console,
                             final MavenProgressIndicator process,
                             final Object message) throws MavenProcessCanceledException {
    doWithEmbedder(mavenProject,
                   embeddersManager,
                   MavenEmbeddersManager.EmbedderKind.FOR_FOLDERS_RESOLVE,
                   console,
                   process,
                   new EmbedderTask() {
                     public void run(MavenEmbedderWrapper embedder) throws MavenProcessCanceledException {
                       process.checkCanceled();
                       process.setText(ProjectBundle.message("maven.updating.folders.pom", mavenProject.getDisplayName()));
                       process.setText2("");

                       Pair<Boolean, MavenProjectChanges> resolveResult = mavenProject.resolveFolders(embedder,
                                                                                                      importingSettings,
                                                                                                      new MavenProjectReader(),
                                                                                                      console);
                       if (resolveResult.first) {
                         fireFoldersResolved(Pair.create(mavenProject, resolveResult.second), message);
                       }
                     }
                   });
  }

  public void downloadArtifacts(MavenProject mavenProject,
                                MavenEmbeddersManager embeddersManager,
                                MavenConsole console,
                                MavenProgressIndicator process) throws MavenProcessCanceledException {
    MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(MavenEmbeddersManager.EmbedderKind.FOR_DOWNLOAD);
    embedder.customizeForResolve(console, process);

    try {
      MavenArtifactDownloader.download(this, Collections.singletonList(mavenProject), true, embedder, process);
      fireArtifactsDownloaded(mavenProject);
    }
    finally {
      embeddersManager.release(embedder);
    }
  }

  public MavenArtifact downloadArtifact(MavenProject mavenProject,
                                        MavenId id,
                                        MavenEmbeddersManager embeddersManager,
                                        MavenConsole console,
                                        MavenProgressIndicator process) throws MavenProcessCanceledException {
    MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(MavenEmbeddersManager.EmbedderKind.FOR_DOWNLOAD);
    embedder.customizeForResolve(console, process);

    try {
      Artifact artifact = embedder.createArtifact(id.getGroupId(),
                                                  id.getArtifactId(),
                                                  id.getVersion(),
                                                  MavenConstants.TYPE_JAR,
                                                  null);
      artifact.setScope(Artifact.SCOPE_COMPILE);
      embedder.resolve(artifact, mavenProject.getRemoteRepositories());
      return new MavenArtifact(artifact, mavenProject.getLocalRepository());
    }
    finally {
      embeddersManager.release(embedder);
    }
  }

  public void doWithEmbedder(MavenProject mavenProject,
                             MavenEmbeddersManager embeddersManager,
                             MavenEmbeddersManager.EmbedderKind embedderKind,
                             MavenConsole console,
                             MavenProgressIndicator process,
                             EmbedderTask task) throws MavenProcessCanceledException {
    MavenEmbedderWrapper embedder = embeddersManager.getEmbedder(embedderKind);
    embedder.customizeForStrictResolve(getProjectIdToFileMapping(), console, process);
    embedder.clearCachesFor(mavenProject);
    try {
      task.run(embedder);
    }
    finally {
      embeddersManager.release(embedder);
    }
  }

  public <Result> Result visit(Visitor<Result> visitor) {
    for (MavenProject each : getRootProjects()) {
      if (visitor.isDone()) break;
      doVisit(each, visitor);
    }
    return visitor.getResult();
  }

  private <Result> void doVisit(MavenProject project, Visitor<Result> visitor) {
    if (!visitor.isDone() && visitor.shouldVisit(project)) {
      visitor.visit(project);
      for (MavenProject each : getModules(project)) {
        if (visitor.isDone()) break;
        doVisit(each, visitor);
      }
      visitor.leave(project);
    }
  }

  private void writeLock() {
    myStructureWriteLock.lock();
  }

  private void writeUnlock() {
    myStructureWriteLock.unlock();
  }

  private void readLock() {
    myStructureReadLock.lock();
  }

  private void readUnlock() {
    myStructureReadLock.unlock();
  }

  public void addListener(Listener l) {
    myListeners.add(l);
  }

  private void fireProfilesChanged(List<String> profiles) {
    for (Listener each : myListeners) {
      each.profilesChanged(profiles);
    }
  }

  private void fireProjectsIgnoredStateChanged(List<MavenProject> ignored, List<MavenProject> unignored, Object fromImport) {
    for (Listener each : myListeners) {
      each.projectsIgnoredStateChanged(ignored, unignored, fromImport);
    }
  }

  private void fireProjectsUpdated(List<Pair<MavenProject, MavenProjectChanges>> updated, List<MavenProject> deleted, Object message) {
    for (Listener each : myListeners) {
      each.projectsUpdated(updated, deleted, message);
    }
  }

  private void fireProjectResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges,
                                   org.apache.maven.project.MavenProject nativeMavenProject,
                                   Object message) {
    for (Listener each : myListeners) {
      each.projectResolved(projectWithChanges, nativeMavenProject, message);
    }
  }

  private void firePluginsResolved(MavenProject project) {
    for (Listener each : myListeners) {
      each.pluginsResolved(project);
    }
  }

  private void fireFoldersResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges, Object message) {
    for (Listener each : myListeners) {
      each.foldersResolved(projectWithChanges, message);
    }
  }

  private void fireArtifactsDownloaded(MavenProject project) {
    for (Listener each : myListeners) {
      each.artifactsDownloaded(project);
    }
  }

  private class UpdateContext {
    public final Map<MavenProject, MavenProjectChanges> updatedProjectsWithChanges = new LinkedHashMap<MavenProject, MavenProjectChanges>();
    public final Set<MavenProject> deletedProjects = new LinkedHashSet<MavenProject>();

    public void update(MavenProject project, MavenProjectChanges changes) {
      deletedProjects.remove(project);
      updatedProjectsWithChanges.put(project, changes.mergedWith(updatedProjectsWithChanges.get(project)));
    }

    public void deleted(MavenProject project) {
      updatedProjectsWithChanges.remove(project);
      deletedProjects.add(project);
    }

    public void deleted(Collection<MavenProject> projects) {
      for (MavenProject each : projects) {
        deleted(each);
      }
    }

    public void fireUpdatedIfNecessary(Object message) {
      if (updatedProjectsWithChanges.isEmpty() && deletedProjects.isEmpty()) return;
      fireProjectsUpdated(updatedProjectsWithChanges.isEmpty()
                          ? Collections.EMPTY_LIST
                          : MavenUtil.mapToList(updatedProjectsWithChanges),
                          deletedProjects.isEmpty()
                          ? Collections.EMPTY_LIST
                          : new ArrayList<MavenProject>(deletedProjects),
                          message);
    }
  }

  public interface EmbedderTask {
    void run(MavenEmbedderWrapper embedder) throws MavenProcessCanceledException;
  }

  public static abstract class Visitor<Result> {
    private Result result;

    public boolean shouldVisit(MavenProject project) {
      return true;
    }

    public abstract void visit(MavenProject project);

    public void leave(MavenProject node) {
    }

    public void setResult(Result result) {
      this.result = result;
    }

    public Result getResult() {
      return result;
    }

    public boolean isDone() {
      return result != null;
    }
  }

  public static abstract class SimpleVisitor extends Visitor<Object> {
  }

  private static class MavenProjectTimestamp {
    private final long myPomTimestamp;
    private final long myParentLastReadStamp;
    private final long myProfilesTimestamp;
    private final long myUserSettingsTimestamp;
    private final long myGlobalSettingsTimestamp;
    private final long myActiveProfilesHashCode;

    private MavenProjectTimestamp(long pomTimestamp,
                                  long parentLastReadStamp,
                                  long profilesTimestamp,
                                  long userSettingsTimestamp,
                                  long globalSettingsTimestamp,
                                  long activeProfilesHashCode) {
      myPomTimestamp = pomTimestamp;
      myParentLastReadStamp = parentLastReadStamp;
      myProfilesTimestamp = profilesTimestamp;
      myUserSettingsTimestamp = userSettingsTimestamp;
      myGlobalSettingsTimestamp = globalSettingsTimestamp;
      myActiveProfilesHashCode = activeProfilesHashCode;
    }

    public static MavenProjectTimestamp read(DataInputStream in) throws IOException {
      return new MavenProjectTimestamp(in.readLong(),
                                       in.readLong(),
                                       in.readLong(),
                                       in.readLong(),
                                       in.readLong(),
                                       in.readLong());
    }

    public void write(DataOutputStream out) throws IOException {
      out.writeLong(myPomTimestamp);
      out.writeLong(myParentLastReadStamp);
      out.writeLong(myProfilesTimestamp);
      out.writeLong(myUserSettingsTimestamp);
      out.writeLong(myGlobalSettingsTimestamp);
      out.writeLong(myActiveProfilesHashCode);
    }

    @Override
    public String toString() {
      return "(" + myPomTimestamp
             + ":" + myParentLastReadStamp
             + ":" + myProfilesTimestamp
             + ":" + myUserSettingsTimestamp
             + ":" + myGlobalSettingsTimestamp
             + ":" + myActiveProfilesHashCode + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MavenProjectTimestamp timestamp = (MavenProjectTimestamp)o;

      if (myPomTimestamp != timestamp.myPomTimestamp) return false;
      if (myParentLastReadStamp != timestamp.myParentLastReadStamp) return false;
      if (myProfilesTimestamp != timestamp.myProfilesTimestamp) return false;
      if (myUserSettingsTimestamp != timestamp.myUserSettingsTimestamp) return false;
      if (myGlobalSettingsTimestamp != timestamp.myGlobalSettingsTimestamp) return false;
      if (myActiveProfilesHashCode != timestamp.myActiveProfilesHashCode) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = 0;
      result = 31 * result + (int)(myPomTimestamp ^ (myPomTimestamp >>> 32));
      result = 31 * result + (int)(myParentLastReadStamp ^ (myParentLastReadStamp >>> 32));
      result = 31 * result + (int)(myProfilesTimestamp ^ (myProfilesTimestamp >>> 32));
      result = 31 * result + (int)(myUserSettingsTimestamp ^ (myUserSettingsTimestamp >>> 32));
      result = 31 * result + (int)(myGlobalSettingsTimestamp ^ (myGlobalSettingsTimestamp >>> 32));
      result = 31 * result + (int)(myActiveProfilesHashCode ^ (myActiveProfilesHashCode >>> 32));
      return result;
    }
  }

  public interface Listener extends EventListener {
    void profilesChanged(List<String> profiles);

    void projectsIgnoredStateChanged(List<MavenProject> ignored, List<MavenProject> unignored, Object message);

    void projectsUpdated(List<Pair<MavenProject, MavenProjectChanges>> updated, List<MavenProject> deleted, Object message);

    void projectResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges,
                         org.apache.maven.project.MavenProject nativeMavenProject,
                         Object message);

    void pluginsResolved(MavenProject project);

    void foldersResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges, Object message);

    void artifactsDownloaded(MavenProject project);
  }

  public static class ListenerAdapter implements Listener {
    public void profilesChanged(List<String> profiles) {
    }

    public void projectsIgnoredStateChanged(List<MavenProject> ignored, List<MavenProject> unignored, Object message) {
    }

    public void projectsUpdated(List<Pair<MavenProject, MavenProjectChanges>> updated, List<MavenProject> deleted, Object message) {
    }

    public void projectResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges,
                                org.apache.maven.project.MavenProject nativeMavenProject,
                                Object message) {
    }

    public void pluginsResolved(MavenProject project) {
    }

    public void foldersResolved(Pair<MavenProject, MavenProjectChanges> projectWithChanges, Object message) {
    }

    public void artifactsDownloaded(MavenProject project) {
    }
  }
}
