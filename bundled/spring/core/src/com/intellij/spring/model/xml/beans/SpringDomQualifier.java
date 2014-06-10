package com.intellij.spring.model.xml.beans;

import com.intellij.psi.PsiClass;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.SpringModelElement;
import com.intellij.spring.model.xml.SpringQualifier;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import com.intellij.util.xml.Namespace;
import com.intellij.util.xml.Convert;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
@Namespace(SpringConstants.BEANS_NAMESPACE_KEY)
public interface SpringDomQualifier extends SpringModelElement, SpringQualifier {

  @NotNull
  @NameValue
  GenericAttributeValue<String> getValue();

  @Convert(SpringQualifierTypeConverter.class)
  @NotNull
  GenericAttributeValue<PsiClass> getType();

  @NotNull
  List<SpringDomAttribute> getAttributes();
}