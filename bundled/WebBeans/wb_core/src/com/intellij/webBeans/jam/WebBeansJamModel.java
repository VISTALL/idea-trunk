package com.intellij.webBeans.jam;

import com.intellij.jam.JamService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.jam.events.WebBeansFires;
import com.intellij.webBeans.jam.events.WebBeansObserves;
import com.intellij.webBeans.jam.specialization.WebBeansSpecializes;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WebBeansJamModel {
  private final Module myModule;


  public static WebBeansJamModel getModel(@NotNull Module module) {
    return ModuleServiceManager.getService(module, WebBeansJamModel.class);
  }

  public WebBeansJamModel(@NotNull final Module module) {
    myModule = module;
  }

  public List<NamedWebBean> getNamedWebBeans() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    List<NamedWebBean> webBeans = new ArrayList<NamedWebBean>();

    webBeans.addAll(service.getJamClassElements(WebBeansSemContributor.NAMED_JAM_KEY, WebBeansAnnoConstants.NAMED_ANNOTATION, scope));
    webBeans.addAll(service.getJamMethodElements(WebBeansSemContributor.NAMED_JAM_KEY, WebBeansAnnoConstants.NAMED_ANNOTATION, scope));

    Collection<PsiClass> stereotypeAnnotationClasses =
      WebBeansCommonUtils.getStereotypeAnnotationClasses(myModule, WebBeansAnnoConstants.NAMED_ANNOTATION);

    for (String annoFQN : WebBeansCommonUtils.getQualifiedNames(stereotypeAnnotationClasses)) {
      webBeans.addAll(service.getJamClassElements(WebBeansSemContributor.NAMED_JAM_KEY, annoFQN, scope));
      webBeans.addAll(service.getJamMethodElements(WebBeansSemContributor.NAMED_JAM_KEY, annoFQN, scope));
      webBeans.addAll(service.getJamFieldElements(WebBeansSemContributor.NAMED_JAM_KEY, annoFQN, scope));
    }

    return webBeans;
  }

  public List<WebBeansObserves> getWebBeansObserves() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamParameterElements(WebBeansObserves.META.getJamKey(), WebBeansAnnoConstants.OBSERVES_ANNOTATION, scope);
  }

  public List<WebBeansFires> getWebBeansFires() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    List<WebBeansFires> fires = new ArrayList<WebBeansFires>();

    fires.addAll(service.getJamFieldElements(WebBeansFires.FIELD_META, WebBeansAnnoConstants.FIRES_ANNOTATION, scope));
    fires.addAll(service.getJamMethodElements(WebBeansFires.METHOD_META, WebBeansAnnoConstants.FIRES_ANNOTATION, scope));

    return fires;
  }

  public List<WebBeansSpecializes> getWebBeansSpecializeses() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    List<WebBeansSpecializes> specializeses = new ArrayList<WebBeansSpecializes>();

    specializeses.addAll(service.getJamClassElements(WebBeansSpecializes.CLASS_META, WebBeansAnnoConstants.SPECIALIZES_ANNOTATION, scope));
    specializeses.addAll(service.getJamMethodElements(WebBeansSpecializes.METHOD_META, WebBeansAnnoConstants.SPECIALIZES_ANNOTATION, scope));

    return specializeses;
  }

  public List<WebBeansSpecializes> getWebBeansSpecializeses(final PsiClass psiClass) {
    final JamService service = JamService.getJamService(myModule.getProject());
    
    return service.getAnnotatedMembersList(psiClass, WebBeansSpecializes.SEM_KEY, true, true, false, false);
  }
}
