package com.intellij.seam.impl.model.xml.components;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.jsp.JspImplUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.seam.model.SeamComponentScope;
import com.intellij.seam.model.xml.components.SeamDomComponent;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.seam.utils.beans.ContextVariable;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class BasicSeamComponentImpl implements SeamDomComponent {

  @NotNull
  public String getComponentName() {
    if (!isValid()) return "";
    String value = getName().getValue();

    return value != null ? value : "";
  }

  @Nullable
  public PsiType getComponentType() {
    if (!isValid()) return null;
    if (!DomUtil.hasXml(getClazz())) {
      final String componentName = getComponentName();

      if (!StringUtil.isEmptyOrSpaces(componentName)) {
        final Module module = getModule();
        if (module != null) {
          for (ContextVariable variable : SeamCommonUtils.getSeamContextVariablesWithDependencies(module, true, false)) {
            if (variable.getName().equals(componentName)) {
              return variable.getType();
            }
          }
        }
      }
    }
    PsiClass psiClass = getClazz().getValue();
    return psiClass == null ? getGenericType() : JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass);
  }

  @Nullable
  private PsiType getGenericType() {
    String stringValue = getClazz().getStringValue();
    if (stringValue != null && stringValue.contains("<") && stringValue.contains(">")) {
      XmlElement element = getXmlElement();
      if (element != null) {
        PsiFile context = element.getContainingFile();

        if (context != null) return JspImplUtil.buildTypeFromTypeString(stringValue, context, context);
      }
    }
    return null;
  }

  @Nullable
  public SeamComponentScope getComponentScope() {
    return getScope().getValue();
  }

  @Nullable
  public PsiElement getIdentifyingPsiElement() {
    return getXmlElement();
  }
}
