package com.intellij.webBeans.manager;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.beans.WebBeanDescriptor;
import com.intellij.webBeans.beans.ProducerBeanDescriptor;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeansManager {
  private final Module myModule;
  private final WebBeansFactory myFactory;

  public WebBeansFactory getFactory() {
    return myFactory;
  }

  public WebBeansManager(@NotNull Module module) {
    myModule = module;
    myFactory = new WebBeansFactory(module);
  }

  @NotNull
  public static WebBeansManager getService(@NotNull Module module) {
    synchronized (module) {
      return ModuleServiceManager.getService(module, WebBeansManager.class);
    }
  }

  @NotNull
  public Set<WebBeanDescriptor> resolveWebBeanByType(@Nullable final PsiClass psiClass, final PsiClass... bindings) {
    Set<WebBeanDescriptor> descriptors = new HashSet<WebBeanDescriptor>();
    if (psiClass != null) {
      final Collection<PsiClass> allBindingTypesClasses = WebBeansCommonUtils.getBindingTypesClasses(getModule());

      descriptors.addAll(resolvePsiClassWebBeans(psiClass, allBindingTypesClasses, bindings));
      descriptors.addAll(resolveProducesWebBeans(psiClass, allBindingTypesClasses, bindings));
    }
    return descriptors;
  }

  private Set<WebBeanDescriptor> resolveProducesWebBeans(@NotNull PsiClass psiClass,
                                                         final Collection<PsiClass> allBindingTypesClasses,
                                                         final PsiClass... bindings) {
    final Set<PsiMember> candidates = new HashSet<PsiMember>();
    Processor<PsiMember> processor = new Processor<PsiMember>() {
      public boolean process(PsiMember candidate) {
        if (hasAnnotated(candidate, bindings, allBindingTypesClasses)) {
          candidates.add(candidate);
        }
        return true;
      }
    };

    PsiClass producesAnnoClass = JavaPsiFacade.getInstance(psiClass.getProject())
      .findClass(WebBeansAnnoConstants.PRODUCES_ANNOTATION, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(getModule()));

    if (producesAnnoClass != null) {
      Query<PsiMember> query = AnnotatedMembersSearch.search(producesAnnoClass, GlobalSearchScope.moduleScope(getModule()));

      query.forEach(processor);
    }

    return new HashSet<WebBeanDescriptor>(ContainerUtil.mapNotNull(candidates, new Function<PsiMember, WebBeanDescriptor>() {
      public WebBeanDescriptor fun(PsiMember member) {
        return createProducerWebBeanDescriptor(member);
      }
    }));
  }

  @Nullable
  public ProducerBeanDescriptor createProducerWebBeanDescriptor(@NotNull PsiMember member) {
    return myFactory.createProducerWebBeanDescriptor(member);
  }

  private Set<WebBeanDescriptor> resolvePsiClassWebBeans(@NotNull PsiClass psiClass,
                                                         final Collection<PsiClass> allBindingTypesClasses,
                                                         final PsiClass... bindings) {
    final Set<PsiClass> candidates = new HashSet<PsiClass>();
    Processor<PsiClass> processor = new Processor<PsiClass>() {
      public boolean process(PsiClass candidate) {
        if (hasAnnotated(candidate, bindings, allBindingTypesClasses)) {
          candidates.add(candidate);
        }
        return true;
      }
    };
    processor.process(psiClass);

    Query<PsiClass> classQuery = ClassInheritorsSearch.search(psiClass, GlobalSearchScope.moduleWithLibrariesScope(getModule()), true);
    classQuery.forEach(processor);

    if (bindings.length == 0) {
      candidates.add(psiClass);
    }

    return new HashSet<WebBeanDescriptor>(ContainerUtil.mapNotNull(candidates, new Function<PsiClass, WebBeanDescriptor>() {
      public WebBeanDescriptor fun(PsiClass psiClass) {
        return myFactory.createWebBeanDescriptor(psiClass);
      }
    }));
  }

  private static boolean hasAnnotated(@Nullable PsiModifierListOwner candidate,
                                      PsiClass[] bindings,
                                      Collection<PsiClass> allBindingTypesClasses) {
    if (candidate == null) return false;
    if (bindings.length == 0 || (bindings.length == 1 && WebBeansAnnoConstants.CURRENT_ANNOTATION.equals(bindings[0].getQualifiedName()))) {
      return hasImplicitCurrentAnnotation(candidate, allBindingTypesClasses);
    }
    else {
      Collection<PsiClass> candidateBinfingTypes = WebBeansCommonUtils.getBindingTypesClasses(candidate);
      if (bindings.length != candidateBinfingTypes.size()) return false;

      for (PsiClass binding : bindings) {
        if (!AnnotationUtil.isAnnotated(candidate, binding.getQualifiedName(), true)) return false;
      }
    }
    return true;
  }

  private static boolean hasImplicitCurrentAnnotation(PsiModifierListOwner candidate, Collection<PsiClass> allBindingTypesClasses) {
    for (PsiClass binding : allBindingTypesClasses) {
      if (!WebBeansAnnoConstants.CURRENT_ANNOTATION.equals(binding.getQualifiedName()) &&
          AnnotationUtil.isAnnotated(candidate, binding.getQualifiedName(), true)) {
        return false;
      }
    }
    return true;
  }


  @Nullable
  public Set<WebBeanDescriptor> resolveWebBeanByName(final String beanName) {
    return null;
  }

  public Module getModule() {
    return myModule;
  }
}
