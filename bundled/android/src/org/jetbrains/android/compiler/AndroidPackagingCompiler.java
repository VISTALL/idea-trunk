package org.jetbrains.android.compiler;

import com.android.sdklib.IAndroidTarget;
import com.intellij.openapi.compiler.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.android.compiler.tools.AndroidApkBuilder;
import org.jetbrains.android.compiler.tools.AndroidApt;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.AndroidFacetConfiguration;
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
 * @author yole
 */
public class AndroidPackagingCompiler implements PackagingCompiler {

  public void processOutdatedItem(CompileContext context, String url, @Nullable ValidityState state) {
  }

  @NotNull
  public ProcessingItem[] getProcessingItems(CompileContext context) {
    final List<ProcessingItem> items = new ArrayList<ProcessingItem>();
    Module[] affectedModules = context.getCompileScope().getAffectedModules();
    for (Module module : affectedModules) {
      AndroidFacet facet = AndroidFacet.getInstance(module);
      if (facet != null) {
        VirtualFile manifestFile = AndroidRootUtil.getManifestFile(module);
        VirtualFile resourcesDir = AndroidRootUtil.getResourceDir(module);
        VirtualFile assetsDir = AndroidRootUtil.getAssetsDir(module);
        if (manifestFile != null) {
          AndroidFacetConfiguration configuration = facet.getConfiguration();
          VirtualFile outputDir = context.getModuleOutputDirectory(module);
          if (outputDir != null) {
            String tempOutputPath = new File(outputDir.getPath(), module.getName() + ".apk.tmp").getPath();
            String outputPath = new File(outputDir.getPath(), module.getName() + ".apk").getPath();
            String classesDexPath = new File(outputDir.getPath(), AndroidUtils.CLASSES_FILE_NAME).getPath();
            IAndroidTarget target = configuration.getAndroidTarget();
            String sdkPath = configuration.getSdkPath();
            if (target != null && sdkPath != null) {
              String assetsDirPath = assetsDir != null ? assetsDir.getPath() : null;
              String resourcesDirPath = resourcesDir != null ? resourcesDir.getPath() : null;
              items.add(new AptPackagingItem(module, manifestFile, target, sdkPath, resourcesDirPath, assetsDirPath, tempOutputPath,
                                             outputPath, classesDexPath));
            }
          }
        }
      }
    }
    return items.toArray(new ProcessingItem[items.size()]);
  }

  public ProcessingItem[] process(CompileContext context, ProcessingItem[] items) {
    context.getProgressIndicator().setText("Building Android package...");
    final List<ProcessingItem> result = new ArrayList<ProcessingItem>();
    for (ProcessingItem processingItem : items) {
      AptPackagingItem item = (AptPackagingItem)processingItem;
      VirtualFile parent = item.getFile().getParent();
      if (parent != null) {
        String rootDir = parent.getPath();
        try {
          Map<CompilerMessageCategory, List<String>> messages =
            AndroidApt.packageResources(item.myAndroidTarget, rootDir, item.myResourcesPath, item.myAssetsPath, item.myOutputPath);
          AndroidCompileUtil.addMessages(context, messages);
          if (messages.get(CompilerMessageCategory.ERROR).isEmpty()) {
            final Map<CompilerMessageCategory, List<String>> apkuBuilderMessages =
              AndroidApkBuilder.execute(item.mySdkPath, item.myOutputPath, item.myClassesDexPath, item.myFinalPath);
            AndroidCompileUtil.addMessages(context, apkuBuilderMessages);
          }
        }
        catch (IOException e) {
          context.addMessage(CompilerMessageCategory.ERROR, e.getMessage(), null, -1, -1);
        }
        if (context.getMessages(CompilerMessageCategory.ERROR).length == 0) {
          result.add(item);
        }
      }
    }
    return result.toArray(new ProcessingItem[result.size()]);
  }

  @NotNull
  public String getDescription() {
    return "Android Packaging Compiler";
  }

  public boolean validateConfiguration(CompileScope scope) {
    return true;
  }

  public ValidityState createValidityState(DataInput is) throws IOException {
    return new ResourcesValidityState(is);
  }

  private static class AptPackagingItem implements ProcessingItem {
    final Module myModule;
    final VirtualFile myFile;
    final String mySdkPath;
    final IAndroidTarget myAndroidTarget;
    final String myResourcesPath;
    final String myAssetsPath;
    final String myOutputPath;
    final String myFinalPath;
    final String myClassesDexPath;

    private AptPackagingItem(@NotNull Module module,
                             @NotNull VirtualFile file,
                             @NotNull IAndroidTarget androidTarget,
                             @NotNull String sdkPath,
                             @Nullable String resourcesPath,
                             @Nullable String assetsPath,
                             @NotNull String outputPath,
                             @NotNull String finalPath,
                             @NotNull String classesDexPath) {
      myModule = module;
      myFile = file;
      myAndroidTarget = androidTarget;
      mySdkPath = sdkPath;
      myResourcesPath = resourcesPath;
      myAssetsPath = assetsPath;
      myOutputPath = outputPath;
      myFinalPath = finalPath;
      myClassesDexPath = classesDexPath;
    }

    @NotNull
    public VirtualFile getFile() {
      return myFile;
    }

    @Nullable
    public ValidityState getValidityState() {
      return new ResourcesValidityState(myModule, true);
    }
  }
}
