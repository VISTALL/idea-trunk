/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 10, 2006
 * Time: 4:25:45 PM
 */
package com.intellij.spring.model.converters;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.MutableLookupElement;
import com.intellij.codeInsight.lookup.LookupElementFactory;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.spring.model.xml.custom.ParseCustomBeanIntention;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SpringBeanResolveConverter extends ResolvingConverter<SpringBeanPointer> {

  public SpringBeanPointer fromString(final @Nullable String s, final ConvertContext context) {
    if (s == null) return null;
    final SpringModel model = getSpringModel(context);
    return model == null ? null : SpringUtils.getBeanPointer(model, s);
  }

  public String toString(final @Nullable SpringBeanPointer springBeanPointer, final ConvertContext context) {
    return springBeanPointer == null ? null : springBeanPointer.getName();
  }

  @NotNull
  public Collection<SpringBeanPointer> getVariants(final ConvertContext context) {
    return getVariants(context, false);
  }

  @Override
  public LookupElement createLookupElement(SpringBeanPointer springBeanPointer) {
    return createCompletionVariant(springBeanPointer);
  }

  protected Collection<SpringBeanPointer> getVariants(final ConvertContext context, boolean parentBeans) {
    final SpringModel model = getSpringModel(context);
    if (model == null) return Collections.emptyList();

    final List<SpringBeanPointer> variants = new ArrayList<SpringBeanPointer>();
    final CommonSpringBean currentBean = SpringConverterUtil.getCurrentBeanCustomAware(context);
    processBeans(model, variants, parentBeans ? model.getAllParentBeans() : model.getAllCommonBeans(), parentBeans, currentBean);
    return variants;
  }

  public Collection<SpringBeanPointer> getSmartVariants(final ConvertContext context) {
    final SpringModel model = getSpringModel(context);
    if (model == null) return Collections.emptyList();

    final List<SpringBeanPointer> variants = new ArrayList<SpringBeanPointer>();

    final CommonSpringBean currentBean = SpringConverterUtil.getCurrentBeanCustomAware(context);
    final List<PsiClassType> requiredClasses = getRequiredClasses(context);
    if (requiredClasses != null) {
      for (final PsiClassType requiredClass : requiredClasses) {
        final PsiClass psiClass = PsiUtil.resolveClassInType(requiredClass);
        if (psiClass != null) {
          processBeans(model, variants, model.findBeansByEffectivePsiClassWithInheritance(psiClass), false, currentBean);
        }
        final PsiClass componentClass = PsiUtil.resolveClassInType(PsiUtil.extractIterableTypeParameter(requiredClass, false));
        if (componentClass != null) {
          processBeans(model, variants, model.findBeansByEffectivePsiClassWithInheritance(componentClass), false, currentBean);
        }
      }
    }
    return variants;
  }

  private static void processBeans(SpringModel model, List<SpringBeanPointer> variants, Collection<? extends SpringBaseBeanPointer> pointers, boolean acceptAbstract, CommonSpringBean currentBean) {
    for (SpringBaseBeanPointer bean : pointers) {
      if (!acceptAbstract && bean.isAbstract()) continue;
      if (bean.isReferenceTo(currentBean)) continue;

      final String beanName = bean.getName();
      if (beanName != null) {
        for (String string : model.getAllBeanNames(beanName)) {
          if (StringUtil.isNotEmpty(string)) {
            variants.add(bean.derive(string));
          }
        }
      }
    }
  }


  private final CreateElementQuickFixProvider<SpringBeanPointer> myQuickFixProvider =
      new CreateElementQuickFixProvider<SpringBeanPointer>(SpringBundle.message("model.bean.quickfix.family")) {
        @Override
        public LocalQuickFix[] getQuickFixes(GenericDomValue<SpringBeanPointer> value) {
          List<LocalQuickFix> result = new SmartList<LocalQuickFix>();
          ContainerUtil.addIfNotNull(getQuickFix(value), result);

          final String id = value.getStringValue();
          if (StringUtil.isNotEmpty(id)) {
            final List<SpringModel> models = SpringUtils.getNonEmptySpringModelsByFile(DomUtil.getFile(value));
            final Collection<XmlTag> tags = ContainerUtil.concat(models, new Function<SpringModel, Collection<? extends XmlTag>>() {
              public Collection<? extends XmlTag> fun(SpringModel model) {
                return model.getCustomBeanCandidates(id);
              }
            });
            if (!tags.isEmpty()) {
              result.add(new TryParsingCustomBeansFix(tags));
            }
          }

          return result.toArray(new LocalQuickFix[result.size()]);
        }

        protected void apply(final String elementName, final GenericDomValue<SpringBeanPointer> genericDomValue) {
          Beans beans = getBeans(genericDomValue);
          if (beans != null) {
            final SpringBean springBean = beans.addBean();
            springBean.setName(elementName);
            // setting correct class...
            final DomElement parent = genericDomValue.getParent();
            if (parent instanceof SpringInjection) {
              final PsiClassType classType = SpringBeanUtil.getRequiredClass((SpringInjection)parent);
              if (classType != null) {
                springBean.getClazz().setStringValue(classType.getCanonicalText());
              }
            }
          }
        }

        @NotNull
        protected String getFixName(String elementName) {
          return SpringBundle.message("model.bean.quickfix.message", elementName);
        }
      };

  @Nullable
  protected Beans getBeans(final GenericDomValue<SpringBeanPointer> genericDomValue) {
    return genericDomValue.getParentOfType(Beans.class, false);
  }

  public String getErrorMessage(final String s, final ConvertContext context) {
    return SpringBundle.message("model.bean.error.message", s);
  }

  @Nullable
  protected SpringModel getSpringModel(final ConvertContext context) {
    return SpringManager.getInstance(context.getFile().getProject()).getSpringModelByFile(context.getFile());
  }

  /**
   * Used to filter variants by bean class.
   *
   * @param context conversion context.
   * @return null if no requirement applied; empty list to suppress completion.
   */
  @Nullable
  public List<PsiClassType> getRequiredClasses(ConvertContext context) {
    return null;
  }

  public PsiElement getPsiElement(@Nullable SpringBeanPointer resolvedValue) {
    if (resolvedValue == null) return null;

    return resolvedValue.getPsiElement();
  }

  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
    return myQuickFixProvider.getQuickFixes((GenericDomValue)context.getInvocationElement());
  }

  @Nullable
  private static List<PsiClassType> getValueClasses(ConvertContext context) {
    final TypeHolder valueHolder = context.getInvocationElement().getParentOfType(TypeHolder.class, false);
    assert valueHolder != null;
    if (valueHolder instanceof ConstructorArg) {
      return getClassesForArg((ConstructorArg)valueHolder);
    }
    else {
      final PsiClassType classType = SpringBeanUtil.getRequiredClass(valueHolder);
      return classType == null ? null : Collections.singletonList(classType);
    }
  }

  @Nullable
  private static List<PsiClassType> getClassesForArg(final ConstructorArg arg) {
    PsiType type = arg.getType().getValue();
    if (type == null) {
      Integer integer = arg.getIndex().getValue();
      int index = -1;
      if (integer != null) {
        index = integer.intValue();
      }
      final ArrayList<PsiClassType> classes = new ArrayList<PsiClassType>();
      final SpringBean springBean = (SpringBean)arg.getParent();
      final List<PsiMethod> psiMethods = SpringBeanUtil.getInstantiationMethods(springBean);
      for (PsiMethod method : psiMethods) {
        final PsiParameterList parameterList = method.getParameterList();
        if (parameterList.getParametersCount() > 0) {
          if (index >= 0) {
            if (index < parameterList.getParametersCount()) {
              type = parameterList.getParameters()[index].getType();
              if (type instanceof PsiClassType) {
                classes.add((PsiClassType)type);
              }
            }
          }
          else {
            for (PsiParameter parameter : parameterList.getParameters()) {
              type = parameter.getType();
              if (type instanceof PsiClassType) {
                classes.add((PsiClassType)type);
              }
            }
          }
        }
      }
      return classes;
    }
    else {
      if (type instanceof PsiClassType) {
        return Collections.singletonList((PsiClassType)type);
      }
      else {
        return Collections.emptyList();
      }
    }
  }

  public static MutableLookupElement<SpringBeanPointer> createCompletionVariant(SpringBeanPointer variant) {
    String name = variant.getName();
    PsiClass beanClass = variant.getBeanClass();
    if (name != null) {
      MutableLookupElement<SpringBeanPointer> element = LookupElementFactory.getInstance().createLookupElement(variant, name);
      element.setIcon(variant.getBeanIcon());
      if (beanClass != null) {
        element.setTypeText(beanClass.getName());
      }
      return element;
    }
     return null;
  }

  public static class Local extends SpringBeanResolveConverter {

    @Nullable
    protected SpringModel getSpringModel(final ConvertContext context) {
      return SpringManager.getInstance(context.getFile().getProject()).getLocalSpringModel(context.getFile());
    }

  }

  public static class PropertyBean extends SpringBeanResolveConverter {

    @Nullable
    public List<PsiClassType> getRequiredClasses(ConvertContext context) {
      return getValueClasses(context);
    }
  }

  public static class PropertyLocal extends Local {

    @Nullable
    public List<PsiClassType> getRequiredClasses(ConvertContext context) {
      return getValueClasses(context);
    }


    public void bindReference(final GenericDomValue<SpringBeanPointer> genericValue,
                              final ConvertContext context,
                              final PsiElement newTarget) {
      if (newTarget.getContainingFile() != context.getFile()) {
        final RefBase ref = (RefBase)genericValue.getParent();
        assert ref != null;
        ref.getBean().setStringValue(genericValue.getStringValue());
        genericValue.undefine();
      }
    }
  }

  public static class Parent extends SpringBeanResolveConverter {
  }

  public static class Key extends SpringBeanResolveConverter {

    @Nullable
    public List<PsiClassType> getRequiredClasses(ConvertContext context) {
      SpringEntry entry = (SpringEntry)context.getInvocationElement().getParent();
      assert entry != null;
      final PsiClass keyClass = entry.getRequiredKeyClass();
      return keyClass == null
             ? null
             : Collections.singletonList(JavaPsiFacade.getInstance(keyClass.getProject()).getElementFactory().createType(keyClass));
    }
  }

  private static class TryParsingCustomBeansFix implements LocalQuickFix, IntentionAction {
    private final Collection<XmlTag> myTags;

    public TryParsingCustomBeansFix(Collection<XmlTag> tags) {
      myTags = tags;
    }

    @NotNull
    public String getText() {
      return getName();
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
      return true;
    }

    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      ParseCustomBeanIntention.invokeCustomBeanParsers(project, myTags);
    }

    public boolean startInWriteAction() {
      return false;
    }

    @NotNull
    public String getName() {
      return SpringBundle.message("try.parsing.custom.beans");
    }

    @NotNull
    public String getFamilyName() {
      return SpringBundle.message("model.bean.quickfix.family");
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      throw new UnsupportedOperationException("Method applyFix is not yet implemented in " + getClass().getName());
    }
  }
}
