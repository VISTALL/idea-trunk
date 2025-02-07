package org.jetbrains.android.compiler;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.compiler.tools.AndroidIdl;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.fileTypes.AndroidIdlFileType;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Android IDL compiler.
 *
 * @author Alexey Efimov
 */
public class AndroidIdlCompiler implements SourceGeneratingCompiler {
  private static final GenerationItem[] EMPTY_GENERATION_ITEM_ARRAY = {};

  private final Project myProject;

  public AndroidIdlCompiler(Project project) {
    myProject = project;
  }

  public GenerationItem[] getGenerationItems(CompileContext context) {
    Module[] affectedModules = context.getCompileScope().getAffectedModules();
    if (affectedModules.length > 0) {
      Application application = ApplicationManager.getApplication();
      return application.runReadAction(new PrepareAction(context));
    }
    return EMPTY_GENERATION_ITEM_ARRAY;
  }

  public GenerationItem[] generate(CompileContext context, GenerationItem[] items, VirtualFile outputRootDirectory) {
    if (items != null && items.length > 0) {
      context.getProgressIndicator().setText("Generating AIDL files...");
      Application application = ApplicationManager.getApplication();
      GenerationItem[] generationItems = application.runReadAction(new GenerateAction(context, items));
      for (GenerationItem item : generationItems) {
        File generatedFile = ((IdlGenerationItem)item).myGeneratedFile;
        if (generatedFile != null) {
          CompilerUtil.refreshIOFile(generatedFile);
        }
      }
      return generationItems;
    }
    return EMPTY_GENERATION_ITEM_ARRAY;
  }

  @NotNull
  public String getDescription() {
    return FileUtil.getNameWithoutExtension(SdkConstants.FN_AIDL);
  }

  public boolean validateConfiguration(CompileScope scope) {
    return true;
  }

  @Nullable
  public ValidityState createValidityState(DataInput is) throws IOException {
    return null;
  }

  private final static class IdlGenerationItem implements GenerationItem {
    final Module myModule;
    final VirtualFile myFile;
    final boolean myTestSource;
    final IAndroidTarget myAndroidTarget;
    final File myGeneratedFile;
    final String myPackageName;

    public IdlGenerationItem(@NotNull Module module,
                             @NotNull VirtualFile file,
                             @NotNull String sourceRootPath,
                             boolean testSource,
                             @NotNull IAndroidTarget androidTarget,
                             @NotNull String packageName) {
      myModule = module;
      myFile = file;
      myTestSource = testSource;
      myAndroidTarget = androidTarget;
      myPackageName = packageName;
      myGeneratedFile =
        new File(sourceRootPath, packageName.replace('.', File.separatorChar) + File.separator + file.getNameWithoutExtension() + ".java");
    }

    @Nullable
    public String getPath() {
      return null;
    }

    @Nullable
    public ValidityState getValidityState() {
      return null;
    }

    public Module getModule() {
      return myModule;
    }

    public boolean isTestSource() {
      return myTestSource;
    }
  }

  private final class PrepareAction implements Computable<GenerationItem[]> {
    private final CompileContext myContext;

    public PrepareAction(CompileContext context) {
      myContext = context;
    }

    public GenerationItem[] compute() {
      ProjectFileIndex fileIndex = ProjectRootManager.getInstance(myProject).getFileIndex();
      CompileScope compileScope = myContext.getCompileScope();
      VirtualFile[] files = compileScope.getFiles(AndroidIdlFileType.ourFileType, false);
      List<GenerationItem> items = new ArrayList<GenerationItem>(files.length);
      for (VirtualFile file : files) {
        Module module = myContext.getModuleByFile(file);
        AndroidFacet facet = FacetManager.getInstance(module).getFacetByType(AndroidFacet.ID);
        if (facet != null) {
          IAndroidTarget target = facet.getConfiguration().getAndroidTarget();
          if (target != null) {
            String sourceRootPath = facet.getGenSourceRootPath();
            if (sourceRootPath != null) {
              String packageName = AndroidUtils.getPackageName(module, file);
              if (packageName != null) {
                IdlGenerationItem generationItem =
                  new IdlGenerationItem(module, file, sourceRootPath, fileIndex.isInTestSourceContent(file), target, packageName);
                if (myContext.isMake()) {
                  File generatedFile = generationItem.myGeneratedFile;
                  if (generatedFile == null || !generatedFile.exists() || generatedFile.lastModified() <= file.getModificationCount()) {
                    AndroidCompileUtil.createSourceRootIfNotExist(sourceRootPath, module);
                    AndroidCompileUtil
                      .removeDuplicatingClass(generationItem.myModule, generationItem.myPackageName, generationItem.myGeneratedFile);
                    items.add(generationItem);
                  }
                }
                else {
                  AndroidCompileUtil.createSourceRootIfNotExist(sourceRootPath, module);
                  items.add(generationItem);
                }
              }
            }
          }
        }
      }
      return items.toArray(new GenerationItem[items.size()]);
    }
  }

  private static final class GenerateAction implements Computable<GenerationItem[]> {
    private final CompileContext myContext;
    private final GenerationItem[] myItems;

    public GenerateAction(CompileContext context, GenerationItem[] items) {
      myContext = context;
      myItems = items;
    }

    public GenerationItem[] compute() {
      List<GenerationItem> results = new ArrayList<GenerationItem>(myItems.length);
      for (GenerationItem item : myItems) {
        if (item instanceof IdlGenerationItem) {
          IdlGenerationItem idlItem = (IdlGenerationItem)item;
          try {
            ModuleRootManager rootManager = ModuleRootManager.getInstance(idlItem.myModule);
            Map<CompilerMessageCategory, List<String>> messages = AndroidIdl
              .execute(idlItem.myAndroidTarget, idlItem.myFile.getPath(), idlItem.myGeneratedFile.getPath(), rootManager.getSourceRoots());
            addMessages(messages, idlItem.myFile.getUrl());
            if (messages.get(CompilerMessageCategory.ERROR).isEmpty()) {
              results.add(idlItem);
            }
          }
          catch (IOException e) {
            myContext.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), idlItem.myFile.getUrl(), -1, -1);
          }
        }
      }
      return results.toArray(new GenerationItem[results.size()]);
    }

    private void addMessages(Map<CompilerMessageCategory, List<String>> messages, String url) {
      for (CompilerMessageCategory category : messages.keySet()) {
        List<String> messageList = messages.get(category);
        for (String message : messageList) {
          myContext.addMessage(category, message, url, -1, -1);
        }
      }
    }
  }
}