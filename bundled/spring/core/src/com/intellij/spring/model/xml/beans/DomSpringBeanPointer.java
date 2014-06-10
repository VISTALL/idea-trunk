/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.xml.beans;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.util.PatchedWeakReference;
import com.intellij.util.xml.impl.DomAnchorImpl;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

/**
 * @author peter
 */
public class DomSpringBeanPointer extends SpringBaseBeanPointer {
  @NotNull private final DomAnchorImpl<DomSpringBean> myPointer;
  private final NotNullLazyValue<Boolean> myAbstract = new NotNullLazyValue<Boolean>() {
    @NotNull
    protected Boolean compute() {
      final CommonSpringBean bean = getSpringBean();
      if (bean instanceof SpringBean) {
        final Boolean value = ((SpringBean)bean).getAbstract().getValue();
        return value != null && value.booleanValue();
      }
      return false;
    }
  };
  private final NullableLazyValue<SpringBeanPointer> myParent = new NullableLazyValue<SpringBeanPointer>() {
    protected SpringBeanPointer compute() {
      final CommonSpringBean parent = getSpringBean();
      return parent instanceof SpringBean ? ((SpringBean)parent).getParentBean().getValue() : null;
    }
  };
  private final NullableLazyValue<PsiClass> myBeanClass = new NullableLazyValue<PsiClass>() {
    protected PsiClass compute() {
      return getSpringBean().getBeanClass();
    }
  };

  private WeakReference<DomSpringBean> myCachedValue;

  private DomSpringBeanPointer(@NotNull final DomSpringBean springBean) {
    super(springBean.getBeanName());
    ProgressManager.getInstance().checkCanceled();
    myCachedValue = new PatchedWeakReference<DomSpringBean>(springBean);
    final XmlTag tag = springBean.getXmlTag();
    assert tag != null;
    myPointer = DomAnchorImpl.createAnchor(springBean);
  }

  @NotNull
  public DomSpringBean getSpringBean() {
    DomSpringBean bean = myCachedValue.get();
    if (bean != null) return bean;

    bean = myPointer.retrieveDomElement();
    assert bean != null : "No bean at pointer";
    myCachedValue = new PatchedWeakReference<DomSpringBean>(bean);
    return bean;
  }

  public boolean isValid() {
    DomSpringBean bean = myCachedValue.get();
    if (bean != null) return bean.isValid();

    bean = myPointer.retrieveDomElement();
    if (bean != null && bean.isValid()) {
      myCachedValue = new PatchedWeakReference<DomSpringBean>(bean);
      return true;
    }
    return false;
  }

  public PsiManager getPsiManager() {
    return PsiManager.getInstance(getContainingFile().getProject());
  }

  public static DomSpringBeanPointer createDomSpringBeanPointer(final @NotNull DomSpringBean bean) {
    return new DomSpringBeanPointer(bean);
  }

  public synchronized boolean isAbstract() {
    return myAbstract.getValue().booleanValue();
  }

  public synchronized SpringBeanPointer getParentPointer() {
    return myParent.getValue();
  }

  public PsiElement getPsiElement() {
    return getSpringBean().getXmlElement();
  }

  public SpringBeanPointer derive(@NotNull final String name) {
    return Comparing.equal(name, getName()) ? this : new DerivedSpringBeanPointer(this, name);
  }

  public synchronized PsiClass getBeanClass() {
    return myBeanClass.getValue();
  }

  public PsiFile getContainingFile() {
    return myPointer.getContainingFile();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof DomSpringBeanPointer)) return false;

    return myPointer.equals(((DomSpringBeanPointer)o).myPointer);
  }

  @Override
  public int hashCode() {
    return myPointer.hashCode();
  }
}
