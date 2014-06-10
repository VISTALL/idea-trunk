package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.enums.IdUnsavedValueType;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import com.intellij.util.xml.NamedEnumUtil;
import com.intellij.util.xml.GenericValue;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * @author Gregory.Shrago
 */
public class IdUnsavedValueTypeConverter extends ResolvingConverter<IdUnsavedValueType> {
  @NotNull
  public Collection<? extends IdUnsavedValueType> getVariants(final ConvertContext context) {
    final IdUnsavedValueType[] unsavedValueTypes = IdUnsavedValueType.values();
    return Arrays.asList(unsavedValueTypes).subList(1, unsavedValueTypes.length);
  }

  public IdUnsavedValueType fromString(@Nullable @NonNls String s, final ConvertContext context) {
    if (s == null) return null;
    final IdUnsavedValueType unsavedValueType = NamedEnumUtil.getEnumElementByValue(IdUnsavedValueType.class, s);
    if (unsavedValueType != null) return unsavedValueType;
    try {
      Long.parseLong(s);
      return IdUnsavedValueType.NUMBER;
    }
    catch (NumberFormatException e) {
      // bad bad bad
    }
    return null;
  }

  public String toString(@Nullable IdUnsavedValueType idUnsavedValueType, final ConvertContext context) {
    return idUnsavedValueType == null ? null : idUnsavedValueType == IdUnsavedValueType.NUMBER ? "0" : idUnsavedValueType.getValue();
  }

  public PsiElement resolve(final IdUnsavedValueType o, final ConvertContext context) {
    if (o == null) {
      final String str = ((GenericValue)context.getInvocationElement()).getStringValue();
    }
    return super.resolve(o, context);
  }

  public Set<String> getAdditionalVariants() {
    return new HashSet<String>(Arrays.asList("0", "-1"));
  }
}
