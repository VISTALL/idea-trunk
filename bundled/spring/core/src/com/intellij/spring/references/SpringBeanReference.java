package com.intellij.spring.references;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.InheritanceUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.converters.SpringBeanResolveConverter;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SpringBeanReference extends PsiReferenceBase<PsiLiteralExpression> implements EmptyResolveMessageProvider {

  private final PsiClass myRequiredClass;

  public SpringBeanReference(final PsiLiteralExpression element, final PsiClass requiredClass) {
    super(element);
    myRequiredClass = requiredClass;
  }

  public SpringBeanReference(final PsiLiteralExpression element) {
    this(element, null);
  }

  public PsiElement resolve() {
    final Object value = myElement.getValue();
    if (!(value instanceof String)) return null;

    final SpringModel model = getSpringModel();
    if (model == null) return null;
    final SpringBeanPointer springBean = model.findBean((String)value);

    return springBean == null ? null : springBean.getPsiElement();
  }

  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    return getElement();
  }

  @Nullable
  private SpringModel getSpringModel() {
    final Module module = ModuleUtil.findModuleForPsiElement(myElement);
    if (module == null) return null;

    return SpringManager.getInstance(module.getProject()).getCombinedModel(module);
  }

  public Object[] getVariants() {
    List<Object> lookups = new ArrayList<Object>();
    final SpringModel model = getSpringModel();
    if (model != null) {
      final Collection<? extends SpringBeanPointer> list = model.getAllCommonBeans(true);

      for (SpringBeanPointer bean : list) {
        final String beanName = bean.getName();
        if (beanName != null && StringUtil.isNotEmpty(beanName)) {
          final PsiClass beanClass = bean.getBeanClass();
          if (myRequiredClass != null && (beanClass == null || !InheritanceUtil.isInheritorOrSelf(beanClass, myRequiredClass, true))) {
            continue;            
          }
          lookups.add(SpringBeanResolveConverter.createCompletionVariant(bean));
        }
      }
    }
    return ArrayUtil.toObjectArray(lookups);
  }

  public String getUnresolvedMessagePattern() {
    return SpringBundle.message("model.bean.error.message", getValue());
  }
}
