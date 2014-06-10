/* It's an automatically generated code. Do not modify it.   * 
** This file needs to be built with JFlex 1.4.2 or higher!!! */

package com.intellij.velocity.lexer;

import static com.intellij.velocity.psi.VtlElementTypes.*;
import com.intellij.velocity.psi.*;
import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

%%

%{
  public _VtlLexer() {
    this((java.io.Reader)null);
  }

  private class StateHolder extends ListElement {
    public final Integer transitionalState;

    public StateHolder(@NotNull Integer state, @Nullable Integer transitionalState, @Nullable StateHolder prevElement) {
      super(state, prevElement);
      this.transitionalState = transitionalState;
    }
  }

  private class ListElement {
    public final Integer data;
    public final ListElement prevElement;

    public ListElement(@NotNull Integer data, @Nullable ListElement prevElement) {
      this.data = data;
      this.prevElement = prevElement;
    }

    public String toString() {
      return " [d" + data + " " + (prevElement == null ? "" : prevElement) + "] ";
    }
  }

  private class ParensState {
    int parensLevel = 0;
    int bracesLevel = 0;
    ListElement specialBraceLevel = null;

    public String toString() {
      return " [p" + parensLevel + " b" + bracesLevel + " " + (specialBraceLevel == null ? "" : specialBraceLevel) + "] ";
    }
  }

  private int commentLevel = 0;
  private StateHolder savedJflexState = null;
  private ParensState storedParensState = null;
  private ParensState parensState = new ParensState();

  private void resetAll() {
    zzAtBOL = true;
    zzAtEOF = false;
    zzBuffer = "";
    zzBufferArray = null;
    zzCurrentPos = 0;
    zzEndRead = 0;
    zzFin = new boolean [ZZ_BUFFERSIZE+1];
    zzLexicalState = 0;
    zzMarkedPos = 0;
    zzPushbackPos  = 0;
    zzStartRead = 0;
    zzState = 0;
    commentLevel = 0;
    savedJflexState = null;
    storedParensState = null;
    parensState = new ParensState();
  }

  void leftParen() {
    parensState.parensLevel++;
  }

  boolean rightParen() {
    return --parensState.parensLevel == 0;
  }

  void saveJflexStateAndBegin(int stateToStart) {
    saveJflexStateAndBegin(null, stateToStart);
  }

  void saveJflexStateAndBegin(@Nullable Integer transitionalState, int stateToStart) {
    savedJflexState = new StateHolder(yystate(), transitionalState, savedJflexState);
    yybegin(stateToStart);
  }

  void restoreJflexState() {
    assert savedJflexState != null;
    yybegin(savedJflexState.data.intValue());
    savedJflexState = (StateHolder)savedJflexState.prevElement;
  }

  void restoreThruTransitionalState() {
    assert savedJflexState != null;
    final Integer transitional = savedJflexState.transitionalState;
    if (transitional != null) {
      yybegin(transitional.intValue());
    }
    else {
      restoreJflexState();
    }
  }

  void openingDoubleQuote() {
//    System.out.println(zzCurrentPos + ": -> opening: " + parensState + " jflex: " + savedJflexState);
    if (storedParensState == null) {
      storedParensState = parensState;
      parensState = new ParensState();
      yybegin(DOUBLE_QUOTED);
    } 
    else {
      // doublequoted text cannot contain double quote, so this one is considered as closing one
      assert savedJflexState != null;
      savedJflexState = (StateHolder)savedJflexState.prevElement;
      closingDoubleQuote();
    }
//    System.out.println("  <- opening: " + parensState + " jflex: " + savedJflexState);
  }

  void openingDoubleQuoteSimple() {
//    System.out.println(zzCurrentPos + ": -> opening simple: " + parensState + " jflex: " + savedJflexState);
    if (storedParensState == null) {
      yybegin(DOUBLE_QUOTED_SIMPLE);
    } 
    else {
      // doublequoted text cannot contain double quote, so this one is considered as closing one
      assert savedJflexState != null;
      savedJflexState = (StateHolder)savedJflexState.prevElement;
      closingDoubleQuote();
    }
//    System.out.println("  <- opening simple: " + parensState + " jflex: " + savedJflexState);
  }

  void closingDoubleQuote() {
//    System.out.println(zzCurrentPos + ": -> closing: " + parensState + " jflex: " + savedJflexState);
    assert storedParensState != null;
    parensState = storedParensState;
    storedParensState = null;
    yybegin(PARENS); 
//    System.out.println("  <- closing: " + parensState + " jflex: " + savedJflexState);
  }

  void dollarLeftBrace() {
    parensState.specialBraceLevel = new ListElement(parensState.bracesLevel++, parensState.specialBraceLevel);
  }

  void leftBrace() {
    parensState.bracesLevel++;
  }

  boolean rightBrace() {
    parensState.bracesLevel--;
    assert parensState.specialBraceLevel == null || parensState.specialBraceLevel.data <= parensState.bracesLevel;
    if(parensState.specialBraceLevel != null && parensState.specialBraceLevel.data == parensState.bracesLevel) {
      parensState.specialBraceLevel = parensState.specialBraceLevel.prevElement;
      return true;
    }
    return false;
  }

%}

