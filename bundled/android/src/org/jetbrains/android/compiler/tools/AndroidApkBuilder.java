package org.jetbrains.android.compiler.tools;

import com.android.sdklib.SdkConstants;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.android.util.ExecutionUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author yole
 */
public class AndroidApkBuilder {
  private AndroidApkBuilder() {
  }

  public static Map<CompilerMessageCategory, List<String>> execute(@NotNull String sdkPath,
                                                                   @NotNull String apkPath,
                                                                   @NotNull String dexPath,
                                                                   @NotNull String outputPath) throws IOException {
    String apkBuilderPath = sdkPath + File.separator + SdkConstants.OS_SDK_TOOLS_FOLDER + AndroidUtils.APK_BUILDER;
    final Map<CompilerMessageCategory, List<String>> messages =
      ExecutionUtil.execute(apkBuilderPath, outputPath, "-z", apkPath, "-f", dexPath);
    return filterUsingKeystoreMessages(messages);
  }

  private static Map<CompilerMessageCategory, List<String>> filterUsingKeystoreMessages(Map<CompilerMessageCategory, List<String>> messages) {
    List<String> infoMessages = messages.get(CompilerMessageCategory.INFORMATION);
    if (infoMessages == null) {
      infoMessages = new ArrayList<String>();
      messages.put(CompilerMessageCategory.INFORMATION, infoMessages);
    }
    final List<String> errors = messages.get(CompilerMessageCategory.ERROR);
    for (Iterator<String> iterator = errors.iterator(); iterator.hasNext();) {
      String s = iterator.next();
      if (s.startsWith("Using keystore:")) {
        // not actually an error
        infoMessages.add(s);
        iterator.remove();
      }
    }
    return messages;
  }
}
