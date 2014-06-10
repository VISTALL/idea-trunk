package com.intellij.spring.model.jam.utils;

import com.intellij.javaee.model.annotations.AnnotationGenericValue;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedMembersSearch;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.util.NullableFunction;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import gnu.trove.THashSet;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.util.*;

/**
 * User: Sergey.Vasiliev
 */
public class JamAnnotationTypeUtil {
  private static final Key<CachedValue<Collection<PsiClass>>> SPRING_MODULE_QUALIIFIER_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("SPRING_MODULE_QUALIIFIER_ANNOTATIONS");
  private static final Key<CachedValue<Collection<PsiClass>>> SPRING_MODULE_COMPONENT_ANNOTATIONS =
    new Key<CachedValue<Collection<PsiClass>>>("SPRING_MODULE_COMPONENT_ANNOTATIONS");
  private static final TObjectHashingStrategy<PsiClass> HASHING_STRATEGY = new TObjectHashingStrategy<PsiClass>() {
    public int computeHashCode(final PsiClass object) {
      final String qualifiedName = object.getQualifiedName();
      return qualifiedName == null ? 0 : qualifiedName.hashCode();
    }

    public boolean equals(final PsiClass o1, final PsiClass o2) {
      return Comparing.equal(o1.getQualifiedName(), o2.getQualifiedName());
    }
  };

  private JamAnnotationTypeUtil() {
  }

  @NotNull
  public static Collection<PsiClass> getAnnotationTypesWithChildren(final String annotationName, final Module module) {
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, false);
    final PsiClass psiClass = JavaPsiFacade.getInstance(module.getProject()).findClass(annotationName, scope);

    final Set<PsiClass> classes = new THashSet<PsiClass>(HASHING_STRATEGY);

    if (psiClass == null || !psiClass.isAnnotationType()) return Collections.emptyList();

    collectClassWithChildren(psiClass, classes, scope);

    return classes;
  }

  @NotNull
  public static List<PsiClass> getQualifierAnnotationTypesWithChildren(final Module module) {
    final List<PsiClass> list = new ArrayList<PsiClass>();

    list.addAll(
      getAnnotationTypesWithChildren(module, SPRING_MODULE_QUALIIFIER_ANNOTATIONS, SpringAnnotationsConstants.QUALIFIER_ANNOTATION));
    list.addAll(getImplicitQualifierAnnotations(module)); // IDEADEV-27559

    return list;
  }

  // IDEADEV-27559
  public static List<PsiClass> getImplicitQualifierAnnotations(final Module module) {
    final List<PsiClass> list = new ArrayList<PsiClass>();

    final JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
    final GlobalSearchScope moduleSearchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);

    final PsiClass configurerClass = facade.findClass(SpringAnnotationsConstants.CUSTOM_AUTOWIRE_CONFIGURER_CLASS, moduleSearchScope);

    if (configurerClass != null) {
      final List<SpringModel> models = SpringManager.getInstance(module.getProject()).getAllModels(module);
      for (SpringModel model : models) {
        final List<SpringBaseBeanPointer> beanPointers = model.findBeansByPsiClassWithInheritance(configurerClass);

        for (SpringBaseBeanPointer beanPointer : beanPointers) {
          final CommonSpringBean bean = beanPointer.getSpringBean();
          final SpringPropertyDefinition propertyDefinition = SpringUtils.findPropertyByName(bean, "customQualifierTypes");
          if (propertyDefinition != null) {
            for (String value : SpringUtils.getListOrSetValues(propertyDefinition)) {
              if (!StringUtil.isEmptyOrSpaces(value)) {
                final PsiClass psiClass = facade.findClass(value, moduleSearchScope);
                if (psiClass != null && psiClass.isAnnotationType()) {
                  list.add(psiClass);
                }
              }
            }
          }
        }
      }
    }
    return list;
  }

  @NotNull
  public static List<String> getUserDefinedCustomComponentAnnotations(final Module module) {
    List<String> annotations = getCustomComponentAnnotations(module);
    for (String annotation : SpringAnnotationsConstants.SPRING_COMPONENT_ANNOTATIONS) {
      annotations.remove(annotation);
    }
    return annotations;
  }

  @NotNull
  public static List<String> getCustomComponentAnnotations(final Module module) {
    final Collection<PsiClass> classes =
      getAnnotationTypesWithChildren(module, SPRING_MODULE_COMPONENT_ANNOTATIONS, SpringAnnotationsConstants.COMPONENT_ANNOTATION);
    return ContainerUtil.mapNotNull(classes, new NullableFunction<PsiClass, String>() {
      public String fun(final PsiClass psiClass) {
        return psiClass.getQualifiedName();
      }
    });
  }

  private static Collection<PsiClass> getAnnotationTypesWithChildren(@Nullable final Module module,
                                                                     final Key<CachedValue<Collection<PsiClass>>> key,
                                                                     final String annotationName) {
    if (module == null) return Collections.emptyList();

    CachedValue<Collection<PsiClass>> cachedValue = module.getUserData(key);
    if (cachedValue == null) {
      cachedValue = PsiManager.getInstance(module.getProject()).getCachedValuesManager()
        .createCachedValue(new CachedValueProvider<Collection<PsiClass>>() {
          public Result<Collection<PsiClass>> compute() {
            final Collection<PsiClass> classes = getAnnotationTypesWithChildren(annotationName, module);
            return new Result<Collection<PsiClass>>(classes, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
          }
        }, false);

      module.putUserData(key, cachedValue);
    }
    final Collection<PsiClass> classes = cachedValue.getValue();

    return classes == null ? Collections.<PsiClass>emptyList() : classes;
  }

  private static void collectClassWithChildren(final PsiClass psiClass, final Set<PsiClass> classes, final GlobalSearchScope scope) {
    classes.add(psiClass);

    for (PsiClass aClass : getChildren(psiClass, scope)) {
      if (!classes.contains(aClass)) {
        collectClassWithChildren(aClass, classes, scope);
      }
    }
  }

  private static Set<PsiClass> getChildren(final PsiClass psiClass, final GlobalSearchScope scope) {
    if (!isAcceptedFor(psiClass, ElementType.ANNOTATION_TYPE, ElementType.TYPE)) return Collections.emptySet();

    final String name = psiClass.getQualifiedName();
    if (name == null) return Collections.emptySet();

    final Set<PsiClass> result = new THashSet<PsiClass>(HASHING_STRATEGY);

    AnnotatedMembersSearch.search(psiClass, scope).forEach(new Processor<PsiMember>() {
      public boolean process(final PsiMember psiMember) {
        if (psiMember instanceof PsiClass && ((PsiClass)psiMember).isAnnotationType()) {
          result.add((PsiClass)psiMember);
        }
        return true;
      }
    });

    return result;
  }

  public static boolean isAcceptedFor(final PsiClass psiClass, final ElementType... elementTypes) {
    final PsiModifierList modifierList = psiClass.getModifierList();
    if (modifierList != null) {
      final PsiAnnotation psiAnnotation = modifierList.findAnnotation(Target.class.getName());
      if (psiAnnotation != null) {
        final List<AnnotationGenericValue<ElementType>> values =
          AnnotationModelUtil.getEnumArrayValue(psiAnnotation, "value", ElementType.class);
        for (AnnotationGenericValue<ElementType> value : values) {
          for (ElementType elementType : elementTypes) {
            if (elementType.equals(value.getValue())) {
              return true;
            }
          }
        }
      }
    }

    return false;
  }
}
