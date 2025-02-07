package org.jetbrains.idea.maven.project;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.embedder.MavenEmbedderFactory;
import org.jetbrains.idea.maven.embedder.MavenExecutionOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class MavenGeneralSettings implements Cloneable {
  private boolean workOffline = false;
  private String mavenHome = "";
  private String mavenSettingsFile = "";
  private String localRepository = "";
  private boolean printErrorStackTraces = false;
  private boolean usePluginRegistry = false;
  private boolean nonRecursive = false;
  private MavenExecutionOptions.LoggingLevel outputLevel = MavenExecutionOptions.LoggingLevel.INFO;
  private MavenExecutionOptions.ChecksumPolicy checksumPolicy = MavenExecutionOptions.ChecksumPolicy.FAIL;
  private MavenExecutionOptions.FailureMode failureBehavior = MavenExecutionOptions.FailureMode.FAST;
  private MavenExecutionOptions.SnapshotUpdatePolicy snapshotUpdatePolicy = MavenExecutionOptions.SnapshotUpdatePolicy.ALWAYS_UPDATE;
  private MavenExecutionOptions.PluginUpdatePolicy pluginUpdatePolicy = MavenExecutionOptions.PluginUpdatePolicy.DO_NOT_UPDATE;

  private File myEffectiveLocalRepositoryCache;

  private List<Listener> myListeners = ContainerUtil.createEmptyCOWList();

  public @NotNull MavenExecutionOptions.PluginUpdatePolicy getPluginUpdatePolicy() {
    return pluginUpdatePolicy;
  }

  public void setPluginUpdatePolicy(MavenExecutionOptions.PluginUpdatePolicy value) {
    if (value == null) return; // null may come from deserializator
    this.pluginUpdatePolicy = value;
  }

  @NotNull
  public MavenExecutionOptions.ChecksumPolicy getChecksumPolicy() {
    return checksumPolicy;
  }

  public void setChecksumPolicy(MavenExecutionOptions.ChecksumPolicy value) {
    if (value == null) return; // null may come from deserializator
    this.checksumPolicy = value;
  }

  @NotNull
  public MavenExecutionOptions.FailureMode getFailureBehavior() {
    return failureBehavior;
  }

  public void setFailureBehavior(MavenExecutionOptions.FailureMode value) {
    if (value == null) return; // null may come from deserializator
    this.failureBehavior = value;
  }

  public @NotNull MavenExecutionOptions.LoggingLevel getLoggingLevel() {
    return outputLevel;
  }

  public void setOutputLevel(MavenExecutionOptions.LoggingLevel value) {
    if (value == null) return; // null may come from deserializator
    this.outputLevel = value;
  }

  public @NotNull MavenExecutionOptions.SnapshotUpdatePolicy getSnapshotUpdatePolicy() {
    return snapshotUpdatePolicy;
  }

  public void setSnapshotUpdatePolicy(MavenExecutionOptions.SnapshotUpdatePolicy value) {
    if (value == null) return; // null may come from deserializator
    this.snapshotUpdatePolicy = value;
  }

  public boolean isWorkOffline() {
    return workOffline;
  }

  public void setWorkOffline(boolean workOffline) {
    this.workOffline = workOffline;
  }

  @NotNull
  public String getLocalRepository() {
    return localRepository;
  }

  public File getEffectiveLocalRepository() {
    if (myEffectiveLocalRepositoryCache == null) {
      myEffectiveLocalRepositoryCache = MavenEmbedderFactory.resolveLocalRepository(mavenHome, mavenSettingsFile, localRepository);
    }
    return myEffectiveLocalRepositoryCache;
  }

  public void setLocalRepository(final @Nullable String localRepository) {
    if (localRepository != null) {
      if (!Comparing.equal(this.localRepository, localRepository)) {
        this.localRepository = localRepository;

        myEffectiveLocalRepositoryCache = null;
        firePathChanged();
      }
    }
  }

  @NotNull
  public String getMavenHome() {
    return mavenHome;
  }

  public void setMavenHome(@NotNull final String mavenHome) {
    if (!Comparing.equal(this.mavenHome, mavenHome)) {
      this.mavenHome = mavenHome;

      myEffectiveLocalRepositoryCache = null;
      firePathChanged();
    }
  }

  @Nullable
  public File getEffectiveMavenHome() {
    return MavenEmbedderFactory.resolveMavenHomeDirectory(getMavenHome());
  }

  @NotNull
  public String getMavenSettingsFile() {
    return mavenSettingsFile;
  }

  public void setMavenSettingsFile(@Nullable String mavenSettingsFile) {
    if (mavenSettingsFile != null) {
      if (!Comparing.equal(this.mavenSettingsFile, mavenSettingsFile)) {
        this.mavenSettingsFile = mavenSettingsFile;

        myEffectiveLocalRepositoryCache = null;
        firePathChanged();
      }
    }
  }

  @Nullable
  public File getEffectiveUserSettingsIoFile() {
    return MavenEmbedderFactory.resolveUserSettingsFile(getMavenSettingsFile());
  }

  @Nullable
  public File getEffectiveGlobalSettingsIoFile() {
    return MavenEmbedderFactory.resolveGlobalSettingsFile(getMavenHome());
  }

  @Nullable
  public VirtualFile getEffectiveUserSettingsFile() {
    File file = getEffectiveUserSettingsIoFile();
    return file == null ? null : LocalFileSystem.getInstance().findFileByIoFile(file);
  }

  public List<VirtualFile> getEffectiveSettingsFiles() {
    List<VirtualFile> result = new ArrayList<VirtualFile>(2);
    VirtualFile file = getEffectiveUserSettingsFile();
    if (file != null) result.add(file);
    file = getEffectiveGlobalSettingsFile();
    if (file != null) result.add(file);
    return result;
  }

  @Nullable
  public VirtualFile getEffectiveGlobalSettingsFile() {
    File file = getEffectiveGlobalSettingsIoFile();
    return file == null ? null : LocalFileSystem.getInstance().findFileByIoFile(file);
  }

  @NotNull
  public VirtualFile getEffectiveSuperPom() {
    return MavenEmbedderFactory.resolveSuperPomFile(getMavenHome());
  }

  public boolean isPrintErrorStackTraces() {
    return printErrorStackTraces;
  }

  public void setPrintErrorStackTraces(boolean value) {
    printErrorStackTraces = value;
  }

  public boolean isUsePluginRegistry() {
    return usePluginRegistry;
  }

  public void setUsePluginRegistry(final boolean usePluginRegistry) {
    this.usePluginRegistry = usePluginRegistry;
  }

  public boolean isNonRecursive() {
    return nonRecursive;
  }

  public void setNonRecursive(final boolean nonRecursive) {
    this.nonRecursive = nonRecursive;
  }

  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final MavenGeneralSettings that = (MavenGeneralSettings)o;

    if (nonRecursive != that.nonRecursive) return false;
    if (outputLevel != that.outputLevel) return false;
    if (pluginUpdatePolicy != that.pluginUpdatePolicy) return false;
    if (snapshotUpdatePolicy != that.snapshotUpdatePolicy) return false;
    if (printErrorStackTraces != that.printErrorStackTraces) return false;
    if (usePluginRegistry != that.usePluginRegistry) return false;
    if (workOffline != that.workOffline) return false;
    if (!checksumPolicy.equals(that.checksumPolicy)) return false;
    if (!failureBehavior.equals(that.failureBehavior)) return false;
    if (!localRepository.equals(that.localRepository)) return false;
    if (!mavenHome.equals(that.mavenHome)) return false;
    if (!mavenSettingsFile.equals(that.mavenSettingsFile)) return false;

    return true;
  }

  public int hashCode() {
    int result;
    result = (workOffline ? 1 : 0);
    result = 31 * result + mavenHome.hashCode();
    result = 31 * result + mavenSettingsFile.hashCode();
    result = 31 * result + localRepository.hashCode();
    result = 31 * result + (printErrorStackTraces ? 1 : 0);
    result = 31 * result + (usePluginRegistry ? 1 : 0);
    result = 31 * result + (nonRecursive ? 1 : 0);
    result = 31 * result + outputLevel.hashCode();
    result = 31 * result + checksumPolicy.hashCode();
    result = 31 * result + failureBehavior.hashCode();
    result = 31 * result + pluginUpdatePolicy.hashCode();
    result = 31 * result + snapshotUpdatePolicy.hashCode();
    return result;
  }

  @Override
  public MavenGeneralSettings clone() {
    try {
      MavenGeneralSettings result = (MavenGeneralSettings)super.clone();
      result.myListeners = ContainerUtil.createEmptyCOWList();
      return result;
    }
    catch (CloneNotSupportedException e) {
      throw new Error(e);
    }
  }

  public void addListener(Listener l) {
    myListeners.add(l);
  }

  public void removeListener(Listener l) {
    myListeners.remove(l);
  }

  private void firePathChanged() {
    for (Listener each : myListeners) {
      each.pathChanged();
    }
  }

  public interface Listener {
    void pathChanged();
  }
}
