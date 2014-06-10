package org.jetbrains.android.compiler.tools;

import com.android.sdklib.IAndroidTarget;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.android.util.ExecutionUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Dx tool.
 *
 * @author Alexey Efimov
 */
public final class AndroidDx {
  private AndroidDx() {
  }

  @NotNull
  public static Map<CompilerMessageCategory, List<String>> dex(@NotNull IAndroidTarget target,
                                                               @NotNull String classDir,
                                                               String... additionalCompileTargets)
    throws IOException {
    List<String> argList = new ArrayList<String>();
    Collections.addAll(argList, target.getPath(IAndroidTarget.DX), SystemInfo.isWindows ? "" : "-JXmx384M", "--dex",
                       "--output=" + classDir + File.separatorChar + "classes.dex",
//                "--locals=full",
"--positions=lines", classDir);
    Collections.addAll(argList, additionalCompileTargets);
    return ExecutionUtil.execute(argList.toArray(new String[argList.size()]));
  }

}