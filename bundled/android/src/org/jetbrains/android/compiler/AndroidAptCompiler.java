package org.jetbrains.android.compiler;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.intellij.compiler.impl.CompilerUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.compiler.tools.AndroidApt;
import org.jetbrains.android.dom.manifest.Manifest;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidRootUtil;
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
 * Apt compiler.
 *
 * @author Alexey Efimov
 */
public class AndroidAptCompiler implements SourceGeneratingCompiler {
  private static final GenerationItem[] EMPTY_GENERATION_ITEM_ARRAY = {};

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
      context.getProgressIndicator().setText("Generating " + AndroidUtils.R_JAVA_FILENAME + "...");
      GenerationItem[] generationItems = doGenerate(context, items);
      for (GenerationItem item : generationItems) {
        File generatedFile = ((AptGenerationItem)item).myGeneratedFile;
        if (generatedFile != null) {
          CompilerUtil.refreshIOFile(generatedFile);
        }
      }
      return generationItems;
    }
    return EMPTY_GENERATION_ITEM_ARRAY;
  }

  private static GenerationItem[] doGenerate(CompileContext context, GenerationItem[] items) {
    List<GenerationItem> results = new ArrayList<GenerationItem>(items.length);
    for (GenerationItem item : items) {
      if (item instanceof AptGenerationItem) {
        AptGenerationItem aptItem = (AptGenerationItem)item;
        try {
          Map<CompilerMessageCategory, List<String>> messages = AndroidApt
            .compile(aptItem.myAndroidTarget, aptItem.myRootPath, aptItem.mySourceRootPath, aptItem.myResourcesPath, aptItem.myAssetsPath);
          AndroidCompileUtil.addMessages(context, messages);
          if (messages.get(CompilerMessageCategory.ERROR).isEmpty()) {
            results.add(aptItem);
          }
        }
        catch (IOException e) {
          context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
        }
      }
    }
    return results.toArray(new GenerationItem[results.size()]);
  }

  @NotNull
  public String getDescription() {
    return FileUtil.getNameWithoutExtension(SdkConstants.FN_AAPT);
  }

  public boolean validateConfiguration(CompileScope scope) {
    return true;
  }

  public ValidityState createValidityState(DataInput is) throws IOException {
    return new ResourcesValidityState(is);
  }

  private final static class AptGenerationItem implements GenerationItem {
    final Module myModule;
    final String myRootPath;
    final String myResourcesPath;
    final String myAssetsPath;
    final String mySourceRootPath;
    final IAndroidTarget myAndroidTarget;
    final File myGeneratedFile;
    final String myPackage;

    private AptGenerationItem(@NotNull Module module,
                              @NotNull String rootPath,
                              @NotNull String resourcesPath,
                              @Nullable String assetsPath,
                              @NotNull String sourceRootPath,
                              @NotNull IAndroidTarget target,
                              @NotNull String packageValue) {
      myModule = module;
      myRootPath = rootPath;
      myResourcesPath = resourcesPath;
      myAssetsPath = assetsPath;
      mySourceRootPath = sourceRootPath;
      myAndroidTarget = target;
      myPackage = packageValue;
      myGeneratedFile =
        new File(sourceRootPath, packageValue.replace('.', File.separatorChar) + File.separator + AndroidUtils.R_JAVA_FILENAME);
    }

    public String getPath() {
      return AndroidUtils.R_JAVA_FILENAME;
    }

    public ValidityState getValidityState() {
      return new ResourcesValidityState(myModule, false);
    }

    public Module getModule() {
      return myModule;
    }

    public boolean isTestSource() {
      return false;
    }
  }

  private static final class PrepareAction implements Computable<GenerationItem[]> {
    private final CompileContext myContext;

    public PrepareAction(CompileContext context) {
      myContext = context;
    }

    public GenerationItem[] compute() {
      CompileScope compileScope = myContext.getCompileScope();
      Module[] modules = compileScope.getAffectedModules();
      List<GenerationItem> items = new ArrayList<GenerationItem>();
      for (Module module : modules) {
        AndroidFacet facet = AndroidFacet.getInstance(module);
        if (facet != null) {
          Manifest manifest = facet.getManifest();
          VirtualFile resourcesDir = AndroidRootUtil.getResourceDir(module);
          VirtualFile assetsDir = AndroidRootUtil.getAssetsDir(module);
          if (manifest != null && resourcesDir != null) {
            String packageName = manifest.getPackage().getValue();
            if (packageName != null && packageName.length() > 0) {
              VirtualFile parent = resourcesDir.getParent();
              if (parent != null) {
                String sourceRootPath = facet.getGenSourceRootPath();
                if (sourceRootPath != null) {
                  IAndroidTarget target = facet.getConfiguration().getAndroidTarget();
                  if (target != null) {
                    AndroidCompileUtil.createSourceRootIfNotExist(sourceRootPath, module);
                    String assetsDirPath = assetsDir != null ? assetsDir.getPath() : null;
                    AptGenerationItem item =
                      new AptGenerationItem(module, parent.getPath(), resourcesDir.getPath(), assetsDirPath, sourceRootPath, target,
                                            packageName);
                    AndroidCompileUtil.removeDuplicatingClass(item.myModule, item.myPackage, item.myGeneratedFile);
                    items.add(item);
                  }
                }
              }
            }
          }
        }
      }
      return items.toArray(new GenerationItem[items.size()]);
    }
  }
}
