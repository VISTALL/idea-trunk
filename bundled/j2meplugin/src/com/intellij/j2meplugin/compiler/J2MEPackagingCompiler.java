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
package com.intellij.j2meplugin.compiler;

import com.intellij.j2meplugin.J2MEBundle;
import com.intellij.j2meplugin.module.J2MEModuleProperties;
import com.intellij.j2meplugin.module.J2MEModuleType;
import com.intellij.j2meplugin.module.settings.MobileApplicationType;
import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.io.ZipUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * User: anna
 * Date: Sep 14, 2004
 */
public class J2MEPackagingCompiler implements PackagingCompiler {
  private static final Logger LOG = Logger.getInstance("#com.intellij.j2meplugin");

  private String myJarPath;
  private String myJadPath;
  private String myExplodedJarPath;
  private String myExplodedJadPath;


  private final J2MEPreverifier myJ2MEPreverifier;
  @NonNls public static final String META_INF = "META-INF";

  public J2MEPackagingCompiler(J2MEPreverifier j2mePreverifier) {
    myJ2MEPreverifier = j2mePreverifier;
  }

  @NotNull
  public ProcessingItem[] getProcessingItems(final CompileContext context) {
    final Module[] affectedModules =
      ApplicationManager.getApplication().runReadAction(new Computable<Module[]>() {
        public Module[] compute() {
          return context.getCompileScope().getAffectedModules();
        }
      });
    if (affectedModules == null || affectedModules.length == 0) {
      return ProcessingItem.EMPTY_ARRAY;
    }
    ArrayList<ProcessingItem> result = new ArrayList<ProcessingItem>();
    for (int i = 0; i < affectedModules.length; i++) {
      final Module module = affectedModules[i];
      if (!module.getModuleType().equals(J2MEModuleType.getInstance())) continue;
      setPaths(module);
      final VirtualFile moduleOutputDirectory = context.getModuleOutputDirectory(module);
      if (moduleOutputDirectory != null) {
        result.add(new MyProcessingItem(moduleOutputDirectory,
                                        module));
      }

    }
    return result.toArray(new ProcessingItem[result.size()]);
  }

