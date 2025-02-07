package org.jetbrains.idea.maven.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.utils.MavenConstants;

import java.util.List;

/**
 * @author Vladislav.Kaznacheev
 */
public class FileFinder {
  public static List<VirtualFile> findPomFiles(VirtualFile[] roots,
                                               boolean lookForNested,
                                               ProgressIndicator indicator,
                                               List<VirtualFile> result) {
    for (VirtualFile f : roots) {
      if (indicator.isCanceled()) break;
      indicator.setText2(f.getPath());

      if (f.isDirectory()) {
        if (lookForNested) {
          findPomFiles(f.getChildren(), lookForNested, indicator, result);
        }
      }
      else {
        if (f.getName().equalsIgnoreCase(MavenConstants.POM_XML)) {
          result.add(f);
        }
      }
    }

    return result;
  }

  public static VirtualFile refreshRecursively(final String path) {
    return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
      @SuppressWarnings({"ConstantConditions"})
      public VirtualFile compute() {
        final VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByPath(path);
        if (dir != null) {
          dir.refresh(false, true);
        }
        return dir;
      }
    });
  }
}
