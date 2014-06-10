package org.jetbrains.android.compiler.tools;

import com.android.sdklib.IAndroidTarget;
import com.android.sdklib.SdkConstants;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import org.jetbrains.android.util.ExecutionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AndroidApt decorator.
 *
 * @author Alexey Efimov
 */
public final class AndroidApt {
  private AndroidApt() {
  }

  @NotNull
  public static Map<CompilerMessageCategory, List<String>> compile(@NotNull IAndroidTarget target,
                                                                   @NotNull String rootDirPath,
                                                                   @NotNull String outDir,
                                                                   @NotNull String resourceDir,
                                                                   @Nullable String assertsDir) throws IOException {
    List<String> args = new ArrayList<String>();
    Collections.addAll(args, target.getPath(IAndroidTarget.AAPT), "package", "-m", "-J", outDir, "-M", buildManifestPath(rootDirPath), "-S",
                       resourceDir);
    if (assertsDir != null) {
      Collections.addAll(args, "-A", assertsDir);
    }
    Collections.addAll(args, "-I", target.getPath(IAndroidTarget.ANDROID_JAR));
    return ExecutionUtil.execute(args.toArray(new String[args.size()]));
  }

  @NotNull
  public static Map<CompilerMessageCategory, List<String>> packageResources(@NotNull IAndroidTarget target,
                                                                            @NotNull String rootDirPath,
                                                                            @Nullable String resourceDir,
                                                                            @Nullable String assetsDir,
                                                                            @NotNull String outputPath) throws IOException {
    List<String> args = new ArrayList<String>();
    Collections.addAll(args, target.getPath(IAndroidTarget.AAPT), "package", "-f",     // force overwrite of existing files
                       "-M", buildManifestPath(rootDirPath));
    if (resourceDir != null) {
      Collections.addAll(args, "-S", resourceDir);
    }
    if (assetsDir != null) {
      Collections.addAll(args, "-A", assetsDir);
    }
    Collections.addAll(args, "-I", target.getPath(IAndroidTarget.ANDROID_JAR), "-F", outputPath);
    return ExecutionUtil.execute(args.toArray(new String[args.size()]));
  }

  private static String buildManifestPath(String rootDirPath) {
    return rootDirPath + File.separator + SdkConstants.FN_ANDROID_MANIFEST_XML;
  }
}
