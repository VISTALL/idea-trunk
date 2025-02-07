package org.jetbrains.plugins.groovy.gant;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.refactoring.psi.SearchUtils;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NonNls;

import java.util.Set;

/**
 * @author ilyas
 */
public class AntTasksProvider {

  @NonNls public static final String ANT_TASK_CLASS = "org.apache.tools.ant.Task";

  private final Project myProject;
  private final CachedValue<Set<String>> myCachedValue;

  public static AntTasksProvider getInstance(Project project) {
    return ServiceManager.getService(project, AntTasksProvider.class);
  }

  public AntTasksProvider(Project project) {
    myProject = project;
    final CachedValuesManager manager = PsiManager.getInstance(myProject).getCachedValuesManager();
    myCachedValue = manager.createCachedValue(new CachedValueProvider<Set<String>>() {
      public Result<Set<String>> compute() {
        final Set<String> set = findAntTasks(myProject);
        return Result.create(set, ProjectRootManager.getInstance(myProject), PsiModificationTracker.JAVA_STRUCTURE_MODIFICATION_COUNT);
      }
    }, false);
  }
  
  public Set<String> getAntTasks() {
    return myCachedValue.getValue();
  }

  private static HashSet<String> findAntTasks(Project project) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    final PsiClass taskClass = facade.findClass(ANT_TASK_CLASS, GlobalSearchScope.allScope(project));

    if (taskClass != null) {
      final Iterable<PsiClass> inheritors = SearchUtils.findClassInheritors(taskClass, true);
      final HashSet<String> classNames = new HashSet<String>();
      for (PsiClass inheritor : inheritors) {
        if (!inheritor.hasModifierProperty(PsiModifier.ABSTRACT) && !inheritor.hasModifierProperty(PsiModifier.PRIVATE)) {
          classNames.add(inheritor.getName());
        }
      }
      return classNames;
    }

    return new HashSet<String>(0);
  }
}
