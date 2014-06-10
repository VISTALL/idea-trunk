package com.sixrr.xrp.context;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ModuleContext implements Context {
    private final Module module;

    public ModuleContext(Module module) {
        super();
        this.module = module;
    }

    public Iterator<XmlFile> iterator() {
        final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
        final Project project = module.getProject();
        final PsiManager psiManager = PsiManager.getInstance(project);
        final VirtualFile[] directories = rootManager.getContentRoots();
        final List<Iterator<XmlFile>> enumerations =
                new ArrayList<Iterator<XmlFile>>();
        for (final VirtualFile directory : directories) {
            final PsiDirectory psiDirectory = psiManager.findDirectory(directory);
            if (psiDirectory != null) {
                enumerations.add(new FileTreeIterator(psiDirectory));
            } else {
            }
        }
        return new MultiIterator<XmlFile>(enumerations);
    }
}
