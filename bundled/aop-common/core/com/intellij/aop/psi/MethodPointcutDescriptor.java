/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import org.jetbrains.annotations.NonNls;
import com.intellij.lang.pratt.PrattBuilder;
import com.intellij.lang.pratt.MutableMarker;
import static com.intellij.aop.psi.AopElementTypes.AOP_LEFT_PAR;
import static com.intellij.aop.psi.AopElementTypes.AOP_AT;
import static com.intellij.aop.psi.AopElementTypes.AOP_NOT;
import static com.intellij.aop.psi.AopElementTypes.AOP_NOT_EXPRESSION;
import static com.intellij.aop.psi.AopElementTypes.AOP_MODIFIER_LIST;
import static com.intellij.aop.psi.AopElementTypes.AOP_CONSTRUCTOR_REFERENCE_EXPRESSION;
import static com.intellij.aop.psi.AopElementTypes.AOP_REFERENCE_HOLDER;
import static com.intellij.aop.psi.AopElementTypes.AOP_MEMBER_REFERENCE_EXPRESSION;
import static com.intellij.aop.psi.AopElementTypes.AOP_RIGHT_PAR;
import static com.intellij.aop.psi.AopElementTypes.AOP_THROWS;
import static com.intellij.aop.psi.AopElementTypes.AOP_COMMA;
import static com.intellij.aop.psi.AopElementTypes.AOP_THROWS_LIST;
import static com.intellij.aop.psi.AopPrattParser.parseAnnotations;
import static com.intellij.aop.psi.AopPrattParser.TYPE_PATTERN;
import com.intellij.aop.AopBundle;
import com.intellij.psi.tree.IElementType;

/**
 * @author peter
*/
public abstract class MethodPointcutDescriptor extends PointcutDescriptor{
  private final boolean myConstructorOnly;

  protected MethodPointcutDescriptor(@NonNls final String tokenText, final boolean constructorOnly) {
    super(tokenText);
    myConstructorOnly = constructorOnly;
  }

   public void parseToken(final PrattBuilder builder) {
     if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
       parseAnnotationsWithModifiers(builder);

       final MutableMarker type = builder.mark();
       final String message = myConstructorOnly
                              ? AopBundle.message("error.constructor.pattern.expected")
                              : AopBundle.message("error.method.return.type.expected");
       final IElementType result = builder.parseChildren(TYPE_PATTERN, message);
       final boolean isConstructor = result == AOP_CONSTRUCTOR_REFERENCE_EXPRESSION;
       if (!isConstructor) {
         if (myConstructorOnly) {
           builder.error(AopBundle.message("error.0.expected", ".new"));
           type.finish(AOP_CONSTRUCTOR_REFERENCE_EXPRESSION);
         } else {
           type.finish(AOP_REFERENCE_HOLDER);
         }
       } else {
         type.drop();
       }

       if (!isConstructor && !myConstructorOnly) {
         final MutableMarker methodRef = builder.mark();
         builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.method.name.pattern.expected"));
         methodRef.finish(AOP_MEMBER_REFERENCE_EXPRESSION);
       }

       if (builder.assertToken(AOP_LEFT_PAR, AopBundle.message("error.0.expected", "("))) {
         AopPointcutTypes.parseParameterList(builder, TYPE_PATTERN, AopBundle.message("error.method.args.pattern.expected"));

         if (builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"))) {
           if (builder.isToken(AOP_THROWS)) {
             final MutableMarker throwsList = builder.mark();
             builder.advance();
             while (true) {
               final MutableMarker exc = builder.mark();
               builder.parseChildren(TYPE_PATTERN, AopBundle.message("error.type.name.pattern.expected"));
               exc.finish(AOP_REFERENCE_HOLDER);
               if (!builder.checkToken(AOP_COMMA)) break;
             }
             throwsList.finish(AOP_THROWS_LIST);
           }
         }
       }
       builder.assertToken(AOP_RIGHT_PAR, AopBundle.message("error.0.expected", ")"));
     }

    }

  public static void parseAnnotationsWithModifiers(final PrattBuilder builder) {
    if (builder.isToken(AOP_AT) || builder.isToken(AOP_NOT)) {
      parseAnnotations(builder);
    }

    final MutableMarker modList = builder.mark();
    while (true) {
      if (AOP_NOT == builder.getTokenType()) {
        final MutableMarker not = builder.mark();
        builder.advance();
        if (builder.isToken(AopElementTypes.AOP_MODIFIER)) {
          builder.advance();
          not.finish(AOP_NOT_EXPRESSION);
        } else {
          not.rollback();
          break;
        }
      } else if (builder.isToken(AopElementTypes.AOP_MODIFIER)) {
        builder.advance();
      } else {
        break;
      }
    }
    modList.finish(AOP_MODIFIER_LIST);
  }
}
