/*
 * User: anna
 * Date: 26-Nov-2007
 */
package com.intellij.j2meplugin.module;

import com.intellij.j2meplugin.module.settings.MobileModuleSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.listeners.RefactoringElementListenerComposite;
import com.intellij.refactoring.listeners.RefactoringElementListenerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.SortedSet;

public class J2MERefactoringListenerProvider implements RefactoringElementListenerProvider {
  private final Project myProject;

  public J2MERefactoringListenerProvider(Project project) {
    myProject = project;
  }

  @Nullable
  public RefactoringElementListener getListener(final PsiElement element) {
    if (!(element instanceof PsiClass)) return null;
    RefactoringElementListenerComposite listenerComposite = null;
    final Module[] modules = ModuleManager.getInstance(myProject).getModules();
    for (Module module : modules) {
      final MobileModuleSettings moduleSettings = MobileModuleSettings.getInstance(module);
      if (moduleSettings != null) {
        final SortedSet<String> midlets = moduleSettings.getMIDlets();
        for (final String midlet : midlets) {
          final String midletClassName = moduleSettings.getMIDletClassName(midlet);
          if (midletClassName != null) {
            final PsiClass psiClass =
              JavaPsiFacade.getInstance(myProject).findClass(midletClassName, GlobalSearchScope.projectScope(myProject));
            if (psiClass != null && element == psiClass) {
              if (listenerComposite == null) {
                listenerComposite = new RefactoringElementListenerComposite();
              }
              listenerComposite
                .addListener(new RefactoringElementListener() {
                  public void elementMoved(@NotNull final PsiElement newElement) {
                    elementRenamed(newElement);
                  }

                  public void elementRenamed(@NotNull final PsiElement newElement) {
                    if (!(newElement instanceof PsiClass)) return;
                    moduleSettings.setMIDletClassName(midlet, ((PsiClass)newElement).getQualifiedName());
                  }
                });
            }
          }
        }
      }
    }
    return listenerComposite;
  }


}
