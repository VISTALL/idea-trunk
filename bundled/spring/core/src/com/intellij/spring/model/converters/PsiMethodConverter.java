/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @author Dmitry Avdeev
 */
public abstract class PsiMethodConverter extends Converter<PsiMethod> implements CustomReferenceConverter<PsiMethod> {

  protected final static Object[] EMPTY_ARRAY = ArrayUtil.EMPTY_OBJECT_ARRAY;

  private final MethodAccepter myMethodAccepter;

  public PsiMethodConverter(MethodAccepter accepter) {
    myMethodAccepter = accepter;
  }

  public PsiMethodConverter() {
    this(new MethodAccepter());
  }

  protected static class MethodAccepter {
    public boolean accept(PsiMethod method) {
      return !method.isConstructor() &&
             method.hasModifierProperty(PsiModifier.PUBLIC) &&
             !method.hasModifierProperty(PsiModifier.STATIC);
    }
  }

  public PsiMethod fromString(@Nullable final String methodName, final ConvertContext context) {
    if (methodName == null || methodName.length() == 0) {
      return null;
    }
    final PsiClass psiClass = getPsiClass(context);
    if (psiClass != null) {
      final PsiMethod[] psiMethods = psiClass.findMethodsByName(methodName, true);
      if (psiMethods.length == 0) {
        return null;
      }
      final MethodAccepter accepter = getMethodAccepter(context, false);
      for (PsiMethod method: psiMethods) {
        if (accepter.accept(method)) {
          return method;
        }
      }
      return psiMethods[0];
    }
    else {
      return null;
    }
  }

  public String toString(@Nullable final PsiMethod psiMethods, final ConvertContext context) {
    return null;
  }

  @Nullable
  protected abstract PsiClass getPsiClass(final ConvertContext context);

  protected MethodAccepter getMethodAccepter(ConvertContext context, final boolean forCompletion) {
    return myMethodAccepter;
  }

  private Object[] getVariants(ConvertContext context) {
    final PsiClass psiClass = getPsiClass(context);
    if (psiClass == null) {
      return EMPTY_ARRAY;
    }
    final ArrayList<Object> result = new ArrayList<Object>();
    final MethodAccepter methodAccepter = getMethodAccepter(context, true);
    PsiMethod[] methods;
    if (psiClass.isEnum()) {
      MethodResolveProcessor processor = new MethodResolveProcessor();
      psiClass.processDeclarations(processor, ResolveState.initial(), null, psiClass);
      methods = processor.getMethods();
    } else {
      methods = psiClass.getAllMethods();
    }
    for (PsiMethod method: methods) {
      if (methodAccepter.accept(method)) {
        String tail = PsiFormatUtil.formatMethod(method,
                                          PsiSubstitutor.EMPTY,
                                          PsiFormatUtil.SHOW_PARAMETERS,
                                          PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_TYPE);

        final PsiType returnType = method.getReturnType();
        final Object value = LookupValueFactory.createLookupValueWithHintAndTail(method.getName(),
                                     method.getIcon(0),
                                     returnType == null ? null : returnType.getPresentableText(),
                                     tail);
        result.add(value);
      }
    }
    return ArrayUtil.toObjectArray(result);
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<PsiMethod> genericDomValue, final PsiElement element, final ConvertContext context) {

    return new PsiReference[] { new MyReference(element, genericDomValue, context) };
  }

  protected class MyReference extends PsiReferenceBase<PsiElement> implements EmptyResolveMessageProvider, LocalQuickFixProvider {
    private final GenericDomValue<PsiMethod> myGenericDomValue;
    private final ConvertContext myContext;

    public MyReference(final PsiElement element,
                       final GenericDomValue<PsiMethod> genericDomValue,
                       ConvertContext context) {
      super(element);
      myGenericDomValue = genericDomValue;
      myContext = context;
    }

    public Object[] getVariants() {
      return PsiMethodConverter.this.getVariants(myContext);
    }

    @Nullable
    public PsiElement resolve() {
      return myGenericDomValue.getValue();
    }

    public boolean isSoft() {
      return true;
    }

    public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
      assert element instanceof PsiMethod : "PsiMethod expected";
      final PsiMethod psiMethod = (PsiMethod)element;
      myGenericDomValue.setStringValue(psiMethod.getName());
      return psiMethod;
    }

    public LocalQuickFix[] getQuickFixes() {
      return PsiMethodConverter.this.getQuickFixes(myContext);
    }

    public String getUnresolvedMessagePattern() {
      return SpringBundle.message("cannot.resolve.method", myGenericDomValue.getStringValue());
    }
  }

  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
     return new LocalQuickFix[0];
  }
}
