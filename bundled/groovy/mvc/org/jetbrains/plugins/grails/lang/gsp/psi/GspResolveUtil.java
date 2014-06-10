/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.jetbrains.plugins.grails.lang.gsp.psi;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.*;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspExprInjection;
import org.jetbrains.plugins.grails.lang.gsp.resolve.taglib.GspTagLibUtil;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrExpression;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;
import org.jetbrains.plugins.groovy.lang.resolve.processors.ResolverProcessor;

/**
 * @author ven
 */
public class GspResolveUtil {
  public static boolean isGspTagMember(PsiMember member) {
    if (member == null) return false;
    PsiFile file = member.getContainingFile();
    if (file == null) return false;

    PsiDirectory dir = file.getContainingDirectory();
    if (dir != null && GrailsUtils.TAGLIB_DIRECTORY.equals(dir.getName())) {
      return true;
    }

    Application application = ApplicationManager.getApplication();
    if (dir != null && application.isUnitTestMode() && GrailsUtils.MOCK_TAGLIB_DIRECTORY.equals(dir.getName())) {
      return true;
    }

    String packageName = null;
    if (file instanceof GroovyFile) {
      packageName = ((GroovyFile) file).getPackageName();
    } else if (file instanceof PsiJavaFile) {
      packageName = ((PsiJavaFile) file).getPackageName();
    }

    if (GspTagLibUtil.DYNAMIC_TAGLIB_PACKAGE.equals(packageName)) {
      return true;
    }
    return false;
  }

  public static void collectGspQualifiedVariants(ResolverProcessor processor, GrReferenceExpression refExpr) {
    GrExpression qualifier = refExpr.getQualifierExpression();
    if (qualifier == null ||
            !(qualifier instanceof GrReferenceExpression)) {
      return;
    }

    String prefix = ((GrReferenceExpression) qualifier).getName();
    if (prefix == null) return;
    for (PsiClass tagLibClass : GspTagLibUtil.getCustomTagLibClasses(refExpr, prefix)) {
      tagLibClass.processDeclarations(processor, ResolveState.initial(), null, refExpr);
    }
  }

  public static boolean isUnderExprInjection(GrReferenceExpression refExpr) {
    if (refExpr == null) {
      return false;
    }
    PsiElement parent = refExpr.getParent();
    while (parent != null && !(parent instanceof PsiFile)) {
      if (parent instanceof GrGspExprInjection) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

  public static void collectGspUnqualifiedVariants(ResolverProcessor processor, GrReferenceExpression refExpr) {

    // Non-qualified gsp tags
    for (PsiClass tagLibClass : GspTagLibUtil.getCustomTagLibClasses(refExpr, GspTagLibUtil.DEFAULT_TAGLIB_PREFIX)) {
      tagLibClass.processDeclarations(processor, ResolveState.initial(), null, refExpr);
    }

    // Dynmic Grails tag
    for (PsiClass tagLibClass : GspTagLibUtil.getDynamicTagLibClasses(refExpr)) {
      tagLibClass.processDeclarations(processor, ResolveState.initial(), null, refExpr);
    }
  }

  public static void collectTagLibNamespaceFields(ResolverProcessor processor, GrReferenceExpression refExpr) {
    // Custom taglibs namespaces
    String namespace = refExpr.getReferenceName();
    if (namespace != null) {
      for (PsiClass tagLibClass : GspTagLibUtil.getCustomTagLibClasses(refExpr, namespace)) {
        tagLibClass.processDeclarations(processor, ResolveState.initial(), null, refExpr);
      }
    }
  }
}