%public
%class _VtlLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode
%eof{  return;
%eof}

LINE_TERMINATOR = \r|\n|\r\n
WS1 = [ ]
ANY_WS_CHAR = {LINE_TERMINATOR}|{WS1}|[\t\f]
WHITE_SPACE = {ANY_WS_CHAR}+

NUMB = [:digit:]+
DOUBLE = \$?-?{NUMB}?(\.){NUMB}
INTEGER = \$?-?{NUMB}

LETTER = [:letter:]|_
J_LETTERDIGIT = {LETTER}|[:digit:]
V_LETTERDIGIT = {J_LETTERDIGIT}|-
V_IDENT = {LETTER}{V_LETTERDIGIT}*
J_IDENT = {LETTER}{J_LETTERDIGIT}*

SHARP = #
DOLLAR = \$
SHARP_AS_SUCH = \\#
DOLLAR_AS_SUCH = \\\$
START_INTERPOL = {DOLLAR}(\!)?
START_INTERPOL_FORMAL = {DOLLAR}(\!)?\{

ONELINE_COMMENT_START = ##
MULTILINE_COMMENT_START = #\*
MULTILINE_COMMENT_END = \*#
DOCUMENTING_COMMENT_START = #\*\*
ONELINE_COMMENT = {ONELINE_COMMENT_START}[^\r\n]*{LINE_TERMINATOR}?
ONELINE_COMMENT_IN_STRING_TEXT = {ONELINE_COMMENT_START}[^\r\n\"]*({LINE_TERMINATOR})?
MULTILINE_COMMENT_MARK = {MULTILINE_COMMENT_START}|{MULTILINE_COMMENT_END}
COMMENT_BODY_TEXT = !([^]*{MULTILINE_COMMENT_MARK}[^]*)
COMMENT_BODY_IN_STRING_TEXT = !([^]*({MULTILINE_COMMENT_MARK}|\")[^]*)

KEYS_FOR_YYINITIAL = {SHARP}|{DOLLAR}|{SHARP_AS_SUCH}|{DOLLAR_AS_SUCH}
TEMPLATE_TEXT_IN_STRING_TEXT = !([^]*({KEYS_FOR_YYINITIAL}|\")[^]*)

%state USER_DIRECTIVE
%state INTERPOLATION
%state AFTER_V_IDENT
%state PARENS
%state RIGHT_BRACE_CONSUMER
%state DOUBLE_QUOTED
%state DOUBLE_QUOTED_SIMPLE
%state SINGLE_QUOTED
%state COMMENT_BODY
%state DOUBLE_QUOTED_COMMENT_BODY

%%

<YYINITIAL, DOUBLE_QUOTED> {
  (#elseif|#\{elseif\})/{WS1}*\(         { saveJflexStateAndBegin(PARENS); return SHARP_ELSEIF; }
  #elseif|#\{elseif\}                    { return SHARP_ELSEIF; }
  (#foreach|#\{foreach\})/{WS1}*\(       { saveJflexStateAndBegin(PARENS); return SHARP_FOREACH; }
  #foreach|#\{foreach\}                  { return SHARP_FOREACH; }
  (#define|#\{define\})/{WS1}*\(         { saveJflexStateAndBegin(PARENS); return SHARP_DEFINE; }
  #define|#\{define\}                    { return SHARP_DEFINE; }
  (#literal|#\{literal\})/{WS1}*\(       { saveJflexStateAndBegin(PARENS); return SHARP_LITERAL; }
  #literal|#\{literal\}                  { return SHARP_LITERAL; }
  (#evaluate|#\{evaluate\})/{WS1}*\(     { saveJflexStateAndBegin(PARENS); return SHARP_EVALUATE; }
  #evaluate|#\{evaluate\}                { return SHARP_EVALUATE; }
  (#include|#\{include\})/{WS1}*\(       { saveJflexStateAndBegin(PARENS); return SHARP_INCLUDE; }
  #include|#\{include\}                  { return SHARP_INCLUDE; }
  (#macro|#\{macro\})/{WS1}*\(           { saveJflexStateAndBegin(PARENS); return SHARP_MACRODECL; }
  #macro|#\{macro\}                      { return SHARP_MACRODECL; }
  (#parse|#\{parse\})/{WS1}*\(           { saveJflexStateAndBegin(PARENS); return SHARP_PARSE; }
  #parse|#\{parse\}                      { return SHARP_PARSE; }
  (#set|#\{set\})/{WS1}*\(               { saveJflexStateAndBegin(PARENS); return SHARP_SET; }
  #set|#\{set\}                          { return SHARP_SET; }
  (#if|#\{if\})/{WS1}*\(                 { saveJflexStateAndBegin(PARENS); return SHARP_IF; }
  #if|#\{if\}                            { return SHARP_IF; }
  (#else|#\{else\})/{WS1}*\(?            { return SHARP_ELSE; }
  (#end|#\{end\})/{WS1}*\(?              { return SHARP_END; }
  (#stop|#\{stop\})/{WS1}*\(?            { return SHARP_STOP; }
  (#break|#\{break\})/{WS1}*\(?          { return SHARP_BREAK; }

  #/({V_IDENT}|\{{V_IDENT}\}){WS1}*\(    { saveJflexStateAndBegin(USER_DIRECTIVE); return SHARP_MACROCALL; }
  #{V_IDENT}                             { return TEMPLATE_TEXT; }

  {START_INTERPOL_FORMAL}/{V_IDENT}      { saveJflexStateAndBegin(RIGHT_BRACE_CONSUMER, INTERPOLATION); return START_REF_FORMAL; }
  {START_INTERPOL}/{V_IDENT}             { saveJflexStateAndBegin(INTERPOLATION); return START_REFERENCE; }

  {KEYS_FOR_YYINITIAL}                        { return TEMPLATE_TEXT; }
}

<YYINITIAL> {
  {ONELINE_COMMENT}                           { return ONELINE_COMMENT; }
  {MULTILINE_COMMENT_START}                   { assert commentLevel == 0; commentLevel++; yybegin(COMMENT_BODY); return MULTILINE_COMMENT; }
  !([^]*{KEYS_FOR_YYINITIAL}[^]*)             { return TEMPLATE_TEXT; }
}

<DOUBLE_QUOTED> {
  {ONELINE_COMMENT_IN_STRING_TEXT}            { return ONELINE_COMMENT; }
  {MULTILINE_COMMENT_START}                   { assert commentLevel == 0; commentLevel++; yybegin(DOUBLE_QUOTED_COMMENT_BODY); return MULTILINE_COMMENT; }
  {TEMPLATE_TEXT_IN_STRING_TEXT}              { return TEMPLATE_TEXT; }
  \"                                          { closingDoubleQuote(); return DOUBLE_QUOTE; }
}

<DOUBLE_QUOTED_SIMPLE> {
  {TEMPLATE_TEXT_IN_STRING_TEXT}              { return STRING_TEXT; }
  \"                                          { yybegin(PARENS); return DOUBLE_QUOTE; }
}

<COMMENT_BODY> {
  {COMMENT_BODY_TEXT}{MULTILINE_COMMENT_START}             { commentLevel++; return MULTILINE_COMMENT; }
  {COMMENT_BODY_TEXT}{MULTILINE_COMMENT_END}?              { if(--commentLevel == 0) yybegin(YYINITIAL); return MULTILINE_COMMENT; }
}

<DOUBLE_QUOTED_COMMENT_BODY> {
  {COMMENT_BODY_IN_STRING_TEXT}{MULTILINE_COMMENT_START}   { commentLevel++; return MULTILINE_COMMENT; }
  {COMMENT_BODY_IN_STRING_TEXT}{MULTILINE_COMMENT_END}     { if(--commentLevel == 0) yybegin(DOUBLE_QUOTED); return MULTILINE_COMMENT; }
  {COMMENT_BODY_IN_STRING_TEXT}                            { commentLevel = 0; yybegin(DOUBLE_QUOTED); return MULTILINE_COMMENT; }
}

<SINGLE_QUOTED> {
  !([^]*(\')[^]*)                             { return STRING_TEXT; }
  \'                                          { yybegin(PARENS); return SINGLE_QUOTE; }
}

<INTERPOLATION> {
  {V_IDENT}/(\.{J_IDENT})                     { yybegin(AFTER_V_IDENT); return IDENTIFIER; }
  {V_IDENT}                                   { restoreJflexState(); return IDENTIFIER; }
  {V_IDENT}/(\})                              { restoreThruTransitionalState(); return IDENTIFIER; }
}

<AFTER_V_IDENT> {
  \./{J_IDENT}                                { return JAVA_DOT; }
  {J_IDENT}/(\.{J_IDENT})                     { return IDENTIFIER; }
  {J_IDENT}/(\()                              { yybegin(PARENS); return IDENTIFIER; }
  {J_IDENT}                                   { restoreJflexState(); return IDENTIFIER; }
  {J_IDENT}/(\})                              { restoreThruTransitionalState(); return IDENTIFIER; }
}

<RIGHT_BRACE_CONSUMER> {
  \}                                          { restoreJflexState(); return RIGHT_BRACE; }
}

<PARENS> {
  \.\.                                        { return RANGE; }
  \.                                          { return JAVA_DOT; }
  \"                                          { openingDoubleQuote(); return DOUBLE_QUOTE; }
  \"/{TEMPLATE_TEXT_IN_STRING_TEXT}\"         { openingDoubleQuoteSimple(); return DOUBLE_QUOTE; }
  \'                                          { yybegin(SINGLE_QUOTED); return SINGLE_QUOTE; }
  \+                                          { return PLUS; }
  \-                                          { return MINUS; }
  \*                                          { return ASTERISK; }
  \/                                          { return DIVIDE; }
  \%                                          { return PERCENT; }

  &&|and                                      { return AND; }
  \|\||or                                     { return OR; }
  \!|not                                      { return EXCLAIM; }
  ==|eq                                       { return EQ; }
  \!=|ne                                      { return NEQ; }
  \<|lt                                       { return LT; }
  \>|gt                                       { return GT; }
  \<=|le                                      { return LTE; }
  \>=|ge                                      { return GTE; }
  in                                          { return IN; }
  =                                           { return ASSIGN; }

  \[                                          { return LEFT_BRACKET; }
  \]                                          { return RIGHT_BRACKET; }
  \{                                          { leftBrace(); return LEFT_BRACE_IN_EXPR; }
  \}                                          { return rightBrace() ? RIGHT_BRACE : RIGHT_BRACE_IN_EXPR; }
  \(                                          { leftParen(); return LEFT_PAREN; }
  \)                                          { if(rightParen()) restoreJflexState(); return RIGHT_PAREN; }
  \)/(\.{J_IDENT})                            { if(rightParen()) yybegin(AFTER_V_IDENT); return RIGHT_PAREN; }
  \)/(\})                                     { if(rightParen()) restoreThruTransitionalState(); return RIGHT_PAREN; }

  \,                                          { return COMMA; }
  \:                                          { return COLON; }
  \;                                          { return SEMICOLON; }
  true|false                                  { return BOOLEAN; }
  {START_INTERPOL_FORMAL}                     { dollarLeftBrace(); return START_REF_FORMAL; }
  {START_INTERPOL}                            { return START_REFERENCE; }
  {V_IDENT}|{J_IDENT}                         { return IDENTIFIER; }
  {DOUBLE}                                    { return DOUBLE; }
  {INTEGER}                                   { return INTEGER; }
  {WHITE_SPACE}                               { return WHITE_SPACE; }
}

<USER_DIRECTIVE> {
  {V_IDENT}                                   { return IDENTIFIER; }
  {WHITE_SPACE}                               { return WHITE_SPACE; }
  \{                                          { return LEFT_BRACE; }
  \}                                          { return RIGHT_BRACE; }
  \(                                          { leftParen(); yybegin(PARENS); return LEFT_PAREN; }
}
 
.                                             { return BAD_CHARACTER; }

