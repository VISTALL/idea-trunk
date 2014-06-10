/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.aop.psi;

import com.intellij.aop.AopAdvisedElementsSearcher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringModel;
import com.intellij.spring.aop.SpringAdvisedElementsSearcher;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author peter
 */
public class PsiBeanPointcutExpression extends AopElementBase implements PsiPointcutExpression{

  public PsiBeanPointcutExpression(@NotNull final ASTNode node) {
    super(node);
  }

  public String toString() {
    return "PsiBeanPointcutExpression";
  }

  @NotNull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return acceptsClass(member instanceof PsiClass ? (PsiClass)member : member.getContainingClass());
  }

  private PointcutMatchDegree acceptsClass(final PsiClass psiClass) {
    final PsiReference reference = getReference();
    if (reference == null) return PointcutMatchDegree.FALSE;

    final Module module = ModuleUtil.findModuleForPsiElement(psiClass);
    if (module == null) return PointcutMatchDegree.FALSE;

    final Pattern pattern = Pattern.compile(reference.getCanonicalText().replaceAll(" ", "").replaceAll("\\*", "\\.\\*"));

    for (final SpringModel model : SpringUtils.getNonEmptySpringModels(module)) {
      for (final SpringBaseBeanPointer pointer : model.findBeansByPsiClass(psiClass)) {
        final String name = pointer.getName();
        if (StringUtil.isNotEmpty(name) && pattern.matcher(name).matches()) {
          return PointcutMatchDegree.TRUE;
        }
      }
    }

    return PointcutMatchDegree.FALSE;
  }

  @Override
  public PsiReference getReference() {
    final String s = getText();
    final int start = s.indexOf('(');
    if (start < 0) return null;

    int end = s.indexOf(')');
    if (end < 0) end = s.length();
    return new PsiReferenceBase<PsiBeanPointcutExpression>(this, new TextRange(start + 1, end), true) {
      public PsiElement resolve() {
        final Ref<PsiElement> bean = Ref.create(null);
        processBeans(new Processor<SpringBaseBeanPointer>() {
          public boolean process(final SpringBaseBeanPointer s) {
            if (getCanonicalText().equals(s.getName())) {
              bean.set(s.getSpringBean().getIdentifyingPsiElement());
              return false;
            }
            return true;
          }
        });
        return bean.get();
      }

      @Override
      public PsiElement handleElementRename(final String newText) throws IncorrectOperationException {
        final AopPointcutExpressionFile file = (AopPointcutExpressionFile)PsiFileFactory.getInstance(getProject())
          .createFileFromText("a", AopPointcutExpressionFileType.INSTANCE, "bean(" + newText + ")");
        final PsiBeanPointcutExpression pointcutExpression = (PsiBeanPointcutExpression)file.getPointcutExpression();
        assert pointcutExpression != null;
        final ASTNode parent = getNode().getTreeParent();
        parent.replaceChild(getNode(), pointcutExpression.getNode());
        final ASTNode node = parent.findChildByType(getNode().getElementType());
        assert node != null;
        return node.getPsi();
      }

      public Object[] getVariants() {
        final List<LookupElement> result = new ArrayList<LookupElement>();
        processBeans(new Processor<SpringBaseBeanPointer>() {
          public boolean process(final SpringBaseBeanPointer bean) {
            final String name = bean.getName();
            if (name != null && name.indexOf('#') < 0) {
              result.add(LookupElementBuilder.create(name).setIcon(SpringIcons.SPRING_BEAN_ICON));
            }
            return true;
          }
        });
        return result.toArray();
      }
    };
  }

  private boolean processBeans(final Processor<SpringBaseBeanPointer> processor) {
    final AopAdvisedElementsSearcher searcher = getContainingFile().getAopModel().getAdvisedElementsSearcher();
    if (!(searcher instanceof SpringAdvisedElementsSearcher)) return true;
    for (final SpringModel model : ((SpringAdvisedElementsSearcher)searcher).getSpringModels()) {
      for (final SpringBaseBeanPointer pointer : model.getAllCommonBeans(true)) {
        if (!processor.process(pointer)) return false;

      }
    }
    return true;
  }

  @NotNull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Collections.emptyList();
  }

}