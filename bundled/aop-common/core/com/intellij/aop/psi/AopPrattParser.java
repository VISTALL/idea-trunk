/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.aop.AopBundle;
import static com.intellij.aop.psi.AopElementTypes.*;
import com.intellij.lang.pratt.*;
import static com.intellij.lang.pratt.PathPattern.path;
import static com.intellij.lang.pratt.PrattRegistry.registerParser;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.IElementTypePattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.Nullable;

/**
 * @author peter
 */
public class AopPrattParser extends PrattParser {
  static final int LOGIC = 30;
  static final int POINTCUT = 40;
  static final int TYPE_PATTERN = 70;
  static final int SIMPLE_TYPE = 90;
  static final int ATOM = 100;

  private static final IElementTypePattern REFERENCE_QUALIFIER = PsiJavaPatterns.elementType().oneOf(
      AOP_REFERENCE_EXPRESSION, AOP_PARENTHESIZED_EXPRESSION, AOP_SUBTYPE_EXPRESSION, AOP_ARRAY_EXPRESSION, AOP_GENERIC_TYPE_EXPRESSION);
  private static final ElementPattern<IElementType> ARRAYABLE = PsiJavaPatterns.or(REFERENCE_QUALIFIER, PsiJavaPatterns.elementType().oneOf(AOP_PRIMITIVE_TYPE_EXPRESSION));
  private static final ElementPattern<IElementType> ANY_TYPE_PATTERN = PsiJavaPatterns.or(ARRAYABLE, PsiJavaPatterns.elementType().oneOf(AOP_BINARY_EXPRESSION, AOP_NOT_EXPRESSION, AOP_PARENTHESIZED_EXPRESSION));

