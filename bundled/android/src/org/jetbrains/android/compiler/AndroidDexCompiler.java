package org.jetbrains.android.compiler;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.JarFileSystem;
import org.jetbrains.android.compiler.tools.AndroidDx;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidFacetConfiguration;
import org.jetbrains.android.sdk.AndroidPlatform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.IOException;
import java.util.*;

/**
 * Android Dex compiler.
 *
 * @author Alexey Efimov
 */
public class AndroidDexCompiler implements ClassPostProcessingCompiler {

  /*private static void saveDocuments() {
    final Application application = ApplicationManager.getApplication();
    Runnable runnable = new Runnable() {
      public void run() {
        application.saveAll();
      }
    };
    if (application.isDispatchThread()) {
      runnable.run();
    }
    else {
      application.invokeAndWait(runnable, ModalityState.defaultModalityState());
    }
  }*/

  private static void fillExternalLibrariesAndModules(final Module module, final Set<VirtualFile> result, final Library platformLibrary) {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      public void run() {
        ModuleRootManager manager = ModuleRootManager.getInstance(module);
        for (OrderEntry entry : manager.getOrderEntries()) {
          if (entry instanceof LibraryOrderEntry) {
            Library library = ((LibraryOrderEntry)entry).getLibrary();
            if (!platformLibrary.equals(library)) {
              if (library != null) {
                for (VirtualFile file : library.getFiles(OrderRootType.CLASSES)) {
                  if (file.exists()) {
                    if (file.getFileSystem() instanceof JarFileSystem) {
                      VirtualFile localFile = JarFileSystem.getInstance().getVirtualFileForJar(file);
                      if (localFile != null) result.add(localFile);
                    }
                    else {
                      result.add(file);
                    }
                  }
                }
              }
            }
          }
          else if (entry instanceof ModuleOrderEntry) {
            Module module = ((ModuleOrderEntry)entry).getModule();
            CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
            if (extension != null) {
              VirtualFile classDir = extension.getCompilerOutputPath();
              boolean added = false;
              if (!result.contains(classDir) && classDir != null && classDir.exists()) {
                result.add(classDir);
                added = true;
              }
              VirtualFile classDirForTests = extension.getCompilerOutputPathForTests();
              if (!result.contains(classDirForTests) && classDirForTests != null && classDirForTests.exists()) {
                result.add(classDirForTests);
                added = true;
              }
              if (added) {
                fillExternalLibrariesAndModules(module, result, platformLibrary);
              }
            }
          }
        }
      }
    });
  }

  private static String[] getExternalLibrariesAndModules(Module module, VirtualFile moduleOutputDir, Library platformLibrary) {
    Set<VirtualFile> files = new HashSet<VirtualFile>();
    fillExternalLibrariesAndModules(module, files, platformLibrary);
    files.remove(moduleOutputDir);
    String[] result = new String[files.size()];
    int i = 0;
    for (VirtualFile file : files) {
      result[i++] = FileUtil.toSystemDependentName(file.getPath());
    }
    return result;
  }

  @NotNull
  public ProcessingItem[] getProcessingItems(CompileContext context) {
    Module[] affectedModules = context.getCompileScope().getAffectedModules();
    if (affectedModules.length > 0) {
      Application application = ApplicationManager.getApplication();
      //saveDocuments();
      return application.runReadAction(new PrepareAction(context));
    }
    return ProcessingItem.EMPTY_ARRAY;
  }

  public ProcessingItem[] process(CompileContext context, ProcessingItem[] items) {
    if (items != null && items.length > 0) {
      Application application = ApplicationManager.getApplication();
      return application.runReadAction(new ProcessAction(context, items));
    }
    return ProcessingItem.EMPTY_ARRAY;
  }

  @NotNull
  public String getDescription() {
    return FileUtil.getNameWithoutExtension(SdkConstants.FN_DX);
  }

  public boolean validateConfiguration(CompileScope scope) {
    return true;
  }

  public ValidityState createValidityState(DataInput is) throws IOException {
    return new EmptyValidityState();
  }

  private static final class PrepareAction implements Computable<ProcessingItem[]> {
    private final CompileContext myContext;

    public PrepareAction(CompileContext context) {
      myContext = context;
    }

    public ProcessingItem[] compute() {
      CompileScope compileScope = myContext.getCompileScope();
      Module[] modules = compileScope.getAffectedModules();
      List<ProcessingItem> items = new ArrayList<ProcessingItem>();
      for (Module module : modules) {
        AndroidFacet facet = FacetManager.getInstance(module).getFacetByType(AndroidFacet.ID);
        if (facet != null) {
          CompilerModuleExtension extension = CompilerModuleExtension.getInstance(module);
          VirtualFile outputDir = extension.getCompilerOutputPath();
          if (outputDir != null) {
            AndroidFacetConfiguration configuration = facet.getConfiguration();
            AndroidPlatform platform = configuration.getAndroidPlatform();
            if (platform != null) {
              String[] dependencies = getExternalLibrariesAndModules(module, outputDir, platform.getLibrary());
              List<String> additionalTargets = new ArrayList<String>();
              Collections.addAll(additionalTargets, dependencies);
              VirtualFile outputDirForTests = extension.getCompilerOutputPathForTests();
              if (outputDirForTests != null) {
                additionalTargets.add(FileUtil.toSystemDependentName(outputDirForTests.getPath()));
              }
              IAndroidTarget target = configuration.getAndroidTarget();
              if (target != null) {
                items.add(new DexItem(module, outputDir, target, additionalTargets.toArray(new String[additionalTargets.size()])));
              }
            }
          }
        }
      }
      return items.toArray(new ProcessingItem[items.size()]);
    }
  }

  private final static class ProcessAction implements Computable<ProcessingItem[]> {
    private final CompileContext myContext;
    private final ProcessingItem[] myItems;

    public ProcessAction(CompileContext context, ProcessingItem[] items) {
      myContext = context;
      myItems = items;
    }

    public ProcessingItem[] compute() {
      List<ProcessingItem> results = new ArrayList<ProcessingItem>(myItems.length);
      for (ProcessingItem item : myItems) {
        if (item instanceof DexItem) {
          DexItem dexItem = (DexItem)item;
          try {
            Map<CompilerMessageCategory, List<String>> messages =
              AndroidDx.dex(dexItem.myAndroidTarget, FileUtil.toSystemDependentName(dexItem.myClassDir.getPath()), dexItem.myLibraries);
            addMessages(messages);
            if (messages.get(CompilerMessageCategory.ERROR).isEmpty()) {
              results.add(dexItem);
            }
          }
          catch (IOException e) {
            myContext.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
          }
        }
      }
      return results.toArray(new ProcessingItem[results.size()]);
    }

    private void addMessages(Map<CompilerMessageCategory, List<String>> messages) {
      for (CompilerMessageCategory category : messages.keySet()) {
        List<String> messageList = messages.get(category);
        for (String message : messageList) {
          myContext.addMessage(category, message, null, -1, -1);
        }
      }
    }
  }

  private final static class DexItem implements ProcessingItem {
    final Module myModule;
    final VirtualFile myClassDir;
    final IAndroidTarget myAndroidTarget;
    final String[] myLibraries;

    public DexItem(@NotNull Module module, @NotNull VirtualFile classDir, @NotNull IAndroidTarget target, String[] libraries) {
      myModule = module;
      myClassDir = classDir;
      myAndroidTarget = target;
      myLibraries = libraries;
    }

    @NotNull
    public VirtualFile getFile() {
      return myModule.getModuleFile();
    }

    @Nullable
    public ValidityState getValidityState() {
      return new EmptyValidityState();
    }
  }
}
