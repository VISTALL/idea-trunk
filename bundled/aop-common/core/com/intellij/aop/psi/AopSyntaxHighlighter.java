/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.aop.psi;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.aop.lexer.AopLexer;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author peter
 */
public class AopSyntaxHighlighter extends SyntaxHighlighterBase implements AopElementTypes {
  public static final TextAttributesKey AOP_KEYWORD = SyntaxHighlighterColors.KEYWORD;
  public static final TextAttributesKey AOP_OPERATION_SIGN = SyntaxHighlighterColors.OPERATION_SIGN;
  public static final TextAttributesKey AOP_PARENTHS = SyntaxHighlighterColors.PARENTHS;
  public static final TextAttributesKey AOP_DOT = SyntaxHighlighterColors.DOT;
  public static final TextAttributesKey AOP_IDENTIFIER = HighlighterColors.TEXT;

  private static final Map<IElementType,TextAttributesKey> ourMap;

  static {
    ourMap = new THashMap<IElementType, TextAttributesKey>();
    fillMap(ourMap, AOP_KEYWORD, AOP_THROWS);
    fillMap(ourMap, AOP_KEYWORD, AOP_NEW);
    fillMap(ourMap, AOP_KEYWORD, AOP_MODIFIER);
    fillMap(ourMap, AOP_KEYWORD, AOP_BOOLEAN_LITERAL);
    fillMap(ourMap, AOP_PRIMITIVE_TYPES, AOP_KEYWORD);

    fillMap(ourMap, AOP_DOT, AopElementTypes.AOP_DOT, AOP_DOT_DOT, AOP_VARARGS);
    fillMap(ourMap, AOP_PARENTHS, AOP_LEFT_PAR, AOP_RIGHT_PAR, AOP_LT, AOP_GT);

    fillMap(ourMap, AOP_OPERATION_SIGN, AOP_ASTERISK);
    fillMap(ourMap, AOP_LOGICAL_OPS, AOP_OPERATION_SIGN);

    fillMap(ourMap, AOP_IDENTIFIER, AopElementTypes.AOP_IDENTIFIER);
  }

  @NotNull
  public Lexer getHighlightingLexer() {
    return new AopLexer();
  }

  @NotNull
  public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
    if (tokenType instanceof AopPointcutDesignatorTokenType) {
      return new TextAttributesKey[]{AOP_KEYWORD};
    }

    return pack(ourMap.get(tokenType));
  }
}
