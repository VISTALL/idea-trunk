package com.sixrr.xrp.context;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProjectContext implements Context {
    private final Project project;

    public ProjectContext(Project project) {
        super();
        this.project = project;
    }

    public Iterator<XmlFile> iterator() {
        final ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
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
