package com.intellij.webBeans.utils;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.javaee.util.JamCommonUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.facet.WebBeansFacet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

/**
 * User: Sergey.Vasiliev
 */
public class WebBeansCommonUtils {
  private static final Key<CachedValue<Collection<PsiClass>>> MODULE_BINDING_TYPES_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("MODULE_BINDING_TYPES_ANNOTATIONS");

  private static final Key<CachedValue<Collection<PsiClass>>> MODULE_INTERCEPTOR_BINDING_TYPES_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("MODULE_INTERCEPTOR_BINDING_TYPES_ANNOTATIONS");

  private static final Key<CachedValue<Collection<PsiClass>>> MODULE_SCOPE_TYPES_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("MODULE_SCOPE_TYPES_ANNOTATIONS");

  private static final Key<CachedValue<Collection<PsiClass>>> MODULE_DEPLOYMENT_TYPES_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("MODULE_DEPLOYMENT_TYPES_ANNOTATIONS");

  private static final Key<CachedValue<Collection<PsiClass>>> MODULE_STEREOTYPES_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("MODULE_STEREOTYPES_ANNOTATIONS");

  private static final Key<CachedValue<Boolean>> SIMPLE_WEB_BEAN = new Key<CachedValue<Boolean>>("SIMPLE_WEB_BEAN");

  private WebBeansCommonUtils() {
  }

  @NotNull
  public static Collection<PsiClass> getBindingTypesClasses(final PsiModifierListOwner modifierListOwner) {
    return getBindingTypesClasses(modifierListOwner, ModuleUtil.findModuleForPsiElement(modifierListOwner));
  }

  @NotNull
  public static Set<PsiClass> getBindingTypesClasses(final PsiModifierListOwner modifierListOwner, @Nullable final Module module) {
    Set<PsiClass> bindingTypes = new HashSet<PsiClass>();
    for (PsiClass anno : getBindingTypesClasses(module)) {
      if (AnnotationUtil.isAnnotated(modifierListOwner, anno.getQualifiedName(), true)) {
        bindingTypes.add(anno);
      }
    }
    return bindingTypes;
  }

  @NotNull
  public static Set<PsiClass> getInterceptorBindingTypesClasses(final PsiModifierListOwner modifierListOwner, @Nullable final Module module) {
    Set<PsiClass> bindingTypes = new HashSet<PsiClass>();
    for (PsiClass anno : getInterceptorBindingTypesClasses(module)) {
      if (AnnotationUtil.isAnnotated(modifierListOwner, anno.getQualifiedName(), true)) {
        bindingTypes.add(anno);
      }
    }
    return bindingTypes;
  }


  @NotNull
  public static Collection<PsiClass> getBindingTypesClasses(@Nullable final Module module) {
    return module == null
           ? Collections.<PsiClass>emptyList()
           : JamCommonUtil
             .getAnnotationTypesWithChildren(module, MODULE_BINDING_TYPES_ANNOTATIONS, WebBeansAnnoConstants.BINDING_TYPE_ANNOTATION);
  } @NotNull
    
  public static Collection<PsiClass> getInterceptorBindingTypesClasses(@Nullable final Module module) {
    return module == null
           ? Collections.<PsiClass>emptyList()
           : JamCommonUtil
             .getAnnotationTypesWithChildren(module, MODULE_INTERCEPTOR_BINDING_TYPES_ANNOTATIONS, WebBeansAnnoConstants.INTERCEPTOR_BINDING_TYPE_ANNOTATION);
  }

  @NotNull
  public static Collection<String> getBindingTypesQualifiedNames(final Module module) {
    return getQualifiedNames(getBindingTypesClasses(module));
  }


  @NotNull
  public static Collection<PsiClass> getScopeTypesClasses(@NotNull final Module module) {
    return JamCommonUtil
      .getAnnotatedTypes(module, MODULE_SCOPE_TYPES_ANNOTATIONS, WebBeansAnnoConstants.SCOPE_TYPE_ANNOTATION);
  }