  static {
    AopPointcutTypes.getPointcutTokens(); //to initialize

    registerParser(AOP_IDENTIFIER, POINTCUT, path().up(), TokenParser.postfix(AOP_REFERENCE_EXPRESSION));
    registerParser(AOP_DOT, POINTCUT, path().left(AOP_REFERENCE_EXPRESSION).up(), new ReducingParser() {
      public IElementType parseFurther(final PrattBuilder builder) {
        builder.assertToken(AOP_IDENTIFIER, AopBundle.message("error.id.expected"));
        return AOP_REFERENCE_EXPRESSION;
      }
    });
    registerParser(AOP_LEFT_PAR, POINTCUT, path().left(AOP_REFERENCE_EXPRESSION).up(), new ReducingParser() {
      public IElementType parseFurther(final PrattBuilder builder) {
        final MutableMarker paramList = builder.mark();
        if (!builder.isToken(AOP_RIGHT_PAR)) {
          while (true) {
            final MutableMarker refHolder = builder.mark();
            builder.parseChildren(SIMPLE_TYPE, AopBundle.message("error.pointcut.arguments.expected"));
            refHolder.finish(AOP_REFERENCE_HOLDER);
            if (!builder.checkToken(AOP_COMMA)) break;
          }
        }
        paramList.finish(AOP_PARAMETER_LIST);
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        return AOP_POINTCUT_REFERENCE;
      }
    });

    for (IElementType type : AOP_PRIMITIVE_TYPES.getTypes()) {
      registerParser(type, ATOM + 1, path().up(), TokenParser.postfix(AOP_PRIMITIVE_TYPE_EXPRESSION));
    }

    registerParser(AOP_IDENTIFIER, ATOM + 1, path().up(), new TokenParser() {
      public final boolean parseToken(final PrattBuilder builder) {
        builder.reduce(parsePatternPart(builder));
        return true;
      }
    });
    registerParser(AOP_ASTERISK, ATOM + 1, path().up(), new TokenParser() {
      public final boolean parseToken(final PrattBuilder builder) {
        builder.reduce(parsePatternPart(builder));
        return true;
      }
    });
    final TokenParser pathSeparator = new TokenParser() {
      public final boolean parseToken(final PrattBuilder builder) {
        builder.advance();

        if (builder.isToken(AOP_NEW)) {
          builder.advance();
          builder.reduce(AOP_CONSTRUCTOR_REFERENCE_EXPRESSION);
          return false;
        }

        if (!builder.isToken(AOP_ASTERISK) && !builder.isToken(AOP_IDENTIFIER)) {
          builder.error(AopBundle.message("error.id.expected"));
          return true;
        }
        builder.reduce(parsePatternPart(builder));
        return true;
      }
    };
    registerParser(AOP_DOT, SIMPLE_TYPE + 1, path().left(REFERENCE_QUALIFIER).up(), pathSeparator);
    registerParser(AOP_DOT_DOT, SIMPLE_TYPE - 1, path().left(REFERENCE_QUALIFIER).up(), pathSeparator);
    registerParser(ANNO_WHITE_SPACE, Integer.MAX_VALUE, new TokenParser() {
      public boolean parseToken(final PrattBuilder builder) {
        builder.advance();
        return false;
      }
    });

    registerParser(AOP_OR, LOGIC + 1, path().left().up(), new ReducingParser() {
      @Nullable
      public IElementType parseFurther(final PrattBuilder builder) {
        parsePointcut(builder, builder.createChildBuilder(LOGIC + 1));
        return AOP_POINTCUT_BINARY_EXPRESSION;
      }
    });
    registerParser(AOP_AND, LOGIC + 2, path().left().up(), new ReducingParser() {
      @Nullable
      public IElementType parseFurther(final PrattBuilder builder) {
        parsePointcut(builder, builder.createChildBuilder(LOGIC + 2));
        return AOP_POINTCUT_BINARY_EXPRESSION;
      }
    });
    registerParser(AOP_NOT, LOGIC + 3, path().up(), new ReducingParser() {
      @Nullable
      public IElementType parseFurther(final PrattBuilder builder) {
        parsePointcut(builder, builder.createChildBuilder(LOGIC + 2));
        return AOP_POINTCUT_NOT_EXPRESSION;
      }
    });

    registerParser(AOP_OR, TYPE_PATTERN + 1, path().left(ANY_TYPE_PATTERN).up(), TokenParser.infix(TYPE_PATTERN + 1, AOP_BINARY_EXPRESSION));
    registerParser(AOP_AND, TYPE_PATTERN + 2, path().left(ANY_TYPE_PATTERN).up(), TokenParser.infix(TYPE_PATTERN + 2, AOP_BINARY_EXPRESSION));
    registerParser(AOP_NOT, TYPE_PATTERN + 3, path().up(), new TokenParser() {
      public boolean parseToken(final PrattBuilder builder) {
        final MutableMarker annotatedType = builder.mark();
        final MutableMarker not = builder.mark();
        builder.advance();
        if (builder.isToken(AOP_AT)) {
          not.rollback();
          parseAnnotations(builder);

          builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
          annotatedType.finish(AOP_ANNOTATED_TYPE_EXPRESSION);
          return true;
        }
        annotatedType.drop();

        builder.parseChildren(TYPE_PATTERN + 2, AopBundle.message("error.type.name.pattern.expected"));
        not.finish(AOP_NOT_EXPRESSION);
        return true;
      }
    });

    registerParser(AOP_AT, TYPE_PATTERN + 5, path().up(), new TokenParser() {
      public boolean parseToken(final PrattBuilder builder) {
        final MutableMarker annotatedType = builder.mark();
        parseAnnotations(builder);

        builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
        annotatedType.finish(AOP_ANNOTATED_TYPE_EXPRESSION);
        return true;
      }
    });

    registerParser(AOP_LEFT_PAR, POINTCUT + 1, path().up(), new ReducingParser() {
      public IElementType parseFurther(final PrattBuilder builder) {
        parsePointcut(builder, builder.createChildBuilder(0));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        return AOP_POINTCUT_PARENTHESIZED_EXPRESSION;
      }
    });
    registerParser(AOP_LEFT_PAR, ATOM + 1, path().up(), new ReducingParser() {
      public IElementType parseFurther(final PrattBuilder builder) {
        builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
        builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
        return AOP_PARENTHESIZED_EXPRESSION;
      }
    });

    registerParser(AOP_LT, TYPE_PATTERN + 11, path().left(REFERENCE_QUALIFIER).up(), new TokenParser() {
      public boolean parseToken(final PrattBuilder builder) {
        final MutableMarker typeParamList = builder.mark();
        builder.advance();
        while (true) {
          final MutableMarker holder = builder.mark();
          final MutableMarker wildcard = builder.mark();
          if (builder.checkToken(AOP_QUESTION)) {
            if (builder.checkToken(AOP_EXTENDS) || builder.checkToken(AOP_SUPER)) {
              builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
            }
            wildcard.finish(AOP_WILDCARD_EXPRESSION);
          } else {
            builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
            wildcard.drop();
          }
          holder.finish(AOP_REFERENCE_HOLDER);

          if (!builder.checkToken(AOP_COMMA)) break;
        }
        builder.assertToken(AOP_GT, AopBundle.message("error.0.expected", ">"));
        typeParamList.finish(AOP_TYPE_PARAMETER_LIST);
        builder.reduce(AOP_GENERIC_TYPE_EXPRESSION);
        return true;
      }
    });
    registerParser(AOP_PLUS, TYPE_PATTERN + 10, path().left(REFERENCE_QUALIFIER).up(), TokenParser.postfix(AOP_SUBTYPE_EXPRESSION));
    registerParser(AOP_BRACES, TYPE_PATTERN + 9, path().left(ARRAYABLE).up(), TokenParser.postfix(AOP_ARRAY_EXPRESSION));
    registerParser(AOP_BRACES, TYPE_PATTERN + 9, path().left(ARRAYABLE).up(), TokenParser.postfix(AOP_ARRAY_EXPRESSION));
    registerParser(AOP_VARARGS, TYPE_PATTERN + 9, path().left(ARRAYABLE).up(), TokenParser.postfix(AOP_ARRAY_EXPRESSION));

  }

