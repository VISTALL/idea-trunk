package com.sixrr.xrp.psi;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

public class PsiUtils {
    private PsiUtils() {
        super();
    }

    public static Module getModuleForFile(PsiFile file) {
        final VirtualFile virtualFile = file.getVirtualFile();
        final Project project = file.getProject();
        final ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        final ProjectFileIndex fileIndex = rootManager.getFileIndex();
        return fileIndex.getModuleForFile(virtualFile);
    }
}
