package com.intellij.coldFusion.model.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.CfmlUtil;
import java.util.Stack;

%%

%{
    CfscriptLexerConfiguration myCurrentConfiguration = new CfscriptLexerConfiguration();

  public class CfscriptLexerConfiguration {
      public int mySharpCounter = 0;
      public int myCommentCounter = 0;
      public Stack<Integer> myReturnStack = new Stack<Integer>();

      public CfscriptLexerConfiguration() {}

      public CfscriptLexerConfiguration(int sharpCounter, int commentCounter,
                                        Stack<Integer> returnStack) {
          mySharpCounter = sharpCounter;
          myCommentCounter = commentCounter;
          myReturnStack = returnStack;
      }

      public void reset() {
          mySharpCounter = 0;
          myCommentCounter = 0;
          myReturnStack.clear();
      }
  }

  public _CfscriptLexer() {
    this((java.io.Reader)null);
  }
  private IElementType startComment(int stateToReturnTo) {
    myCurrentConfiguration.myCommentCounter = 0;
    myCurrentConfiguration.myReturnStack.push(stateToReturnTo);
    myCurrentConfiguration.myCommentCounter++;
    yybegin(COMMENT);
    return CfmlTokenTypes.COMMENT;
  }
%}

%class _CfscriptLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

COMMENTSTART = "<!---"
COMMENTBEGIN = "!---"
COMMENTFINISH = "-->"

IDENTIFIER=([:jletter:] | [_]) ([:jletterdigit:] | [_])*
WHITE_SPACE_CHAR=[\ \n\r\t\f]+

INTEGER = 0 | [1-9] ([0-9])*
DOUBLE = {INTEGER}"."[0-9]*
IDENTIFIER=[:jletter:] [:jletterdigit:]*

DOUBLEQUOTE = \"
SINGLEQUOTE = \'

SHARP_SYMBOL = #

