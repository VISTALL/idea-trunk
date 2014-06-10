/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

/*
 * Created by IntelliJ IDEA.
 * User: Sergey.Vasiliev
 * Date: Nov 13, 2006
 * Time: 4:35:08 PM
 */
package com.intellij.spring.model.converters;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementFactory;
import com.intellij.codeInsight.lookup.MutableLookupElement;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.converters.DelimitedListConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SpringBeanListConverter extends DelimitedListConverter<SpringBeanPointer> {

  public SpringBeanListConverter() {
    this(SpringUtils.SPRING_DELIMITERS);
  }

  protected SpringBeanListConverter(String delimiters) {
    super(delimiters);
  }

  @Nullable
  protected SpringBeanPointer convertString(final @Nullable String s, final ConvertContext context) {
    if (s == null) return null;

    final SpringModel model = SpringConverterUtil.getSpringModel(context);
    if (model == null) return null;

    return SpringUtils.findBean(model, s);
  }

  @Nullable
  protected String toString(@Nullable final SpringBeanPointer springBeanPointer) {
    return springBeanPointer == null ? null : springBeanPointer.getName();
  }

  @Nullable
  protected PsiElement resolveReference(final SpringBeanPointer springBeanPointer, final ConvertContext context) {
    return springBeanPointer == null ? null : springBeanPointer.getPsiElement();
  }

  protected String getUnresolvedMessage(final String value) {
    return SpringBundle.message("model.bean.error.message", value);
  }

  protected Object[] getReferenceVariants(final ConvertContext context, GenericDomValue<List<SpringBeanPointer>> genericDomValue) {
    final SpringModel model = SpringConverterUtil.getSpringModel(context);
    if (model == null) return EMPTY_ARRAY;
    
    List<SpringBeanPointer> variants = new ArrayList<SpringBeanPointer>();

    final DomSpringBean currentBean = SpringConverterUtil.getCurrentBean(context);

    final Collection<? extends SpringBeanPointer> allBeans = getVariantBeans(model);
    for (SpringBeanPointer pointer : allBeans) {
      if (pointer.isReferenceTo(currentBean)) continue;

      final String beanName = pointer.getName();
      if (beanName != null) {
        for (String string : model.getAllBeanNames(beanName)) {
          if (StringUtil.isNotEmpty(string)) {
            variants.add(pointer.derive(string));
          }
        }
      }
    }

    final List<SpringBeanPointer> list = genericDomValue.getValue();
    if (list != null) {
      for (Iterator<SpringBeanPointer> i = variants.iterator(); i.hasNext();) {
        final CommonSpringBean variant = i.next().getSpringBean();
        for (SpringBeanPointer existing: list) {
          if (existing.isReferenceTo(variant)) {
            i.remove();
            break;
          }
        }
      }
    }

    List<LookupElement> result = new ArrayList<LookupElement>();
    for (final SpringBeanPointer pointer : variants) {
      final String beanName = pointer.getName();
      if (beanName != null) {
        final MutableLookupElement<SpringBeanPointer> element = LookupElementFactory.getInstance().createLookupElement(pointer, beanName).setIcon(pointer.getBeanIcon());
        final PsiClass psiClass = pointer.getBeanClass();
        if (psiClass != null) {
          element.setTypeText(psiClass.getName());
        }
        result.add(element);
      }
    }

    return result.toArray();
  }

  protected Collection<? extends SpringBaseBeanPointer> getVariantBeans(@NotNull SpringModel model) {
    return model.getAllCommonBeans(true);
  }

  @NotNull
  protected PsiReference createPsiReference(final PsiElement element, final int start, final int end, final ConvertContext context,
                                            final GenericDomValue<List<SpringBeanPointer>> genericDomValue, final boolean delimitersOnly) {
    
    return new MyFixableReference(element, new TextRange(start, end), context, genericDomValue, delimitersOnly);
  }

  protected class MyFixableReference extends MyPsiReference implements LocalQuickFixProvider {

    public MyFixableReference(final PsiElement element, final TextRange range, final ConvertContext context, final GenericDomValue<List<SpringBeanPointer>> genericDomValue,
                              final boolean delimitersOnly) {
      super(element, range, context, genericDomValue, delimitersOnly);
    }

    private final CreateElementQuickFixProvider<List<SpringBeanPointer>> myQuickFixProvider =

      new CreateElementQuickFixProvider<List<SpringBeanPointer>>(SpringBundle.message("model.bean.quickfix.family")) {

        protected String getElementName(@NotNull final GenericDomValue<List<SpringBeanPointer>> genericDomValue) {
          return getValue().trim();
        }

        protected void apply(final String elementName, final GenericDomValue<List<SpringBeanPointer>> genericDomValue) {
          Beans beans = genericDomValue.getParentOfType(Beans.class, false);
          final SpringBean springBean = beans.addBean();
          springBean.setName(elementName);
        }

        @NotNull
        protected String getFixName(String elementName) {
          return SpringBundle.message("model.bean.quickfix.message", elementName);
        }
      };

    public LocalQuickFix[] getQuickFixes() {
      return myQuickFixProvider.getQuickFixes(myGenericDomValue);
    }
  }
}
