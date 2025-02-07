package org.jetbrains.idea.maven.dom.converters;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.dom.references.MavenUrlPsiReference;

public class MavenUrlConverter extends MavenReferenceConverter<String> {
  @Override
  public String fromString(@Nullable @NonNls String s, ConvertContext context) {
    return s;
  }

  @Override
  public String toString(@Nullable String text, ConvertContext context) {
    return text;
  }

  protected PsiReference createReference(PsiElement element, String text, TextRange range) {
    return new MavenUrlPsiReference(element, text, range);
  }
}