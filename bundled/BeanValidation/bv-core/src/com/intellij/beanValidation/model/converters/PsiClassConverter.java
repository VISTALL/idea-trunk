/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.model.converters;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.QuickFixFactory;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.ClassUtil;
import com.intellij.util.xml.*;
import com.intellij.util.xml.impl.GenericDomValueReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Konstantin Bulenkov
 */
public class PsiClassConverter extends ResolvingConverter<PsiClass> implements CustomReferenceConverter {

  @Nullable
  protected String getDefaultPackageName(final ConvertContext context) {
    return null;
  }

  @Nullable
  protected String getBaseClassName(final ConvertContext context) {
    return null;
  }

  @NotNull
  protected GlobalSearchScope getVariantsSearchScope(final ConvertContext context) {
    return getDefaultSearchScope(context);
  }

  @NotNull
  protected GlobalSearchScope getResolveSearchScope(final ConvertContext context) {
    return getDefaultSearchScope(context);
  }

  public static GlobalSearchScope getDefaultSearchScope(final ConvertContext context) {
    final GlobalSearchScope searchScope;
    if (context.getModule() != null) {
      searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(context.getModule());
    }
    else {
      searchScope = context.getFile().getResolveScope();
    }
    return searchScope;
  }

  public PsiClass fromString(final String s, final ConvertContext context) {
    final String fullClassName = getQualifiedClassName(s, getDefaultPackageName(context));
    PsiClass result = null;
    if (fullClassName != null) {
      result = DomJavaUtil.findClass(fullClassName, context.getFile(), context.getModule(), getResolveSearchScope(context));
      if (result == null && !Comparing.equal(s, fullClassName)) {
        result = DomJavaUtil.findClass(s, context.getFile(), context.getModule(), getResolveSearchScope(context));
      }
      if (result != null && !Comparing.equal(getQualifiedName(result), fullClassName)) {
        return null;
      }
    }
    return result;
  }

  @Nullable
  public static String getQualifiedClassName(final String s, final String defaultPackage) {
    if (s == null) return null;
    return StringUtil.isEmpty(defaultPackage) || s.indexOf('.') > -1 ? s : defaultPackage + "." + s;
  }

  public String toString(final PsiClass t, final ConvertContext context) {
    if (t == null) return null;
    return processQualifiedName(getQualifiedName(t), context);
  }

  protected String getQualifiedName(final PsiClass t) {
    return isJVMFormat()? ClassUtil.getJVMClassName(t) : t.getQualifiedName();
  }

  protected boolean isJVMFormat() {
    return true;
  }

  private String processQualifiedName(final String qualifiedName, final ConvertContext context) {
    final String defaultPackage = getDefaultPackageName(context);

    if (StringUtil.isNotEmpty(defaultPackage) && StringUtil.startsWithConcatenationOf(qualifiedName, defaultPackage, ".")) {
      return qualifiedName.substring(defaultPackage.length() + 1);
    }
    return qualifiedName;
  }


  @NotNull
  public Collection<? extends PsiClass> getVariants(final ConvertContext context) {
    return Collections.emptyList();
  }

  public void handleElementRename(final GenericDomValue<PsiClass> genericValue, final ConvertContext context, final String newElementName) {
    final PsiClass psiClass = genericValue.getValue();
    if (psiClass != null) {
      final String fqName = psiClass.getQualifiedName();
      if (StringUtil.isNotEmpty(fqName)) {                
        genericValue.setStringValue(processQualifiedName(newElementName, context));
        return;
      }
    }
    super.handleElementRename(genericValue, context, newElementName);
  }

  public void bindReference(final GenericDomValue<PsiClass> genericValue, final ConvertContext context, final PsiElement newTarget) {
    if (newTarget == null || newTarget instanceof PsiClass) {
      genericValue.setStringValue(toString((PsiClass)newTarget, context));
    }
    else {
      super.bindReference(genericValue, context, newTarget);
    }
  }

  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
    final String defaultPackage = getDefaultPackageName(context);
    return getCreateClassQuickFixes(context, defaultPackage);
  }

  public static LocalQuickFix[] getCreateClassQuickFixes(final ConvertContext context, final String defaultPackage) {
    final PsiElement classContext = context.getFile();
    final String stringValue = ((GenericDomValue)context.getInvocationElement()).getStringValue();
    final PsiNameHelper helper = JavaPsiFacade.getInstance(context.getPsiManager().getProject()).getNameHelper();
    final IntentionAction quickFix = !helper.isIdentifier(stringValue) && !helper.isQualifiedName(stringValue)? null :
                                     QuickFixFactory.getInstance().createCreateClassOrPackageFix(classContext, getQualifiedClassName(stringValue, defaultPackage), true, null);
    return quickFix instanceof LocalQuickFix? new LocalQuickFix[] {(LocalQuickFix)quickFix}: LocalQuickFix.EMPTY_ARRAY;
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue genericDomValue, final PsiElement element, final ConvertContext context) {
    final String s = genericDomValue.getStringValue();
    if (s == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    final ArrayList<PsiReference> result = new ArrayList<PsiReference>();
    result.addAll(Arrays.asList(createJavaClassReferences(element, context)));
    //result.add(createDomReference(genericDomValue));
    return result.toArray(new PsiReference[result.size()]);
  }

  protected PsiReference[] createJavaClassReferences(final PsiElement element, final ConvertContext context) {
    final JavaClassReferenceProvider classReferenceProvider = new JavaClassReferenceProvider(context.getPsiManager().getProject());
    setJavaClassReferenceProviderOptions(classReferenceProvider, context);
    classReferenceProvider.setSoft(true);
    return classReferenceProvider.getReferencesByElement(element);
  }

  protected void setJavaClassReferenceProviderOptions(final JavaClassReferenceProvider referenceProvider, final ConvertContext context) {
    final String packageName = getDefaultPackageName(context);
    final String baseClassName = getBaseClassName(context);
    final boolean hasDefPackage = StringUtil.isNotEmpty(packageName);
    if (!StringUtil.isEmpty(baseClassName)) {
      referenceProvider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, new String[] {baseClassName});
    }
    if (hasDefPackage) {
      referenceProvider.setOption(JavaClassReferenceProvider.DEFAULT_PACKAGE, packageName);
    }
    if (isJVMFormat()) {
      referenceProvider.setOption(JavaClassReferenceProvider.JVM_FORMAT, Boolean.TRUE);
    }
  }

  protected GenericDomValueReference createDomReference(final GenericDomValue genericDomValue) {
    return new GenericDomValueReference(genericDomValue);
  }

  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return CodeInsightBundle.message("error.cannot.resolve.class", s);
  }
}

