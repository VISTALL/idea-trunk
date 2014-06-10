package com.intellij.spring.model.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInspection.*;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.java.SpringJavaClassInfo;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.spring.model.xml.beans.DomSpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.references.SpringBeanReference;
import com.intellij.spring.references.SpringQualifierReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class SpringJavaAutowiringInspection extends BaseJavaLocalInspectionTool {

  @Nullable
  private static SpringModel getModelForBean(final PsiClass aClass) {
    final SpringJavaClassInfo info = SpringJavaClassInfo.getSpringJavaClassInfo(aClass);
    final List<DomSpringBeanPointer> beans = info.getMappedBeans();
    final Module module = ModuleUtil.findModuleForPsiElement(aClass);
    if (module == null) {
      return null;
    }

    if (beans.isEmpty()) {
      if (!AnnotationUtil.isAnnotated(aClass, JamAnnotationTypeUtil.getCustomComponentAnnotations(module))) {
        return null;
      }
    }

    return SpringManager.getInstance(module.getProject()).getCombinedModel(module);
  }

  @Override
  public ProblemDescriptor[] checkMethod(@NotNull PsiMethod psiMethod, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (SpringAutowireUtil.isAutowiredByAnnotation(psiMethod)) {
      SpringModel springModel = getModelForBean(psiMethod.getContainingClass());
      if (springModel != null) {
        final ProblemsHolder holder = new ProblemsHolder(manager, psiMethod.getContainingFile());
        final boolean required = SpringAutowireUtil.isRequired(psiMethod);
        checkAutowiredMethod(psiMethod, holder, springModel, required);
        return holder.getResultsArray();
      }
    }
    return null;
  }

  @Override
  public ProblemDescriptor[] checkField(@NotNull PsiField psiField, @NotNull InspectionManager manager, boolean isOnTheFly) {
    if (SpringAutowireUtil.isAutowiredByAnnotation(psiField)) {
      SpringModel springModel = getModelForBean(psiField.getContainingClass());
      if (springModel != null) {
        final ProblemsHolder holder = new ProblemsHolder(manager, psiField.getContainingFile());
        final boolean required = SpringAutowireUtil.isRequired(psiField);
        checkAutowiredPsiMember(psiField, psiField.getType(), holder, springModel, required);
        return holder.getResultsArray();
      }
    }
    return null;
  }

  private static void checkAutowiredMethod(final PsiMethod psiMethod, @Nullable final ProblemsHolder holder, final SpringModel springModel,
                                           final boolean required) {
    final PsiAnnotation resourceAnnotation = SpringAutowireUtil.getResourceAnnotation(psiMethod);
    if (resourceAnnotation != null) {
      final PsiType type = PropertyUtil.getPropertyType(psiMethod);
      if (type != null) {
        checkAutowiredPsiMember(psiMethod, type, holder, springModel, required);
      }
    } else {
      for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
         checkAutowiredPsiMember(parameter, parameter.getType(), holder, springModel, required);
      }
    }
  }

  @Nullable
  public static Collection<SpringBaseBeanPointer> checkAutowiredPsiMember(final PsiModifierListOwner modifierListOwner,
                                                                    @NotNull final PsiType psiType,
                                                                    @Nullable final ProblemsHolder holder,
                                                                    @NotNull final SpringModel springModel,
                                                                    final boolean required) {

    PsiType beanType = psiType instanceof PsiArrayType ? ((PsiArrayType)psiType).getComponentType() : PsiUtil.extractIterableTypeParameter(psiType, true);
    final boolean isIterable = beanType != null;
    if (beanType == null) {
      beanType = psiType;
    }
    final PsiAnnotation resourceAnnotation = SpringAutowireUtil.getResourceAnnotation(modifierListOwner);
    if (resourceAnnotation != null && modifierListOwner instanceof PsiMember) {
      final PsiAnnotationMemberValue attributeValue = resourceAnnotation.findDeclaredAttributeValue("name");
      if (attributeValue != null) {
        return checkByNameAutowiring(attributeValue, beanType, holder, springModel);
      }
      String name = null;
      if (modifierListOwner instanceof PsiMethod) {
        name = PropertyUtil.getPropertyNameBySetter((PsiMethod)modifierListOwner);
      } else if (modifierListOwner instanceof PsiField) {
        name = ((PsiField)modifierListOwner).getName();
      }
      if (name != null) {
        final SpringBeanPointer bean = springModel.findBean(name);
        if (bean != null) {
          return Collections.singleton(bean.getBasePointer());
        }
      }
      return checkByTypeAutowire(((PsiNameIdentifierOwner)modifierListOwner).getNameIdentifier(), beanType, holder, springModel, isIterable, required);
    }
    final PsiAnnotation qualifiedAnnotation = SpringAutowireUtil.getQualifiedAnnotation(modifierListOwner);

    if (qualifiedAnnotation == null) {
      return checkByTypeAutowire(((PsiNameIdentifierOwner)modifierListOwner).getNameIdentifier(), beanType, holder, springModel, isIterable, required);
    }
    else {
      return checkQualifiedAutowiring(beanType, qualifiedAnnotation, holder, springModel);
    }
  }

  @Nullable
  private static Collection<SpringBaseBeanPointer> checkByNameAutowiring(final PsiAnnotationMemberValue annotationMemberValue,
                                                                         final PsiType psiType,
                                                                         @Nullable final ProblemsHolder holder,
                                                                         @NotNull final SpringModel model) {
    SpringBeanReference ref = null;
    for (PsiReference reference : annotationMemberValue.getReferences()) {
      if (reference instanceof SpringBeanReference) {
        ref = (SpringBeanReference)reference;
      }
    }
    if (ref != null) {
      final SpringBeanPointer bean = model.findBean(ref.getValue());
      if (bean != null) {
        final PsiClass beanClass = bean.getBeanClass();
        if (psiType instanceof PsiClassType) {
          final PsiClass psiClass = ((PsiClassType)psiType).resolve();
          if (psiClass != null && InheritanceUtil.isInheritorOrSelf(beanClass, psiClass, true)) {
            return Collections.singleton(bean.getBasePointer());
          }
        }
        if (holder != null) {
          holder.registerProblem(ref, SpringBundle.message("cannot.autowire.bean.of.type", psiType.getCanonicalText()), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
      } else {
        if (holder != null) {
          holder.registerProblem(ref);
        }
      }
    }
    return null;
  }

  @Nullable
  private static List<SpringBaseBeanPointer> checkQualifiedAutowiring(final PsiType type,
                                          final PsiAnnotation qualifiedAnnotation,
                                          @Nullable final ProblemsHolder holder,
                                          @NotNull final SpringModel springModel) {

    final PsiAnnotationMemberValue attributeValue = qualifiedAnnotation.findDeclaredAttributeValue("value");
    PsiReference qreference = null;
    final String name = attributeValue == null ? null : AnnotationModelUtil.getStringValue(qualifiedAnnotation, "value", "").getValue();
    final PsiReference[] references;
    if (attributeValue != null) {
      references = attributeValue.getReferences();
      for (PsiReference reference : references) {
        if (reference instanceof SpringQualifierReference) {
          qreference = reference;
          if (((SpringQualifierReference)reference).multiResolve(false).length == 0) {
            if (holder != null && reference.getElement().isPhysical()) {
              holder.registerProblem(reference, SpringBundle.message("bean.class.unknown.qualifier.bean", name), ProblemHighlightType.LIKE_UNKNOWN_SYMBOL);
            }
            return null;
          }
        }
      }
    }
    if (type instanceof PsiClassType) {
      final PsiClass aClass = ((PsiClassType)type).resolve();
      if (aClass != null) {
        List<SpringBaseBeanPointer> candidates = SpringAutowireUtil.getQualifiedBeans(qualifiedAnnotation, springModel);
        if (name != null) {
          final SpringBeanPointer pointer = springModel.findBean(name);
          if (pointer != null) {
            candidates = new ArrayList<SpringBaseBeanPointer>(candidates);
            candidates.add(pointer.getBasePointer());
          }
        }
        final List<SpringBaseBeanPointer> beanPointers = SpringAutowireUtil.excludeAutowireCandidatesForCommonBeans(candidates);

        if (beanPointers.size() == 0) {
          if (holder != null) {
            if (attributeValue != null) {
              reportProblem(holder, qreference, attributeValue,
                            SpringBundle.message("bean.class.unknown.qualifier.bean", name));
            } else {
              final String qualifiedName = qualifiedAnnotation.getQualifiedName();
              assert qualifiedName != null;
              reportProblem(holder, qreference, qualifiedAnnotation,
                            SpringBundle.message("cannot.find.bean.qualified.by", "@" + StringUtil.getShortName(qualifiedName)));
            }
          }
          return null;
        } else if (beanPointers.size() == 1) {
          boolean isAssignable = false;
          for (SpringBaseBeanPointer bean : beanPointers) {
            @NonNls PsiClass[] psiClasses = bean.getEffectiveBeanType();
            for (PsiClass psiClass : psiClasses) {
              if (InheritanceUtil.isInheritorOrSelf(psiClass, aClass, true)) {
                isAssignable = true;
                break;
              }
            }
          }

          if (!isAssignable) {
            if (holder != null) {
              final String message = SpringBundle.message("bean.class.autowired.incorrect.qualifier.type", type.getPresentableText());
              reportProblem(holder, qreference, attributeValue == null ? qualifiedAnnotation : attributeValue, message);
            }
            return null;
          }
        }
        return beanPointers;
      }
    }
    return null;
  }

  private static void reportProblem(@NotNull final ProblemsHolder holder,
                                    final PsiReference qreference,
                                    @NotNull final PsiAnnotationMemberValue attributeValue,
                                    final String text) {
    if (qreference == null) {
      holder.registerProblem(attributeValue, text);
    } else {
      holder.registerProblem(qreference, text, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
    }
  }

  @Nullable
  private static Collection<SpringBaseBeanPointer> checkByTypeAutowire(final PsiElement psiElement,
                                                                 @NotNull final PsiType type,
                                                                 @Nullable final ProblemsHolder holder,
                                                                 @NotNull final SpringModel springModel,
                                                                 final boolean iterable, final boolean required) {

    final Collection<SpringBaseBeanPointer> beanPointers = SpringAutowireUtil.autowireByType(springModel, type);

    if (beanPointers.isEmpty() && required) {
      if (holder != null && !SpringAutowireUtil.isAutowiredByDefault(type)) {
        holder.registerProblem(psiElement, SpringBundle.message("bean.autowiring.by.type.none", type.getPresentableText()));
      }
      return null;
    } else if (beanPointers.size() > 1 && !iterable) {
      if (holder != null) {
        final List<String> beanNames = new ArrayList<String>();
        for (SpringBaseBeanPointer bean : beanPointers) {
          @NonNls String beanName = bean.getName();
          if (StringUtil.isEmptyOrSpaces(beanName)) beanName = "Unknown";
          beanNames.add(beanName);
        }
        Collections.sort(beanNames);
        final String message =
            SpringBundle.message("bean.class.autowired.by.type", type.getPresentableText(), StringUtil.join(beanNames, ","));
        holder.registerProblem(psiElement, message);
      }
      return beanPointers;
    } else {
      return beanPointers;
    }
  }

  @NotNull
  public String getGroupDisplayName() {
    return SpringBundle.message("model.inspection.group.name");
  }

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("model.qualifiers.in.class.inspection.display.name");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringJavaAutowiringInspection";
  }

  public boolean isEnabledByDefault() {
    return true;
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}