DOUBLEQUOTED_STRING = ([^#\"] | \"\" | "##")+
SINGLEQUOTED_STRING = ([^#\'] | \'\' | "##")+
LINE_TERMINATOR = \r|\n|\r\n

MULTILINE_COMMENT = ("/*")~("*/")
ONELINECOMMENT = ("//")~{LINE_TERMINATOR}
VARIABLE_TYPE_DECL = (("/*"){WHITE_SPACE_CHAR}"@cfmlvariable"~("*/"))|(("//"){WHITE_SPACE_CHAR}"@cfmlvariable"~{LINE_TERMINATOR})

%state DOUBLE_QUOTED_STRING, SINGLE_QUOTED_STRING, DOUBLEQUOTE_CLOSER, SINGLEQUOTE_CLOSER
%state EXPRESSION, X, Y
%state COMMENT, COMMENTEND
 
%%

<YYINITIAL> {MULTILINE_COMMENT} {return CfscriptTokenTypes.COMMENT;}
<YYINITIAL> {ONELINECOMMENT} {return CfscriptTokenTypes.COMMENT;}
<YYINITIAL> {COMMENTSTART} {return startComment(YYINITIAL);}
<YYINITIAL> {VARIABLE_TYPE_DECL} { return CfmlTokenTypes.VAR_ANNOTATION; }
<YYINITIAL> "break" { return CfscriptTokenTypes.BREAK_KEYWORD; }
<YYINITIAL> "default" { return CfscriptTokenTypes.DEFAULT_KEYWORD; }
<YYINITIAL> "function" { return CfscriptTokenTypes.FUNCTION_KEYWORD; }
<YYINITIAL> "switch" { return CfscriptTokenTypes.SWITCH_KEYWORD; }
<YYINITIAL> "case" { return CfscriptTokenTypes.CASE_KEYWORD; }
<YYINITIAL> "do" { return CfscriptTokenTypes.DO_KEYWORD; }
<YYINITIAL> "if" { return CfscriptTokenTypes.IF_KEYWORD; }
<YYINITIAL> "try" { return CfscriptTokenTypes.TRY_KEYWORD; }
<YYINITIAL> "catch" { return CfscriptTokenTypes.CATCH_KEYWORD; }
<YYINITIAL> "else" { return CfscriptTokenTypes.ELSE_KEYWORD; }
<YYINITIAL> "var"/({WHITE_SPACE_CHAR} {IDENTIFIER}) { return CfscriptTokenTypes.VAR_KEYWORD; }
<YYINITIAL> "continue" { return CfscriptTokenTypes.CONTINUE_KEYWORD; }
<YYINITIAL> "for" { return CfscriptTokenTypes.FOR_KEYWORD; }
<YYINITIAL> "return" { return CfscriptTokenTypes.RETURN_KEYWORD; }
<YYINITIAL> "while" { return CfscriptTokenTypes.WHILE_KEYWORD; }
<YYINITIAL> {WHITE_SPACE_CHAR} {return CfscriptTokenTypes.WHITE_SPACE; }
<YYINITIAL> "&" {return CfscriptTokenTypes.CONTCAT; }
<YYINITIAL> "," {return CfscriptTokenTypes.COMMA; }
<YYINITIAL> "." {return CfscriptTokenTypes.POINT; }
<YYINITIAL> "(" {return CfscriptTokenTypes.L_BRACKET; }
<YYINITIAL> ")" {return CfscriptTokenTypes.R_BRACKET; }
<YYINITIAL> "[" {return CfscriptTokenTypes.L_SQUAREBRAKET; }
<YYINITIAL> "]" {return CfscriptTokenTypes.R_SQUAREBRAKET; }
<YYINITIAL> "{" {return CfscriptTokenTypes.L_CURLYBRAKET; }
<YYINITIAL> "}" {return CfscriptTokenTypes.R_CURLYBRAKET; }
<YYINITIAL> ";" {return CfscriptTokenTypes.SEMICOLON; }
<YYINITIAL> ":" {return CfscriptTokenTypes.DOTDOT; }
/* arithmetic operators */
<YYINITIAL> "=" {return CfscriptTokenTypes.ASSIGN; }
<YYINITIAL> "+" {return CfscriptTokenTypes.ADD; }
<YYINITIAL> "+=" {return CfscriptTokenTypes.ADD_EQ; }
<YYINITIAL> "-" {return CfscriptTokenTypes.MINUS; }
<YYINITIAL> "-=" {return CfscriptTokenTypes.MINUS_EQ; }
<YYINITIAL> "*" {return CfscriptTokenTypes.MUL; }
<YYINITIAL> "*=" {return CfscriptTokenTypes.MUL_EQ; }
<YYINITIAL> "/" {return CfscriptTokenTypes.DEV; }
<YYINITIAL> "/=" {return CfscriptTokenTypes.DEV_EQ; }
<YYINITIAL> "++" {return CfscriptTokenTypes.INC; }
<YYINITIAL> "--" {return CfscriptTokenTypes.DEC; }
<YYINITIAL> "%" {return CfscriptTokenTypes.MOD; }
<YYINITIAL> "\\" {return CfscriptTokenTypes.INT_DEV; }
<YYINITIAL> "^" {return CfscriptTokenTypes.POW; }
/* logic operators */
<YYINITIAL> "!" {return CfscriptTokenTypes.NOT; }
<YYINITIAL> "||" {return CfscriptTokenTypes.OR; }
<YYINITIAL> "&&" {return CfscriptTokenTypes.AND; }

<YYINITIAL> "==" {return CfscriptTokenTypes.EQEQ; }
<YYINITIAL> "!=" {return CfscriptTokenTypes.NEQ; }
<YYINITIAL> "<" {return CfscriptTokenTypes.LT; }
<YYINITIAL> "<=" {return CfscriptTokenTypes.LTE; }
<YYINITIAL> ">" {return CfscriptTokenTypes.GT; }
<YYINITIAL> ">=" {return CfscriptTokenTypes.GTE; }

<YYINITIAL> "in" | "IN" { return CfscriptTokenTypes.IN_L; }
<YYINITIAL> "MOD" {return CfscriptTokenTypes.MOD_L; }
<YYINITIAL> "NOT" | "not" {return CfscriptTokenTypes.NOT_L; }
<YYINITIAL> "AND" | "and" {return CfscriptTokenTypes.AND_L; }
<YYINITIAL> "OR" | "or" {return CfscriptTokenTypes.OR_L; }
<YYINITIAL> "XOR" | "xor" {return CfscriptTokenTypes.XOR_L; }
<YYINITIAL> "EQV" | "eqv" {return CfscriptTokenTypes.NOT_XOR_L; }
<YYINITIAL> "IMP" | "imp" {return CfscriptTokenTypes.IMP_L; }
<YYINITIAL> "IS" | "EQUAL" | "EQ" | "is" | "equal" | "eq" {return CfscriptTokenTypes.EQ_L; }
<YYINITIAL> "IS NOT" | "NOT EQUAL" | "NEQ" | "is not" | "not equals" | "neq" {return CfscriptTokenTypes.NEQ_L; }
<YYINITIAL> "CONTAINS" | "contains" {return CfscriptTokenTypes.CONTAINS_L; }
<YYINITIAL> "DOES NOT CONTAIN" | "does not contain" {return CfscriptTokenTypes.NOT_CONTAINS_L; }
<YYINITIAL> "GREATER THAN" | "GT" | "greater than" | "gt" {return CfscriptTokenTypes.GT_L; }
<YYINITIAL> "LESS THAN" | "LT" | "less than" | "lt" {return CfscriptTokenTypes.LT_L; }
<YYINITIAL> "GREATER THAN OR EQUAL TO" | "GTE" | "GE" | "greater than or equal to" | "gte" | "ge" {return CfscriptTokenTypes.GE_L; }
<YYINITIAL> "LESS THAN OR EQUAL TO" | "LTE" | "LE" | "less than or equal to" | "lte" | "le" {return CfscriptTokenTypes.LE_L; }

/* numbers */
<YYINITIAL> {INTEGER} {return CfscriptTokenTypes.INTEGER; }
<YYINITIAL> {DOUBLE} {return CfscriptTokenTypes.DOUBLE; }
/* strings */
/*<YYINITIAL> {IDENTIFIER}/("(")  { return CfscriptTokenTypes.FUNCTION; }*/
<YYINITIAL> {IDENTIFIER} / (".")  {
    if (CfmlUtil.myVariableScopes.contains(yytext().toString().toLowerCase())) {
        return CfscriptTokenTypes.SCOPE_KEYWORD;
    } else {
        return CfscriptTokenTypes.IDENTIFIER;
    }
 }
<YYINITIAL> {IDENTIFIER} { return CfscriptTokenTypes.IDENTIFIER; }

<YYINITIAL> {SINGLEQUOTE} {
    yybegin(SINGLE_QUOTED_STRING);
    return CfscriptTokenTypes.SINGLE_QUOTE;
}
<YYINITIAL> {DOUBLEQUOTE} {
    yybegin(DOUBLE_QUOTED_STRING);
    return CfscriptTokenTypes.DOUBLE_QUOTE;
}
<YYINITIAL> {SHARP_SYMBOL} {
    if (myCurrentConfiguration.mySharpCounter == 0) {
        myCurrentConfiguration.myReturnStack.push(YYINITIAL);
        myCurrentConfiguration.mySharpCounter = 1;
        return CfscriptTokenTypes.OPENSHARP;
    }
    myCurrentConfiguration.mySharpCounter--;
    yybegin(myCurrentConfiguration.myReturnStack.pop());
    return CfscriptTokenTypes.CLOSESHARP;
}

<DOUBLE_QUOTED_STRING> {DOUBLEQUOTED_STRING} {yybegin(DOUBLEQUOTE_CLOSER); return CfscriptTokenTypes.STRING_TEXT;}
<DOUBLE_QUOTED_STRING> {DOUBLEQUOTED_STRING}/{SHARP_SYMBOL} {return CfscriptTokenTypes.STRING_TEXT;}
<DOUBLE_QUOTED_STRING> {SHARP_SYMBOL} {
    myCurrentConfiguration.myReturnStack.push(DOUBLE_QUOTED_STRING);
    myCurrentConfiguration.mySharpCounter++;
    yybegin(YYINITIAL);
    return CfscriptTokenTypes.OPENSHARP;
}
<DOUBLE_QUOTED_STRING> {DOUBLEQUOTE} {yybegin(YYINITIAL); return CfscriptTokenTypes.DOUBLE_QUOTE_CLOSER; }

<SINGLE_QUOTED_STRING> {SINGLEQUOTED_STRING} {yybegin(SINGLEQUOTE_CLOSER); return CfscriptTokenTypes.STRING_TEXT;}
<SINGLE_QUOTED_STRING> {SINGLEQUOTED_STRING}/{SHARP_SYMBOL} {return CfscriptTokenTypes.STRING_TEXT; }
<SINGLE_QUOTED_STRING> {SHARP_SYMBOL} {
    myCurrentConfiguration.myReturnStack.push(SINGLE_QUOTED_STRING);
    myCurrentConfiguration.mySharpCounter++;
    yybegin(YYINITIAL);
    return CfscriptTokenTypes.OPENSHARP;
}
<SINGLE_QUOTED_STRING> {SINGLEQUOTE} {yybegin(YYINITIAL); return CfscriptTokenTypes.SINGLE_QUOTE_CLOSER;}

<DOUBLEQUOTE_CLOSER> {DOUBLEQUOTE} {yybegin(YYINITIAL); return CfscriptTokenTypes.DOUBLE_QUOTE_CLOSER; }
<SINGLEQUOTE_CLOSER> {SINGLEQUOTE} {yybegin(YYINITIAL); return CfscriptTokenTypes.SINGLE_QUOTE_CLOSER; }

<COMMENTEND> {COMMENTFINISH} { yybegin(myCurrentConfiguration.myReturnStack.pop()); return CfmlTokenTypes.COMMENT; }

<COMMENT> "<"/({COMMENTBEGIN}) { myCurrentConfiguration.myCommentCounter++; return CfmlTokenTypes.COMMENT; }
<COMMENT> "-"/{COMMENTFINISH} { myCurrentConfiguration.myCommentCounter--;
    if (myCurrentConfiguration.myCommentCounter == 0) {
        yybegin(COMMENTEND);
    }
    return CfmlTokenTypes.COMMENT;
}
<COMMENT> [^<-]* {return CfmlTokenTypes.COMMENT;}
<COMMENT> [^] {return CfmlTokenTypes.COMMENT;}

[^]  { return CfscriptTokenTypes.BAD_CHARACTER; }

