package com.intellij.eclipse.export.model;

import com.intellij.eclipse.export.IdeaProjectFileConstants;
import com.intellij.eclipse.export.ResourceUtil;
import com.intellij.eclipse.export.FileUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import static org.eclipse.jdt.core.IClasspathContainer.*;
import static org.eclipse.jdt.core.IClasspathEntry.*;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class IdeaModule {
  public static final int MT_NO_TYPE = 0;
  public static final int MT_JAVA = 1;
  public static final int MT_J2EE_WEB = 2;
  public static final int MT_J2EE_EJB = 3;
  public static final int MT_J2EE_EAR = 4;

  private Map properties = new HashMap();

  private IPath webContextRoot;

  private int type = MT_JAVA;

  private IdeaProject ideaProject;
  private IProject eclipseProject;

  public IdeaModule(IdeaProject ideaProject, IProject eclipseProject) {
    this.ideaProject = ideaProject;
    this.eclipseProject = eclipseProject;
  }

  public String getName() {
    return eclipseProject.getName();
  }

  public String getFileName() {
    return getName() + "." + IdeaProjectFileConstants.IDEA_MODULE_FILE_EXT;
  }

  public File getRelativeFile() {
    return new File(getRelativeDirectory(), getFileName());
  }

  public File getRelativeDirectory() {
    IPath projectPath = eclipseProject.getWorkspace().getRoot().getLocation();
    IPath modulePath = eclipseProject.getLocation();

    if (projectPath.isPrefixOf(modulePath)) {
      return new File(relativize(projectPath.toString(), modulePath.toString()));
    }

    return new File(modulePath.lastSegment());
  }

  public String relativize(String path) {
    return relativize(eclipseProject.getLocation().toString(), path);
  }

  public static String relativize(String relativizeFrom, String path) {
    File projectLocation = new File(relativizeFrom) {
      public boolean isDirectory() { return true; }
    };
    File pathLocation = new File(path);

    String result = FileUtil.getRelativePath(projectLocation, pathLocation);

    if (result == null) return path;
    if (result.equals(".")) return "";

    return result.replace('\\', '/'); // BAD
  }

  public String getContentRoot() {
    return eclipseProject.getLocation().toString();
  }

  public List<String> getSourceLocations() {
    List<String> result = new ArrayList<String>();

    for (IClasspathEntry entry : getEclipseRawClasspath()) {
      if (entry.getEntryKind() == CPE_SOURCE) {
        String path = ResourceUtil.getFullPath(entry.getPath(), eclipseProject);
        result.add(ResourceUtil.transormToCanonicalPathString(path));
      }
    }

    return result;
  }

  public List<IdeaModule> getReferencedModules() {
    try {
      List<IdeaModule> result = new ArrayList<IdeaModule>();

      for (IProject p : eclipseProject.getReferencedProjects()) {
        IdeaModule module = ideaProject.getModuleFor(p);
        if (module != null) {
          result.add(module);
        }
      }

      return result;
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  public List<Library> getLibraries() {
    List<Library> result = new ArrayList<Library>();

    for (IClasspathEntry entry : getEclipseRawClasspath()) {
      if (entry.getEntryKind() == CPE_LIBRARY || entry.getEntryKind() == CPE_VARIABLE) {
        result.add(new Library(entry, eclipseProject));
      }
    }

    return result;
  }

  public List<ModuleLibrary> getModuleLibraries() {
    List<ModuleLibrary> result = new ArrayList<ModuleLibrary>();

    for (IClasspathEntry entry : getEclipseRawClasspath()) {
      if (entry.getEntryKind() != CPE_CONTAINER) continue;

      IClasspathContainer container = getEclipseProjectClasspathContainer(entry.getPath());

      if (container == null) continue;
      if (container.getKind() != K_APPLICATION && container.getKind() != K_SYSTEM) continue;

      result.add(new ModuleLibrary(entry, container, eclipseProject));
    }

    return result;
  }

  public List<String> getPathVariables() {
    Set<String> result = new HashSet<String>();
    for (Library lib : getLibraries()) {
      collectPathVariables(result, lib.getResource());
    }

    for (ModuleLibrary lib : getModuleLibraries()) {
      collectPathVariables(result, lib.getResources());
    }
    return new ArrayList<String>(result);
  }

  private void collectPathVariables(Collection<String> result,
                                    Resource... resources) {
    for (Resource r : resources) {
      // todo betted replace with lib.addVariableName(result)
      if (r.isVariable()) {
        result.add(r.getVariableName());
      }
    }
  }

  public String getJdkName() {
    IVMInstall vm = getEclipseVMInstall();

    if (vm == null) return null;

    String jdkName = vm.getName();
    if (jdkName.equals(ideaProject.getJdkName())) return null;

    return jdkName;
  }

  public LanguageLevel getLanguageLevel() {
    return LanguageLevel.fromEclipseCompilerLevel(getEclipseCompilerLevel());
  }

  // todo make is protected 
  public IProject getEclipseProject() {
    return eclipseProject;
  }

  protected IJavaProject getEclipseJavaProject() {
    return JavaModelManager.getJavaModelManager().getJavaModel()
      .getJavaProject(eclipseProject);
  }

  protected IClasspathContainer getEclipseProjectClasspathContainer(IPath path) {
    try {
      return JavaCore.getClasspathContainer(path, getEclipseJavaProject());
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
  }

  protected IVMInstall getEclipseVMInstall() {
    try {
      return JavaRuntime.getVMInstall(getEclipseJavaProject());
    } catch (CoreException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getEclipseCompilerLevel() {
    return getEclipseJavaProject().getOption(JavaCore.COMPILER_COMPLIANCE, false);
  }

  private IClasspathEntry[] getEclipseRawClasspath() {
    try {
      return getEclipseJavaProject().getRawClasspath();
    } catch (JavaModelException e) {
      throw new RuntimeException(e);
    }
  }

  public void copyContentTo(File destDir) throws IOException {
    // todo test it!!!
    // todo and refactor!!!! there are lots of hacks over here!!!
    if (destDir.equals(new File(getContentRoot()))) return;

    for (String loc : getSourceLocations()) {
      if (isFileUnderTheModule(loc)) {
        copyFolder(new File(loc), new File(destDir, relativize(loc)));
      }
    }

    for (Library lib : getLibraries()) {
      if (lib.isLocal()) {
        String path = lib.getResource().getAbsolutePath();

        // todo factor ouy this ugly hack!!!
        if (isFileUnderTheModule(path)) {
          File sourceFile = new File(path);
          File destFile = new File(destDir, relativize(path));

          if (sourceFile.isDirectory()) copyFolder(sourceFile, destFile);
          else copyFile(sourceFile, destFile);
        }
      }
    }
  }

  private boolean isFileUnderTheModule(String path) {
    // todo move this method or better remove
    return new Path(getContentRoot()).isPrefixOf(new Path(path));
  }

  protected void copyFolder(File srcFolder, File destFolder) throws IOException {
    destFolder.mkdirs();

    for (File srcFile : srcFolder.listFiles()) {
      File destFile = new File(destFolder, srcFile.getName());

      if (srcFile.isFile()) {
        copyFile(srcFile, destFile);
      } else if (isDirectory(srcFile)) {
        copyFolder(srcFile, destFile);
      }
    }
  }

  private void copyFile(File srcFile, File destFile) throws IOException {
    FileUtil.copy(srcFile, destFile);
  }

  private boolean isDirectory(File file) {
    if (file.getName().equals(".")) return false;
    if (file.getName().equals("..")) return false;

    return file.isDirectory();
  }

  // todo clean up next methods:

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getProperty(String propertyName) {
    return (String)properties.get(propertyName);
  }

  public void setProperty(String propertyName, String value) {
    properties.put(propertyName, value);
  }

  public IPath getWebContextRoot() {
    return webContextRoot;
  }

  public void setWebContextRoot(IPath webContextRoot) {
    this.webContextRoot = webContextRoot;
  }
}