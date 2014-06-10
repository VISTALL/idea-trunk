package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.enums.CascadeType;
import com.intellij.hibernate.HibernateMessages;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.NamedEnumUtil;
import com.intellij.util.xml.converters.DelimitedListConverter;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.MessageFormat;

/**
 * @author Gregory.Shrago
 */
public class CascadeTypeListConverter extends DelimitedListConverter<CascadeType> {

  public CascadeTypeListConverter() {
    super(", ");
  }

  protected CascadeType convertString(final @Nullable String string, final ConvertContext context) {
    if (string == null) return null;
    return NamedEnumUtil.getEnumElementByValue(CascadeType.class, string);
  }

  protected String toString(final @Nullable CascadeType cascadeType) {
    return cascadeType == null ? null : cascadeType.getValue();
  }

  protected Object[] getReferenceVariants(final ConvertContext context, final GenericDomValue<List<CascadeType>> genericDomValue) {
    final List<CascadeType> variants = new ArrayList<CascadeType>(Arrays.asList(CascadeType.values()));
    filterVariants(variants, genericDomValue);
    return ElementPresentationManager.getInstance().createVariants(variants, new Function<CascadeType, String>() {
      public String fun(final CascadeType cascadeType) {
        return cascadeType.getValue();
      }
    });
  }

  protected PsiElement resolveReference(@Nullable final CascadeType cascadeType, final ConvertContext context) {
    return cascadeType == null ? null : context.getReferenceXmlElement();
  }

  protected String getUnresolvedMessage(final String value) {
    return MessageFormat.format(HibernateMessages.message("cannot.resolve.cascade.type.0"), value);
  }
}