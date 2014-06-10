/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.model.xml.custom;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.*;
import com.intellij.spring.impl.model.AbstractDomSpringBean;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.CustomBean;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.GenericValue;
import com.intellij.util.xml.ReadOnlyGenericValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
*/
public class CustomNamespaceSpringBean extends AbstractDomSpringBean implements CustomBean {
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.model.xml.custom.CustomNamespaceSpringBean");
  private final CustomBeanInfo myInfo;
  private final Module myModule;
  private final XmlTag mySourceTag;
  private final PsiElement myFakePsi;
  private final CustomBeanWrapper myWrapper;
  private final XmlAttribute myIdAttribute;

  public CustomNamespaceSpringBean(@NotNull final CustomBeanInfo info, final Module module, @NotNull CustomBeanWrapper wrapper) {
    myInfo = info;
    myModule = module;
    myWrapper = wrapper;
    XmlTag tag = wrapper.getXmlTag();
    if (tag == null) {
      LOG.error(String.valueOf(wrapper.getParent()));
    }
    tag = CustomBeanRegistry.getActualSourceTag(info, tag);
    mySourceTag = tag;

    myIdAttribute = mySourceTag.getAttribute(info.idAttribute);

    myFakePsi = new CustomBeanFakePsiElement(this);
  }

  @Nullable
  public XmlAttribute getIdAttribute() {
    return myIdAttribute;
  }

  @Nullable
  public GenericValue<SpringBeanPointer> getFactoryBean() {
    final String beanName = myInfo.factoryBeanName;
    if (beanName != null) {
      final SpringModel model = SpringManager.getInstance(getPsiManager().getProject()).getSpringModelByFile((XmlFile)getContainingFile());
      if (model != null) {
        final SpringBeanPointer beanPointer = SpringUtils.getBeanPointer(model, beanName);
        if (beanPointer != null) {
          return ReadOnlyGenericValue.getInstance(beanPointer);
        }
      }
    }
    return super.getFactoryBean();
  }

  @NotNull
  public CustomBeanWrapper getWrapper() {
    return myWrapper;
  }

  @Nullable
  public GenericValue<PsiMethod> getFactoryMethod() {
    final String name = myInfo.factoryMethodName;
    if (name != null) {
      final PsiClass beanClass = getBeanClass(false);
      if (beanClass != null) {
        final PsiMethod method = findMatchingFactoryMethod(name, beanClass);
        if (method != null) {
          return ReadOnlyGenericValue.getInstance(method);
        }
      }
    }
    return super.getFactoryMethod();
  }

  @Nullable
  private PsiMethod findMatchingFactoryMethod(final String name, final PsiClass beanClass) {
    PsiMethod result = null;
    PsiType returnType = null;
    final int count = myInfo.constructorArgumentCount;
    for (final PsiMethod method : beanClass.findMethodsByName(name, true)) {
      if (method.getParameterList().getParametersCount() == count && method.hasModifierProperty(PsiModifier.STATIC)) {
        if (returnType == null) {
          result = method;
          returnType = method.getReturnType();
        } else if (!returnType.equals(method.getReturnType())) {
          return null;
        }
      }
    }
    return result;
  }

  @Nullable
  public String getBeanName() {
    return myInfo.beanName;
  }

  @NotNull
  public String[] getAliases() {
    return ArrayUtil.EMPTY_STRING_ARRAY;
  }

  public boolean isValid() {
    return mySourceTag.isValid();
  }

  @NotNull
  public XmlTag getXmlTag() {
    return mySourceTag;
  }

  public PsiManager getPsiManager() {
    return mySourceTag.getManager();
  }

  @Nullable
  public Module getModule() {
    return myModule;
  }

  @Nullable
  public PsiElement getIdentifyingPsiElement() {
    return myFakePsi;
  }

  @NotNull
  public final PsiFile getContainingFile() {
    return mySourceTag.getContainingFile();
  }

  @Nullable
  public String getClassName() {
    return myInfo.beanClassName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CustomNamespaceSpringBean that = (CustomNamespaceSpringBean)o;

    if (myInfo != null ? !myInfo.equals(that.myInfo) : that.myInfo != null) return false;
    if (myWrapper != null ? !myWrapper.equals(that.myWrapper) : that.myWrapper != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myInfo != null ? myInfo.hashCode() : 0;
    result = 31 * result + (myWrapper != null ? myWrapper.hashCode() : 0);
    return result;
  }
}
