/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.CreateBeanPropertyFix;
import com.intellij.psi.meta.PsiPresentableMetaData;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.xml.*;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlUndefinedElementFixProvider;
import com.intellij.xml.impl.XmlAttributeDescriptorEx;
import com.intellij.xml.impl.schema.XmlNSDescriptorImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author peter
 */
public class PNamespaceDescriptor extends XmlNSDescriptorImpl implements XmlUndefinedElementFixProvider {

  public XmlAttributeDescriptor getAttribute(final String localName, final String namespace, final XmlTag context) {
    // TODO: this is not efficient!
    if (SpringConstants.P_NAMESPACE.equals(namespace)) {
      for(XmlAttributeDescriptor a:getAttributeDescriptors(context)) {
        if (a.getName().equals(localName)) return a;
      }
    }
    return super.getAttribute(localName, namespace, context);
  }

  @Nullable
  private static PsiClass getClass(@NotNull final XmlTag tag) {
    final DomElement element = DomManager.getDomManager(tag.getProject()).getDomElement(tag);

    if (element instanceof SpringBean) {
      final SpringBean bean = (SpringBean)element;
      return bean.getBeanClass();
    }
    return null;
  }

  private static XmlAttributeDescriptor[] getAttributeDescriptors(@NotNull final XmlTag tag) {
    final PsiClass psiClass = getClass(tag);
    if (psiClass == null) {
      return XmlAttributeDescriptor.EMPTY;
    }
    final List<XmlAttributeDescriptor> result = new ArrayList<XmlAttributeDescriptor>();
    final Map<String,PsiMethod> properties = PropertyUtil.getAllProperties(psiClass, true, false);

    for (final String propertyName : properties.keySet()) {
      final PsiMethod method = properties.get(propertyName);
      result.add(new PAttributeDescriptor(propertyName, "", method));

      if (method.getParameterList().getParameters()[0].getType() instanceof PsiClassType) {
        result.add(new PAttributeDescriptor(propertyName, "-ref", method));
      }
    }
    return result.toArray(new XmlAttributeDescriptor[result.size()]);
  }

  @NotNull
  public XmlElementDescriptor[] getRootElementsDescriptors(@Nullable final XmlDocument doc) {
    return XmlElementDescriptor.EMPTY_ARRAY;
  }

  public XmlAttributeDescriptor[] getRootAttributeDescriptors(final XmlTag context) {
    return getAttributeDescriptors(context);
  }

  @NotNull
  public IntentionAction[] createFixes(final @NotNull XmlElement element) {
    if (element instanceof XmlAttribute) {
      final PsiClass psiClass = getClass((XmlTag)element.getParent());
      if (psiClass != null) {
        PsiType type = null;
        @NonNls final String localName = ((XmlAttribute)element).getLocalName();
        final Project project = element.getProject();
        if (localName.endsWith("-ref")) {
          final SpringModel model = SpringManager.getInstance(project).getSpringModelByFile((XmlFile)element.getContainingFile());
          final SpringBeanPointer pointer = SpringUtils.getBeanPointer(model, ((XmlAttribute)element).getDisplayValue());
          if (pointer != null && pointer.getEffectiveBeanType().length > 0) {
            type = JavaPsiFacade.getInstance(project).getElementFactory().createType(pointer.getEffectiveBeanType()[0]);
          }
        }
        @NonNls String name = ((XmlAttribute)element).getLocalName();
        if (name.endsWith("-ref")) {
          name = name.substring(0, name.length() - "-ref".length());
        }
        return CreateBeanPropertyFix.createActions(name, psiClass, type, true);
      }
    }
    return IntentionAction.EMPTY_ARRAY;
  }

  private static class PAttributeDescriptor implements XmlAttributeDescriptorEx, PsiPresentableMetaData {
    private final String myPropertyName;
    private final String mySuffix;
    private final PsiMethod myMethod;

    public PAttributeDescriptor(@NonNls final String propertyName, @NonNls String suffix, final PsiMethod method) {
      myPropertyName = propertyName;
      mySuffix = suffix;
      myMethod = method;
    }

    public String getName() {
      return myPropertyName + mySuffix;
    }

    public void init(PsiElement element) {
      throw new UnsupportedOperationException("Method init is not yet implemented in " + getClass().getName());
    }

    public Object[] getDependences() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    public PsiElement getDeclaration() {
      return myMethod;
    }

    @NonNls
    public String getName(PsiElement context) {
      String name = getName();
      final String prefix = ((XmlTag)context).getPrefixByNamespace(SpringConstants.P_NAMESPACE);
      name = (StringUtil.isNotEmpty(prefix) ? prefix + ":" : "") + name;
      return name;
    }

    public boolean isRequired() {
      return false;
    }

    public boolean isFixed() {
      return false;
    }

    public boolean hasIdType() {
      return false;
    }

    public boolean hasIdRefType() {
      return false;
    }

    @Nullable
    public String getDefaultValue() {
      return null;
    }

    public boolean isEnumerated() {
      return false;
    }

    public String[] getEnumeratedValues() {
      return ArrayUtil.EMPTY_STRING_ARRAY;
    }

    @Nullable
    public String validateValue(XmlElement context, String value) {
      return null;
    }

    public String getTypeName() {
      throw new UnsupportedOperationException("Method getTypeName is not yet implemented in " + getClass().getName());
    }

    @Nullable
    public Icon getIcon() {
      return SpringIcons.SPRING_BEAN_PROPERTY_ICON;
    }

    @NonNls
    public String handleTargetRename(@NotNull @NonNls final String newTargetName) {
      final String propertyName = PropertyUtil.getPropertyName(newTargetName);
      return propertyName == null ? null : propertyName + mySuffix;

    }
  }
}
