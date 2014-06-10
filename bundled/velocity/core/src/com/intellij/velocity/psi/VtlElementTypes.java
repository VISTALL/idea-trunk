/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.velocity.VelocityBundle;
import static com.intellij.velocity.psi.VtlExpressionTypeCalculator.*;
import com.intellij.velocity.psi.parsers.*;
import com.intellij.lang.PsiBuilder;

/**
 * @author Alexey Chmutov
 */
public interface VtlElementTypes extends TokenType, VtlCompositeElementTypes {

    IElementType DOLLAR = new IElementType("DOLLAR", VtlLanguage.INSTANCE);
    IElementType SHARP = new IElementType("SHARP", VtlLanguage.INSTANCE);

    VtlTokenType SHARP_SET = new VtlCompositeStarterTokenType("#SET", SetDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_IF = new VtlCompositeStarterTokenType("#IF", IfDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_ELSEIF = new VtlCompositeStarterTokenType("#ELSEIF", ElseifDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_ELSE = new VtlCompositeStarterTokenType("#ELSE", ElseDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_END = new VtlTokenType("#END");
    VtlTokenType SHARP_FOREACH = new VtlCompositeStarterTokenType("#FOREACH", ForeachDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_BREAK = new VtlCompositeStarterTokenType("#BREAK", new CompositeBodyParser() {
        public void parseBody(PsiBuilder builder, PsiBuilder.Marker bodyMarker) {
            bodyMarker.done(DIRECTIVE_BREAK);
        }
    });
    VtlTokenType SHARP_INCLUDE = new VtlCompositeStarterTokenType("#INCLUDE", IncludeDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_PARSE = new VtlCompositeStarterTokenType("#PARSE", ParseDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_STOP = new VtlTokenType("#STOP");
    VtlTokenType SHARP_MACRODECL = new VtlCompositeStarterTokenType("#MACRO_DECL", MacroDeclDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_MACROCALL = new VtlCompositeStarterTokenType("#MACRO_CALL", MacroCallDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_LITERAL = new VtlCompositeStarterTokenType("#LITERAL", LiteralDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_EVALUATE = new VtlCompositeStarterTokenType("#EVALUATE", EvaluateDirectiveBodyParser.INSTANCE);
    VtlTokenType SHARP_DEFINE = new VtlCompositeStarterTokenType("#DEFINE", DefineDirectiveBodyParser.INSTANCE);

    VtlTokenType START_REFERENCE = new VtlCompositeStarterTokenType("$", InterpolationBodyParser.INSTANCE);
    VtlTokenType START_REF_FORMAL = new VtlCompositeStarterTokenType("${", InterpolationFormalBodyParser.INSTANCE);

    VtlTokenType TEMPLATE_TEXT = new VtlTokenType("TEMPLATE_TEXT");

    VtlTokenType MULTILINE_COMMENT = new VtlCommentType("MULTILINE_COMMENT", 2, 2);
    VtlTokenType DOCUMENTING_COMMENT = new VtlCommentType("DOCUMENTING_COMMENT", 3, 2);
    VtlTokenType ONELINE_COMMENT = new VtlCommentType("ONELINE_COMMENT", 2, 0);

    VtlTokenType IN = new VtlTokenType("IN");

    VtlTokenType COMMA = new VtlTokenType(",");
    VtlTokenType COLON = new VtlTokenType(":");
    VtlTokenType SEMICOLON = new VtlTokenType(";");

    VtlTokenType OR = new VtlOperatorTokenType("OR", BOOLEAN_CALCULATOR);
    VtlTokenType AND = new VtlOperatorTokenType("AND", BOOLEAN_CALCULATOR);

    VtlTokenType EQ = new VtlOperatorTokenType("EQ", BOOLEAN_CALCULATOR);
    VtlTokenType NEQ = new VtlOperatorTokenType("NEQ", BOOLEAN_CALCULATOR);
    VtlTokenType LT = new VtlOperatorTokenType("LT", BOOLEAN_CALCULATOR);
    VtlTokenType LTE = new VtlOperatorTokenType("LTE", BOOLEAN_CALCULATOR);
    VtlTokenType GT = new VtlOperatorTokenType("GT", BOOLEAN_CALCULATOR);
    VtlTokenType GTE = new VtlOperatorTokenType("GTE", BOOLEAN_CALCULATOR);

    VtlTokenType RANGE = new VtlTokenType("RANGE");

    VtlTokenType ASSIGN = new VtlTokenType("=");

    VtlTokenType PLUS = new VtlOperatorTokenType("+", PLUS_CALCULATOR);
    VtlTokenType MINUS = new VtlOperatorTokenType("-", MINUS_CALCULATOR);
    VtlTokenType ASTERISK = new VtlOperatorTokenType("*", MULTIPLICATIVE_CALCULATOR);
    VtlTokenType DIVIDE = new VtlOperatorTokenType("/", MULTIPLICATIVE_CALCULATOR);
    VtlTokenType PERCENT = new VtlOperatorTokenType("%", MULTIPLICATIVE_CALCULATOR);

    VtlTokenType EXCLAIM = new VtlOperatorTokenType("!", BOOLEAN_CALCULATOR);
    VtlTokenType QUESTION = new VtlTokenType("?");
    VtlTokenType DOT = new VtlTokenType(".");

    VtlTokenType LEFT_BRACKET = new VtlTokenType("[");
    VtlTokenType LEFT_BRACE = new VtlTokenType("{");
    VtlTokenType LEFT_PAREN = new VtlTokenType("(");
    VtlTokenType LEFT_BRACE_IN_EXPR = new VtlTokenType("{");
    VtlTokenType RIGHT_BRACKET = new VtlTokenType("]");
    VtlTokenType RIGHT_PAREN = new VtlTokenType(")");
    VtlTokenType RIGHT_BRACE = new VtlTokenType("}");
    VtlTokenType RIGHT_BRACE_IN_EXPR = new VtlTokenType("}");


    VtlTokenType SINGLE_QUOTE = new VtlTokenType("'");
    VtlTokenType DOUBLE_QUOTE = new VtlTokenType("\"");

    VtlTokenType BOOLEAN = new VtlTokenType("BOOLEAN");

    VtlTokenType INTEGER = new VtlTokenType(VelocityBundle.message("number"));

    VtlTokenType DOUBLE = new VtlTokenType(VelocityBundle.message("number"));

    VtlTokenType V_IDENT = new VtlTokenType("V_IDENT");

    VtlTokenType IDENTIFIER = new VtlTokenType(VelocityBundle.message("identifier"));

    VtlTokenType JAVA_DOT = new VtlTokenType("JAVA_DOT");

    VtlTokenType STRING_TEXT = new VtlTokenType("STRING_TEXT");

    VtlTokenType CHAR_ESCAPE = new VtlTokenType("CHAR_ESCAPE");

    TokenSet LOGICAL_OPERATIONS = TokenSet.create(AND, OR);

    TokenSet RELATIONAL_OPERATIONS = TokenSet.create(LT, GT, LTE, GTE, EQ, NEQ);

    TokenSet ADDITIVE_OPERATIONS = TokenSet.create(PLUS, MINUS);

    TokenSet MULTIPLICATIVE_OPERATIONS = TokenSet.create(ASTERISK, DIVIDE, PERCENT);

    TokenSet UNARY_OPERATIONS = TokenSet.create(EXCLAIM, MINUS);

    TokenSet OPERATIONS = TokenSet.orSet(LOGICAL_OPERATIONS, RELATIONAL_OPERATIONS, ADDITIVE_OPERATIONS, LOGICAL_OPERATIONS, MULTIPLICATIVE_OPERATIONS, UNARY_OPERATIONS);

    TokenSet DIR_STARTERS = TokenSet.create(SHARP_SET, SHARP_IF, SHARP_FOREACH, SHARP_ELSEIF, SHARP_INCLUDE, SHARP_MACRODECL, SHARP_PARSE, SHARP_LITERAL, SHARP_EVALUATE, SHARP_DEFINE);

    TokenSet COMMENTS = TokenSet.create(MULTILINE_COMMENT, ONELINE_COMMENT, DOCUMENTING_COMMENT);
}
