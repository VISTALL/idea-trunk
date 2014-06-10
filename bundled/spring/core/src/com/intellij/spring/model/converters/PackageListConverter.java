package com.intellij.spring.model.converters;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PackageReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.PsiPackageReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.PatternUtil;
import com.intellij.util.xml.*;
import com.intellij.util.xml.converters.DelimitedListProcessor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * @author Dmitry Avdeev
 */
public class PackageListConverter extends Converter<Collection<PsiPackage>> implements CustomReferenceConverter {

  public Collection<PsiPackage> fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    if (s == null) {
      return Collections.emptyList();
    }
    final XmlAttributeValue xmlAttributeValue = ((GenericAttributeValue)context.getInvocationElement()).getXmlAttributeValue();

    if (xmlAttributeValue == null) {
      return Collections.emptyList();
    }
    final PsiReference[] psiReferences = xmlAttributeValue.getReferences();
    final Collection<PsiPackage> list = new HashSet<PsiPackage>();
    for (PsiReference psiReference : psiReferences) {
      if (psiReference instanceof PsiPackageReference) {
        list.addAll(((PsiPackageReference)psiReference).getReferenceSet().resolvePackage());
      }
    }
    return list;
  }

  public String toString(@Nullable final Collection<PsiPackage> psiPackages, final ConvertContext context) {
    return null;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    final String text = genericDomValue.getStringValue();
    if (text == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    final ArrayList<PsiReference> list = new ArrayList<PsiReference>();
    new DelimitedListProcessor(",") {
      protected void processToken(final int start, final int end, final boolean delimitersOnly) {
        final PackageReferenceSet referenceSet = new PackageReferenceSet(text.substring(start, end), element, 1 + start) {
          @Override
          public Collection<PsiPackage> resolvePackageName(final PsiPackage context, final String packageName) {
            if (packageName.contains("*")) {
              final Pattern pattern = PatternUtil.fromMask(packageName);
              final PsiPackage[] psiPackages = context.getSubPackages();
              final ArrayList<PsiPackage> packages = new ArrayList<PsiPackage>(psiPackages.length);
              for (PsiPackage aPackage : psiPackages) {
                if (pattern.matcher(aPackage.getName()).matches()) {
                  packages.add(aPackage);
                }
              }
              return packages;
            } else {
              return super.resolvePackageName(context, packageName);
            }
          }
        };
        list.addAll(referenceSet.getReferences());
      }
    }.processText(text);
    return list.toArray(new PsiReference[list.size()]);
  }
}
