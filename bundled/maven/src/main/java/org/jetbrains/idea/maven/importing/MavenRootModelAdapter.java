package org.jetbrains.idea.maven.importing;

import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.pom.java.LanguageLevel;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenArtifact;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenConstants;
import org.jetbrains.idea.maven.utils.Path;
import org.jetbrains.idea.maven.utils.Url;

import java.io.File;

public class MavenRootModelAdapter {
  private static final String MAVEN_LIB_PREFIX = "Maven: ";
  private final MavenProject myMavenProject;
  private final ModifiableModuleModel myModuleModel;
  private final ModifiableRootModel myRootModel;

  public MavenRootModelAdapter(MavenProject p, Module module, final MavenModifiableModelsProvider rootModelsProvider) {
    myMavenProject = p;
    myModuleModel = rootModelsProvider.getModuleModel();
    myRootModel = rootModelsProvider.getRootModel(module);
  }

  public void init(boolean isNewlyCreatedModule) {
    setupInitialValues(isNewlyCreatedModule);
    initContentRoots();
    initOrderEntries();
  }

  private void setupInitialValues(boolean newlyCreatedModule) {
    if (newlyCreatedModule || myRootModel.getSdk() == null) {
      myRootModel.inheritSdk();
    }
    if (newlyCreatedModule) {
      myRootModel.getModule().setSavePathsRelative(true);
      getCompilerExtension().setExcludeOutput(true);
    }
  }

  private void initContentRoots() {
    Url url = toUrl(myMavenProject.getDirectory());
    if (getContentRootFor(url) != null) return;
    myRootModel.addContentEntry(url.getUrl());
  }

  private ContentEntry getContentRootFor(Url url) {
    for (ContentEntry e : myRootModel.getContentEntries()) {
      if (isEqualOrAncestor(e.getUrl(), url.getUrl())) return e;
    }
    return null;
  }

  private void initOrderEntries() {
    for (OrderEntry e : myRootModel.getOrderEntries()) {
      if (e instanceof ModuleSourceOrderEntry || e instanceof JdkOrderEntry) continue;
      if (e instanceof LibraryOrderEntry) {
        if (!isMavenLibrary(((LibraryOrderEntry)e).getLibrary())) continue;
      }
      if (e instanceof ModuleOrderEntry) {
        Module m = ((ModuleOrderEntry)e).getModule();
        if (m != null && !MavenProjectsManager.getInstance(myRootModel.getProject()).isMavenizedModule(m)) continue;
      }
      myRootModel.removeOrderEntry(e);
    }
  }

  public ModifiableRootModel getRootModel() {
    return myRootModel;
  }

  public Module getModule() {
    return myRootModel.getModule();
  }

  public void addSourceFolder(String path, boolean testSource) {
    if (!exists(path)) return;

    Url url = toUrl(path);
    ContentEntry e = getContentRootFor(url);
    if (e == null) return;
    unregisterAll(path, false, true);
    e.addSourceFolder(url.getUrl(), testSource);
  }

  public boolean hasRegisteredSourceSubfolder(File f) {
    String url = toUrl(f.getPath()).getUrl();
    for (ContentEntry eachEntry : myRootModel.getContentEntries()) {
      for (SourceFolder eachFolder : eachEntry.getSourceFolders()) {
        if (isEqualOrAncestor(url, eachFolder.getUrl())) return true;
      }
    }
    return false;
  }

  public boolean isAlreadyExcluded(File f) {
    String url = toUrl(f.getPath()).getUrl();
    for (ContentEntry eachEntry : myRootModel.getContentEntries()) {
      for (ExcludeFolder eachFolder : eachEntry.getExcludeFolders()) {
        if (isEqualOrAncestor(eachFolder.getUrl(), url)) return true;
      }
    }
    return false;
  }

  private boolean isEqualOrAncestor(String ancestor, String child) {
    return ancestor.equals(child) || StringUtil.startsWithConcatenationOf(child, ancestor, "/");
  }

  private boolean exists(String path) {
    return new File(toPath(path).getPath()).exists();
  }

  public void addExcludedFolder(String path) {
    unregisterAll(path, true, false);
    Url url = toUrl(path);
    ContentEntry e = getContentRootFor(url);
    if (e == null) return;
    e.addExcludeFolder(url.getUrl());
  }

  public void unregisterAll(String path, boolean under, boolean unregisterSources) {
    Url url = toUrl(path);

    for (ContentEntry eachEntry : myRootModel.getContentEntries()) {
      if (unregisterSources) {
        for (SourceFolder eachFolder : eachEntry.getSourceFolders()) {
          String ancestor = under ? url.getUrl() : eachFolder.getUrl();
          String child = under ? eachFolder.getUrl() : url.getUrl();
          if (isEqualOrAncestor(ancestor, child)) {
            eachEntry.removeSourceFolder(eachFolder);
          }
        }
      }

      for (ExcludeFolder eachFolder : eachEntry.getExcludeFolders()) {
        String ancestor = under ? url.getUrl() : eachFolder.getUrl();
        String child = under ? eachFolder.getUrl() : url.getUrl();

        if (isEqualOrAncestor(ancestor, child)) {
          if (eachFolder.isSynthetic()) {
            getCompilerExtension().setExcludeOutput(false);
          }
          else {
            eachEntry.removeExcludeFolder(eachFolder);
          }
        }
      }
    }
  }

