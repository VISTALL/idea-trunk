package com.intellij.hibernate.model.converters;

import com.intellij.jpa.model.xml.impl.converters.ClassConverterBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * @author Gregory.Shrago
 */
public abstract class ClassWithShortcutsResolvingConverter extends ClassConverterBase {

  @NotNull
  protected abstract Map<String, String> getShortcutsMap();

  public PsiClass fromString(@Nullable @NonNls String s, final ConvertContext context) {
    if (s == null) return null;
    final String generatorClassName = getShortcutsMap().get(s);
    return super.fromString(StringUtil.isNotEmpty(generatorClassName) ? generatorClassName : s, context);
  }

  public Set<String> getAdditionalVariants() {
    return getShortcutsMap().keySet();
  }

  protected void setJavaClassReferenceProviderOptions(final JavaClassReferenceProvider referenceProvider, final ConvertContext context) {
    super.setJavaClassReferenceProviderOptions(referenceProvider, context);
    referenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
  }
}