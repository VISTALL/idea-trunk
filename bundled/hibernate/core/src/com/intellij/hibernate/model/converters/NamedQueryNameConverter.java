package com.intellij.hibernate.model.converters;

import com.intellij.hibernate.model.xml.mapping.HbmPersistentObjectBase;
import com.intellij.psi.PsiClass;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * @author Gregory.Shrago
 */
public class NamedQueryNameConverter extends Converter<String> {
  public String fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    final String prefix = getClassPrefix(context);
    return prefix != null && s != null && !s.startsWith(prefix) ? prefix + s : s;
  }

  public String toString(@Nullable final String s, final ConvertContext context) {
    final String prefix = getClassPrefix(context);
    return prefix != null && s != null && s.startsWith(prefix) ? s.substring(prefix.length()) : s;
  }

  @Nullable
  private static String getClassPrefix(final ConvertContext context) {
    final HbmPersistentObjectBase object = context.getInvocationElement().getParentOfType(HbmPersistentObjectBase.class, true);
    if (object != null) {
      final PsiClass psiClass = object.getClazz().getValue();
      return psiClass != null ? psiClass.getQualifiedName() +"." : null;
    }
    return null;
  }

}
