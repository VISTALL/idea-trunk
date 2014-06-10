 /* It's an automatically generated code. Do not modify it. */
package com.intellij.aop.lexer;

import com.intellij.aop.psi.*;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

%%

%{
  public _AopLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class _AopLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%eof{  return;
%eof}

IDENTIFIER=[:jletter:][:jletterdigit:]*
WS=[\ \n\r\t\f]
WHITE_SPACE={WS}+

MODIFIER = "public"|"private"|"protected"|"synchronized"|"static"|"final"

%state PATH_ELEMENT
%state AFTER_QUESTION
%state AFTER_DOT
%state ANNO_PATTERN

%%
<YYINITIAL> "@target" {yybegin(YYINITIAL); return AopElementTypes.AOP_IDENTIFIER; }
<YYINITIAL> "@within" {yybegin(YYINITIAL); return AopElementTypes.AOP_IDENTIFIER; }
<YYINITIAL> "@args" {yybegin(YYINITIAL); return AopElementTypes.AOP_IDENTIFIER; }
<YYINITIAL> "@annotation" {yybegin(YYINITIAL); return AopElementTypes.AOP_IDENTIFIER; }
<YYINITIAL> "@this" {yybegin(YYINITIAL); return AopElementTypes.AOP_IDENTIFIER; }

"..." {yybegin(YYINITIAL); return AopElementTypes.AOP_VARARGS; }

"@" {yybegin(ANNO_PATTERN); return AopElementTypes.AOP_AT; }
"*" { if (yystate() != ANNO_PATTERN) { yybegin(PATH_ELEMENT); } return AopElementTypes.AOP_ASTERISK; }
".." { if (yystate() != ANNO_PATTERN) { yybegin(AFTER_DOT); } return AopElementTypes.AOP_DOT_DOT; }
"." { if (yystate() != ANNO_PATTERN) { yybegin(AFTER_DOT); } return AopElementTypes.AOP_DOT; }
"(" {yybegin(YYINITIAL); return AopElementTypes.AOP_LEFT_PAR; }
")" {yybegin(YYINITIAL); return AopElementTypes.AOP_RIGHT_PAR; }
"," {yybegin(YYINITIAL); return AopElementTypes.AOP_COMMA; }
"<" {yybegin(YYINITIAL); return AopElementTypes.AOP_LT; }
">" {yybegin(YYINITIAL); return AopElementTypes.AOP_GT; }
"!" {yybegin(YYINITIAL); return AopElementTypes.AOP_NOT; }
"&&" {yybegin(YYINITIAL); return AopElementTypes.AOP_AND; }
"||" {yybegin(YYINITIAL); return AopElementTypes.AOP_OR; }
"+" {yybegin(YYINITIAL); return AopElementTypes.AOP_PLUS; }
"[]" {yybegin(YYINITIAL); return AopElementTypes.AOP_BRACES; }
"?" {yybegin(AFTER_QUESTION); return AopElementTypes.AOP_QUESTION; }
<AFTER_QUESTION> "extends" {yybegin(YYINITIAL); return AopElementTypes.AOP_EXTENDS; }
<AFTER_QUESTION> "super" {yybegin(YYINITIAL); return AopElementTypes.AOP_SUPER; }

<AFTER_DOT> "new" {yybegin(YYINITIAL); return AopElementTypes.AOP_NEW; }

<PATH_ELEMENT,AFTER_DOT> {IDENTIFIER} { return AopElementTypes.AOP_IDENTIFIER; }
{IDENTIFIER} / [\.\*] { return AopElementTypes.AOP_IDENTIFIER; }

{MODIFIER} {yybegin(YYINITIAL); return AopElementTypes.AOP_MODIFIER; }
"throws" {yybegin(YYINITIAL); return AopElementTypes.AOP_THROWS; }
"true"|"false" {yybegin(YYINITIAL); return AopElementTypes.AOP_BOOLEAN_LITERAL; }

{IDENTIFIER} { return AopElementTypes.AOP_IDENTIFIER; }

<ANNO_PATTERN> {WHITE_SPACE} { yybegin(YYINITIAL); return AopElementTypes.ANNO_WHITE_SPACE; }
<AFTER_DOT> {WHITE_SPACE} { return AopElementTypes.WHITE_SPACE; }
<AFTER_QUESTION> {WHITE_SPACE} { return AopElementTypes.WHITE_SPACE; }
{WHITE_SPACE} { yybegin(YYINITIAL); return AopElementTypes.WHITE_SPACE; }
. { yybegin(YYINITIAL); return AopElementTypes.BAD_CHARACTER; }

