package com.sixrr.ejbmetrics;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

public class ModuleUtil {

    public static Module calculateModuleForClass(PsiClass aClass) {
        final PsiJavaFile file = PsiTreeUtil.getParentOfType(aClass, PsiJavaFile.class);
        if (file == null) {
            return null;
        }
        return calculateModuleForFile(file);
    }

    public static Module calculateModuleForFile(PsiFile file) {
        final VirtualFile virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return null;
        }
        final PsiManager manager = file.getManager();
        final Project project = manager.getProject();
        final ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        final ProjectFileIndex fileIndex = rootManager.getFileIndex();
        return fileIndex.getModuleForFile(virtualFile);
    }
}