  static boolean parseAnnotations(final PrattBuilder builder) {
    MutableMarker annoHolder = builder.mark();
    boolean first = true;
    while (true) {
      if (builder.isToken(AOP_NOT)) {
        final MutableMarker not = builder.mark();
        builder.advance();
        if (!builder.isToken(AOP_AT)) {
          not.rollback();
          annoHolder.drop();
          return !first;
        }
        parseSingleAnnotation(builder);
        not.finish(AOP_NOT_EXPRESSION);
      } else if (builder.isToken(AOP_AT)) {
        parseSingleAnnotation(builder);
      } else {
        break;
      }

      first = false;
    }
    annoHolder.finish(AOP_ANNOTATION_HOLDER);
    return true;
  }

  private static void parseSingleAnnotation(final PrattBuilder builder) {
    final MutableMarker anno = builder.mark();
    builder.advance();
    final MutableMarker refHolder = builder.mark();
    builder.parseChildren(SIMPLE_TYPE, AopBundle.message("error.anno.expected"));
    refHolder.finish(AOP_REFERENCE_HOLDER);

    if (builder.checkToken(AOP_LEFT_PAR) && !builder.checkToken(AOP_RIGHT_PAR)) {
      final MutableMarker params = builder.mark();
      int depth = 1;
      while (depth != 0 && !builder.isEof()) {
        builder.advance();
        if (builder.isToken(AOP_LEFT_PAR)) depth++;
        else if (builder.isToken(AOP_LEFT_BRACE)) depth++;
        else if (builder.isToken(AOP_RIGHT_PAR)) depth--;
        else if (builder.isToken(AOP_RIGHT_BRACE)) depth--;
      }
      params.finish(AOP_ANNOTATION_VALUES);
      builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
    }

    anno.finish(AOP_ANNOTATION_EXPRESSION);
  }

  static void parsePointcut(final PrattBuilder builder, final PrattBuilderFacade child) {
    final MutableMarker marker = builder.mark();
    if (child.expecting(AopBundle.message("error.pointcut.expression.expected")).parse() == AOP_REFERENCE_EXPRESSION) {
      builder.error(AopBundle.message("error.0.expected", "("));
      marker.finish(AOP_POINTCUT_REFERENCE);
      return;
    }
    marker.drop();
  }

  private static boolean noGapAdvance(final PrattBuilder builder) {
    final int offset = builder.getCurrentOffset();
    final String s = builder.getTokenText();
    if (s == null) return false;

    final int tokenLength = s.length();
    builder.advance();
    return builder.getCurrentOffset() == offset + tokenLength;
  }

  public static IElementType parsePatternPart(final PrattBuilder builder) {
    boolean expectAsterisk = builder.getTokenType() == AOP_IDENTIFIER;
    while (noGapAdvance(builder)) {
      if (expectAsterisk) {
        if (!builder.isToken(AOP_ASTERISK)) break;
        if (!noGapAdvance(builder)) break;
      }
      expectAsterisk = true;
      if (!builder.isToken(AOP_IDENTIFIER)) break;
    }
    return AOP_REFERENCE_EXPRESSION;
  }

  @Override
  protected void parse(final PrattBuilderImpl builder) {
    parsePointcut(builder, builder);
    while (!builder.isEof()) builder.advance();
  }
}
