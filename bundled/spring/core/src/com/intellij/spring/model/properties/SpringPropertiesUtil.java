/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.properties;

import com.intellij.codeInsight.TargetElementUtilBase;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.model.jam.javaConfig.SpringJavaBean;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public class SpringPropertiesUtil {
  private SpringPropertiesUtil() {
  }

  @Nullable
  public static BeanProperty getBeanProperty(DataContext dataContext) {
    final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    final PsiFile file = LangDataKeys.PSI_FILE.getData(dataContext);

    return getBeanProperty(editor, file);
  }

  @Nullable
  public static BeanProperty getBeanProperty(final Editor editor, final PsiFile file) {
    if (editor != null && file instanceof XmlFile) {
      final int offset = editor.getCaretModel().getOffset();
      final DomElement value = DomUtil.getContextElement(editor);
      final SpringPropertyDefinition property = DomUtil.getParentOfType(value, SpringPropertyDefinition.class, false);

      if (property == null || isJavaBeanReference(file, editor, offset)) return null;

      final PsiReference reference = TargetElementUtilBase.findReference(editor, offset);
      if (reference != null) {
        final PsiElement psiElement = reference.resolve();
        if (psiElement instanceof PsiMethod) {
          return BeanProperty.createBeanProperty((PsiMethod)psiElement);
        }
      }
    }
    return null;
  }

  static boolean isJavaBeanReference(final PsiFile file, final Editor editor, final int offset) {
    final XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(file.findElementAt(offset), XmlAttribute.class);

    if (xmlAttribute != null) {
      final DomElement value = DomManager.getDomManager(file.getProject()).getDomElement(xmlAttribute);
      if (value instanceof GenericAttributeValue) {
        final Object attributeValue = ((GenericAttributeValue)value).getValue();
        if (attributeValue instanceof SpringBeanPointer) {
          return ((SpringBeanPointer)attributeValue).getSpringBean() instanceof SpringJavaBean;
        }
      }
    }

    return false;
  }
}
