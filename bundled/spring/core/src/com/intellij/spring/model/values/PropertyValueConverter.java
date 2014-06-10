/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.values;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.PsiType;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.spring.SpringManager;
import com.intellij.spring.model.xml.beans.TypeHolder;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.WrappingConverter;
import com.intellij.util.xml.converters.values.GenericDomValueConvertersRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class PropertyValueConverter extends WrappingConverter {

  @NotNull
  public List<? extends PsiType> getValueTypes(final GenericDomValue element) {
    if (element instanceof TypeHolder) {
      final List<? extends PsiType> psiTypes = ((TypeHolder)element).getRequiredTypes();
      if (!psiTypes.isEmpty())
      return psiTypes;
    }
    final DomElement parent = element.getParent();
    return parent instanceof TypeHolder ? ((TypeHolder)parent).getRequiredTypes() : Collections.<PsiType>emptyList();
  }

  @NotNull
  public List<Converter> getConverters(@NotNull final GenericDomValue element) {
    
    XmlElement xmlElement = element.getXmlElement();
    if (xmlElement instanceof XmlAttribute) {
      PsiLanguageInjectionHost host = (PsiLanguageInjectionHost)((XmlAttribute)xmlElement).getValueElement();
      if (host == null || host.getInjectedPsi() != null) {
        return Collections.emptyList();
      }
    }
    Project project = element.getManager().getProject();
    final GenericDomValueConvertersRegistry registry = SpringManager.getInstance(project).getValueProvidersRegistry();
    final List<? extends PsiType> types = getValueTypes(element);
    final ArrayList<Converter> list = new ArrayList<Converter>(types.size());
    if (types.isEmpty()) {
      final Converter converter = registry.getConverter(element, null);
      if (converter != null) {
        list.add(converter);
      }
    }
    for (PsiType type : types) {
      final Converter converter = registry.getConverter(element, type);
      if (converter != null) {
        list.add(converter);
      } else {
        return Collections.emptyList();
      }
    }
    return list;
  }

  public Converter getConverter(@NotNull final GenericDomValue domElement) {
    final List<Converter> converters = getConverters(domElement);
    return converters.isEmpty() ? null : converters.get(0);
  }
}