  public void useModuleOutput(String production, String test) {
    getCompilerExtension().inheritCompilerOutputPath(false);
    getCompilerExtension().setCompilerOutputPath(toUrl(production).getUrl());
    getCompilerExtension().setCompilerOutputPathForTests(toUrl(test).getUrl());
  }

  private CompilerModuleExtension getCompilerExtension() {
    return myRootModel.getModuleExtension(CompilerModuleExtension.class);
  }

  private Url toUrl(String path) {
    return toPath(path).toUrl();
  }

  private Path toPath(String path) {
    if (!new File(path).isAbsolute()) {
      path = new File(myMavenProject.getDirectory(), path).getPath();
    }
    return new Path(path);
  }

  public void addModuleDependency(String moduleName, boolean isExportable, DependencyScope scope) {
    Module m = findModuleByName(moduleName);

    ModuleOrderEntry e;
    if (m != null) {
      e = myRootModel.addModuleOrderEntry(m);
    }
    else {
      e = myRootModel.addInvalidModuleEntry(moduleName);
    }

    e.setExported(isExportable);
    e.setScope(scope);
  }

  @Nullable
  private Module findModuleByName(String moduleName) {
    return myModuleModel.findModuleByName(moduleName);
  }

  public void addLibraryDependency(MavenArtifact artifact,
                                   boolean isExportable,
                                   DependencyScope scope,
                                   MavenModifiableModelsProvider provider) {
    String libraryName = makeLibraryName(artifact);

    Library library = provider.getLibraryByName(libraryName);
    if (library == null) {
      library = provider.createLibrary(libraryName);
    }

    Library.ModifiableModel libraryModel = provider.getLibraryModel(library);

    setUrl(libraryModel, OrderRootType.CLASSES, artifact, null);
    setUrl(libraryModel, OrderRootType.SOURCES, artifact, MavenConstants.SOURCES_CLASSIFIER);
    setUrl(libraryModel, JavadocOrderRootType.getInstance(), artifact, MavenConstants.JAVADOC_CLASSIFIER);

    LibraryOrderEntry e = myRootModel.addLibraryEntry(library);
    e.setExported(isExportable);
    e.setScope(scope);

    removeOldLibraryDependency(artifact);
  }

  private void setUrl(Library.ModifiableModel libraryModel,
                      OrderRootType type,
                      MavenArtifact artifact,
                      String classifier) {
    for (String url : libraryModel.getUrls(type)) {
      if (isRepositoryUrl(artifact, url, classifier)) {
        libraryModel.removeRoot(url, type);
      }
    }

    libraryModel.addRoot(artifact.getUrlForClassifier(classifier), type);
  }

  private boolean isRepositoryUrl(MavenArtifact artifact, String url, String classifier) {
    return url.endsWith(artifact.getRelativePathForClassifier(classifier) + JarFileSystem.JAR_SEPARATOR);
  }

  public static boolean isChangedByUser(Library library) {
    String[] classRoots = library.getUrls(OrderRootType.CLASSES);
    if (classRoots.length != 1) return true;

    String classes = classRoots[0];

    int dotPos = classes.lastIndexOf(".");
    if (dotPos == -1) return true;
    String path = classes.substring(0, dotPos);

    String jarSuffix = ".jar" + JarFileSystem.JAR_SEPARATOR;
    String sourcesPath = path + "-" + MavenConstants.SOURCES_CLASSIFIER + jarSuffix;
    String javadocPath = path + "-" + MavenConstants.JAVADOC_CLASSIFIER + jarSuffix;

    for (String each : library.getUrls(OrderRootType.SOURCES)) {
      if (!FileUtil.pathsEqual(each, sourcesPath)) return true;
    }
    for (String each : library.getUrls(JavadocOrderRootType.getInstance())) {
      if (!FileUtil.pathsEqual(each, javadocPath)) return true;
    }
    return false;
  }

  private void removeOldLibraryDependency(MavenArtifact artifact) {
    Library lib = findLibrary(artifact, false);
    if (lib == null) return;
    LibraryOrderEntry entry = myRootModel.findLibraryOrderEntry(lib);
    if (entry == null) return;

    myRootModel.removeOrderEntry(entry);
  }

  public Library findLibrary(MavenArtifact artifact) {
    return findLibrary(artifact, true);
  }

  private Library findLibrary(final MavenArtifact artifact, final boolean newType) {
    return myRootModel.processOrder(new RootPolicy<Library>() {
      @Override
      public Library visitLibraryOrderEntry(LibraryOrderEntry e, Library result) {
        String name = newType ? makeLibraryName(artifact) : artifact.getDisplayStringForLibraryName();
        return name.equals(e.getLibraryName()) ? e.getLibrary() : result;
      }
    }, null);
  }

  private static String makeLibraryName(MavenArtifact artifact) {
    return MAVEN_LIB_PREFIX + artifact.getDisplayStringForLibraryName();
  }

  public static boolean isMavenLibrary(Library library) {
    if (library == null) return false;

    String name = library.getName();
    return name != null && name.startsWith(MAVEN_LIB_PREFIX);
  }

  public void setLanguageLevel(LanguageLevel level) {
    try {
      myRootModel.getModuleExtension(LanguageLevelModuleExtension.class).setLanguageLevel(level);
    }
    catch (IllegalArgumentException e) {
      //bad value was stored
    }
  }
}
