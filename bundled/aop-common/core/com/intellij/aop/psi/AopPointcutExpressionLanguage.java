/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SingleLazyInstanceSyntaxHighlighterFactory;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class AopPointcutExpressionLanguage extends Language implements InjectableLanguage {
  protected AopPointcutExpressionLanguage() {
    super("PointcutExpression");
    SyntaxHighlighterFactory.LANGUAGE_FACTORY.addExplicitExtension(this, new SingleLazyInstanceSyntaxHighlighterFactory() {
      @NotNull
      protected SyntaxHighlighter createHighlighter() {
        return new AopSyntaxHighlighter();
      }
    });
  }

  @Override
  public FileType getAssociatedFileType() {
    return AopPointcutExpressionFileType.INSTANCE;
  }

  public static AopPointcutExpressionLanguage getInstance() {
    // to initialize
    //noinspection UnusedDeclaration
    final AopPointcutExpressionFileType fileType = AopPointcutExpressionFileType.INSTANCE;
    return findInstance(com.intellij.aop.psi.AopPointcutExpressionLanguage.class);
  }
}
