package com.intellij.spring.facet;

import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class SpringConfigsSearcher {

  private final MultiMap<Module, PsiFile> myFiles = new MultiMap<Module, PsiFile>();
  private final MultiMap<VirtualFile, PsiFile> myJars = new MultiMap<VirtualFile, PsiFile>();
  private final List<VirtualFile> myVirtualFiles = new ArrayList<VirtualFile>();
  private final @NotNull Module myModule;

  public SpringConfigsSearcher(FacetEditorContext context) {
    myModule = context.getModule();
  }

  public SpringConfigsSearcher(@NotNull Module module) {
    myModule = module;
  }

  public void search() {
    myFiles.clear();
    myJars.clear();

    List<DomFileElement<Beans>> elements = DomService.getInstance()
      .getFileElements(Beans.class, myModule.getProject(), GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule));
    for (DomFileElement<Beans> element : elements) {
      XmlFile file = element.getFile();
      VirtualFile jar = JarFileSystem.getInstance().getVirtualFileForJar(file.getVirtualFile());
      if (jar != null) {
        myJars.putValue(jar, file);
      } else {
        Module module = ModuleUtil.findModuleForPsiElement(file);
        if (module != null) {
          myFiles.putValue(module, file);
        }
      }
    }
  }

  public MultiMap<Module, PsiFile> getFilesByModules() {
    return myFiles;
  }

  public MultiMap<VirtualFile, PsiFile> getJars() {
    return myJars;
  }

  public List<VirtualFile> getVirtualFiles() {
    return myVirtualFiles;
  }
}