  private void setPaths(final Module module) {
    final MobileApplicationType mobileApplicationType = J2MEModuleProperties.getInstance(module).getMobileApplicationType();
    final MobileModuleSettings settings = MobileModuleSettings.getInstance(module);
    LOG.assertTrue(settings != null);
    myJarPath = settings.getSettings().get(mobileApplicationType.getJarUrlSettingName());
    myJadPath = settings.getMobileDescriptionPath();
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        //copy to exploded dir
        VirtualFile explodedDir = ModuleRootManager.getInstance(module).getExplodedDirectory();
        if (explodedDir != null) {
          String explodedJarPath = myJarPath.replace(File.separatorChar, '/');
          myExplodedJarPath = explodedDir.getPath() + "/" + explodedJarPath.substring(explodedJarPath.lastIndexOf('/') + 1);
          String explodedJadPath = myJadPath.replace(File.separatorChar, '/');
          myExplodedJadPath = explodedDir.getPath() + "/" + explodedJadPath.substring(explodedJadPath.lastIndexOf('/') + 1);
        } else {
          myExplodedJadPath = null;
          myExplodedJarPath = null;
        }
      }
    });
  }


  public ProcessingItem[] process(final CompileContext context, ProcessingItem[] items) {
    ProgressIndicator progressIndicator = context.getProgressIndicator();
    try {
      progressIndicator.pushState();
      progressIndicator.setText(J2MEBundle.message("compiler.packaging.progress.title"));
      ArrayList<ProcessingItem> result = new ArrayList<ProcessingItem>();
      for (int i = 0; items != null && i < items.length; i++) {
        final Module module = ((MyProcessingItem)items[i]).getModule();
        try {
          final File jarFile = ApplicationManager.getApplication().runReadAction(new Computable<File>() {
            public File compute() {
              try {
                return makeJar(context,
                               new FileFilter() {
                                 public boolean accept(File pathname) {
                                   return true;
                                 }
                               },
                               myJ2MEPreverifier.getModulePreverifiedClasses(module),
                               module);
              }
              catch (IOException e) {
                context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
                return null;
              }

            }
          });
          if (jarFile == null) continue;
          final MobileModuleSettings settings = MobileModuleSettings.getInstance(module);
          setPaths(module);
          MobileMakeUtil.makeJad(settings, true);
          if (myExplodedJarPath != null && myExplodedJadPath != null) {
            if (!jarFile.getPath().replace(File.separatorChar, '/').equals(myExplodedJarPath)) {
              FileUtil.copy(jarFile, new File(myExplodedJarPath));
            }
            final String jadPath = settings.getMobileDescriptionPath().replace(File.separatorChar, '/');
            if (!jadPath.equals(myExplodedJadPath)) {
              FileUtil.copy(new File(jadPath), new File(myExplodedJadPath));
            }
          }
          result.add(items[i]);
        }
        catch (Exception e) {
          context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
        }
      }
      return result.toArray(new ProcessingItem[result.size()]);
    }
    finally {
      progressIndicator.popState();
    }
  }

  @NotNull
  public String getDescription() {
    return J2MEBundle.message("compiler.packaging");
  }

  public boolean validateConfiguration(CompileScope scope) {
    return true;
  }

  public ValidityState createValidityState(DataInput in) throws IOException {
    return new EmptyValidityState();
  }


  private File makeJar(CompileContext context,
                       FileFilter fileFilter,
                       File myDirectoryToJar,
                       Module module) throws IOException {
    MobileModuleSettings settings = MobileModuleSettings.getInstance(module);
    File jarFile = new File(settings.getJarURL());
    FileUtil.createParentDirs(jarFile);
    File manifest = settings.getManifest();/*new Manifest();
    ManifestBuilder.setGlobalAttributes(manifest.getMainAttributes());
    settings.getManifest();*/

    JarOutputStream jarOutputStream;//, manifest);

    final OutputStream fos = new BufferedOutputStream(new FileOutputStream(jarFile));
    if (manifest != null) {
      jarOutputStream = new JarOutputStream(fos);
      ZipUtil.addFileToZip(jarOutputStream, manifest, JarFile.MANIFEST_NAME, null, fileFilter);
    }
    else {
      jarOutputStream = new JarOutputStream(fos, new Manifest());
    }
    final THashSet<String> writtenItemRelativePaths = new THashSet<String>();
    boolean ok = jarLibraries(module, jarOutputStream, jarFile, fileFilter, writtenItemRelativePaths, true);
    ok &= ZipUtil.addDirToZipRecursively(jarOutputStream,
                                         jarFile,
                                         myDirectoryToJar,
                                         "",
                                         fileFilter,
                                         writtenItemRelativePaths);
    if (!ok) {
      final String message = J2MEBundle.message("compiler.packaging.fail");
      context.addMessage(CompilerMessageCategory.ERROR, message, null, -1, -1);
    }
    jarOutputStream.close();
    return jarFile;
  }

  private boolean jarLibraries(final Module module, final JarOutputStream jarOutputStream, final File jarFile, final FileFilter fileFilter,
                               final THashSet<String> writtenItemRelativePaths, boolean ok) throws IOException {
    final OrderEntry[] orderEntries = ModuleRootManager.getInstance(module).getOrderEntries();
    for (OrderEntry entry : orderEntries) {
      if (entry instanceof LibraryOrderEntry){
        final LibraryOrderEntry libraryEntry = (LibraryOrderEntry)entry;
        final String[] urls = libraryEntry.getUrls(OrderRootType.CLASSES);
        if (urls != null){
          for (String  url : urls) {
            @NonNls final String protocol = VirtualFileManager.extractProtocol(url);
            final String path = VfsUtil.urlToPath(url);
            if (protocol != null && protocol.equals("jar")) {
              final VirtualFile libraryFile = JarFileSystem.getInstance().findFileByPath(path);
              final File tempFile = FileUtil.createTempDirectory("temp", "temp");
              tempFile.deleteOnExit();
              ZipUtil.extract(VfsUtil.virtualToIoFile(libraryFile), tempFile, new FilenameFilter() {
                public boolean accept(File dir, String name) {
                  return !dir.getName().equals(META_INF);
                }
              });
              ok &= ZipUtil.addFileOrDirRecursively(jarOutputStream, jarFile, tempFile, "", fileFilter, writtenItemRelativePaths);
            } else {
              ok &= ZipUtil.addFileOrDirRecursively(jarOutputStream, jarFile, new File(path), "", fileFilter, writtenItemRelativePaths);
            }
          }
        }
      }
    }
    return ok;
  }

  public void processOutdatedItem(CompileContext context, String url, ValidityState state) {
  }


  private class MyProcessingItem implements ProcessingItem {
    private EmptyValidityState myValidityState;
    private final VirtualFile myOutputClasses;
    private final Module myModule;


    public MyProcessingItem(VirtualFile outputClasses,
                            Module module) {
      myOutputClasses = outputClasses;
      myModule = module;
      setValidityState();
    }

    @NotNull
    public VirtualFile getFile() {
      return myOutputClasses;
    }

    public VirtualFile getOutputClasses() {
      return myOutputClasses;
    }

    public Module getModule() {
      return myModule;
    }

    public EmptyValidityState getValidityState() {
      return myValidityState;
    }

    public void setValidityState() {
      myValidityState = new EmptyValidityState();
    }
  }
}
