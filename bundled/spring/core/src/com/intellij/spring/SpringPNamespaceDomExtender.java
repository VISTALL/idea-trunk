/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.beans.PNamespaceRefValue;
import com.intellij.spring.model.xml.beans.PNamespaceValue;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class SpringPNamespaceDomExtender extends DomExtender<SpringBean> {

  public void registerExtensions(@NotNull final SpringBean bean, @NotNull final DomExtensionsRegistrar registrar) {
    final XmlTag tag = bean.getXmlTag();
    if (tag == null || tag.getPrefixByNamespace(SpringConstants.P_NAMESPACE) == null) {
      return;
    }
    final PsiClass beanClass = bean.getBeanClass();
    if (beanClass != null) {
      for (final String name : PropertyUtil.getAllProperties(beanClass, true, false).keySet()) {
        registrar.registerAttributeChildExtension(new XmlName(name, SpringConstants.P_NAMESPACE_KEY), PNamespaceValue.class);
        registrar.registerAttributeChildExtension(new XmlName(name + "-ref", SpringConstants.P_NAMESPACE_KEY), PNamespaceRefValue.class);
      }
    }
  }

}