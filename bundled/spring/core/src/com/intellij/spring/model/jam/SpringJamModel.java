package com.intellij.spring.model.jam;

import com.intellij.jam.JamService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.model.jam.javaConfig.JavaSpringConfiguration;
import com.intellij.spring.model.jam.stereotype.*;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SpringJamModel {
  private final Module myModule;


  public static SpringJamModel getModel(@NotNull Module module) {
    return ModuleServiceManager.getService(module, SpringJamModel.class);
  }

  public SpringJamModel(@NotNull final Module module) {
    myModule = module;
  }

  public List<SpringJavaConfiguration> getConfigurations() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);
    List<SpringJavaConfiguration> configurations = new ArrayList<SpringJavaConfiguration>();

    configurations.addAll(service.getJamClassElements(JavaConfigConfiguration.META, SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION, scope));
    configurations.addAll(service.getJamClassElements(JavaSpringConfiguration.META, SpringAnnotationsConstants.JAVA_SPRING_CONFIGURATION_ANNOTATION, scope));

    return configurations;
  }

  @NotNull
  public List<? extends SpringStereotypeElement> getAllStereotypeComponents(@NotNull PsiClass psiMember) {
    final JamService service = JamService.getJamService(myModule.getProject());

    return service
      .getAnnotatedMembersList(psiMember, true, true, false, true, SpringComponent.META, SpringController.META, SpringService.META,
                               SpringRepository.META);
  }

  @NotNull
  public List<? extends SpringStereotypeElement> getAllStereotypeComponents() {
    List<SpringStereotypeElement> stereotypeElements = new ArrayList<SpringStereotypeElement>();

    stereotypeElements.addAll(getComponents());
    stereotypeElements.addAll(getControllers());
    stereotypeElements.addAll(getRepositories());
    stereotypeElements.addAll(getServices());
    stereotypeElements.addAll(getCustomStereotypeComponents());

    return stereotypeElements;
  }

  @NotNull
  public List<? extends CustomSpringComponent> getCustomStereotypeComponents() {
    List<CustomSpringComponent> customSpringComponents = new ArrayList<CustomSpringComponent>();
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    for (String anno : JamAnnotationTypeUtil.getUserDefinedCustomComponentAnnotations(myModule)) {
      customSpringComponents.addAll(service.getJamClassElements(SpringSemContributor.CUSTOM_COMPONENT_JAM_KEY, anno, scope));
    }

    return customSpringComponents;
  }

  @NotNull
  public List<? extends SpringComponent> getComponents() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringComponent.META, SpringAnnotationsConstants.COMPONENT_ANNOTATION, scope);
  }

  @NotNull
  public List<? extends SpringController> getControllers() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringController.META, SpringAnnotationsConstants.CONTROLLER_ANNOTATION, scope);
  }


  @NotNull
  public List<? extends SpringRepository> getRepositories() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringRepository.META, SpringAnnotationsConstants.REPOSITORY_ANNOTATION, scope);
  }


  @NotNull
  public List<? extends SpringService> getServices() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringService.META, SpringAnnotationsConstants.SERVICE_ANNOTATION, scope);
  }

}