  @NotNull
  public static Collection<String> getScopeQualifiedNames(@NotNull final Module module) {
    return getQualifiedNames(getScopeTypesClasses(module));
  }

  @NotNull
  public static Collection<PsiClass> getDeploymentTypesClasses(@NotNull  final Module module) {
    return JamCommonUtil
      .getAnnotatedTypes(module, MODULE_DEPLOYMENT_TYPES_ANNOTATIONS, WebBeansAnnoConstants.DEPLOYMENT_TYPE_ANNOTATION);
  }

  @NotNull
  public static Collection<PsiClass> getStereotypeAnnotationClasses(@NotNull final Module module) {
    return JamCommonUtil
      .getAnnotatedTypes(module, MODULE_STEREOTYPES_ANNOTATIONS, WebBeansAnnoConstants.STEREOTYPE_ANNOTATION);
  }

  @NotNull
  public static Collection<PsiClass> getStereotypeAnnotationClasses(final Module module, final String... encapsulatedClasses) {
    if (encapsulatedClasses.length == 0) return Collections.emptyList();

    List<PsiClass> stereotypes = new ArrayList<PsiClass>();
    for (PsiClass stereotypeAnnoPsiClass : getStereotypeAnnotationClasses(module)) {
      if (stereotypeAnnoPsiClass.isAnnotationType()) {
        if (AnnotationUtil.isAnnotated(stereotypeAnnoPsiClass, Arrays.asList(encapsulatedClasses))) {
          stereotypes.add(stereotypeAnnoPsiClass);
        }
      }
    }
    return stereotypes;
  }

  @NotNull
  public static Collection<String> getQualifiedNames(final Iterable<PsiClass> annotations) {
    return ContainerUtil.mapNotNull(annotations, new Function<PsiClass, String>() {
      public String fun(PsiClass psiClass) {
        return psiClass.getQualifiedName();
      }
    });
  }


  public static boolean isSimpleWebBean(final PsiClass psiClass) {
    CachedValue<Boolean> cachedValue = psiClass.getUserData(SIMPLE_WEB_BEAN);
    if (cachedValue == null) {
      cachedValue =
        PsiManager.getInstance(psiClass.getProject()).getCachedValuesManager().createCachedValue(new CachedValueProvider<Boolean>() {
          public Result<Boolean> compute() {

            return new Result<Boolean>(SimpleWebBeanValidationUtils.isSimpleWebBean(psiClass),
                                       PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
          }
        }, false);

      psiClass.putUserData(SIMPLE_WEB_BEAN, cachedValue);
    }
    final Boolean value = cachedValue.getValue();

    return value == null ? false : value.booleanValue();
  }

  public static boolean isWebBeansFacetDefined(Module module) {
    if (module == null) return false;

    if (isModuleContainsWebBeansFacet(module)) return true;

    // check module dependencies
    for (Module depModule : JamCommonUtil.getAllModuleDependencies(module)) {
      if (isModuleContainsWebBeansFacet(depModule)) return true;
    }

    return false;
  }

  public static boolean isModuleContainsWebBeansFacet(final Module module) {
    return WebBeansFacet.getInstance(module) != null;
  }

  public static boolean isEnterpaiseWebBean(PsiClass aClass) {
    return false;
  }

  public static List<String> getAnnotations(Class clazz) {
    List<String> annotations = new ArrayList<String>();
    try {
      for (Field field : clazz.getFields()) {
        final Object value = field.get(null);
        if (value instanceof String) {
          annotations.add((String)value);
        }
      }
    }
    catch (IllegalAccessException e) {
      throw new AssertionError(e);
    }
    return annotations;
  }

  public static boolean isDecorator(@NotNull PsiClass psiClass) {
    return AnnotationUtil.isAnnotated(psiClass, WebBeansAnnoConstants.DECORATOR_ANNOTATION, false);
  }

  public static boolean isInterceptor(@NotNull PsiClass psiClass) {
    return AnnotationUtil.isAnnotated(psiClass, WebBeansAnnoConstants.INTERCEPTOR_ANNOTATION, false);
  }
}
