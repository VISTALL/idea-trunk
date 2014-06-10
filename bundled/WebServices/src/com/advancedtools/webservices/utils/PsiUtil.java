package com.advancedtools.webservices.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiMember;
import org.jetbrains.annotations.Nullable;

/**
 * @author Konstantin Bulenkov
 */
public class PsiUtil {
  private PsiUtil() {}

  @Nullable
  public static Module findModule(PsiMember member) {
    try {
      final VirtualFile vf = member.getContainingFile().getVirtualFile();
      final Project project = member.getProject();
      if (vf == null) return null;
      return ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(vf);
    } catch (Exception e) {
      return null;
    }
  }
}
