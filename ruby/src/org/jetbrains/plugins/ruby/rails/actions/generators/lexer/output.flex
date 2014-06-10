package org.jetbrains.plugins.ruby.rails.actions.generators.lexer;

/* Auto generated File */ 
@SuppressWarnings({"AccessStaticViaInstance", "FieldCanBeLocal", "UnusedAssignment", "JavaDoc", "UnusedDeclaration", "SimplifiableIfStatement", "ConstantConditions"})
%%

%class OutputLexer
%unicode
%public

%function advance
%type String

%eof{ return null;
%eof}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////// USER CODE //////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%{
%}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////// REGEXPS DECLARATIONS //////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

EOL =                               "\r" | "\n" | "\r\n"
LINE =                              .*{EOL}
ANYTHING_TILL_END =                 (.*{EOL}?)*
INSTALL_SECTION =                   "Installed Generators"
IDENTIFIER =                        [a-zA-Z_][a-zA-Z0-9_]*
GENERATOR_LOCATION =                [^:]*

WHITE_SPACE_CHAR =                  [ \t\f\r\13]
WHITE_SPACE =                       {WHITE_SPACE_CHAR}+

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// STATES DECLARATIONS //////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

%state IN_INSTALL_SECTION, ID_STATE, EAT_TIL_END
%%
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////// RULES declarations ////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

<YYINITIAL>{
~({INSTALL_SECTION} {LINE})                     {yybegin(IN_INSTALL_SECTION); }
}
<IN_INSTALL_SECTION>{
{WHITE_SPACE}? {GENERATOR_LOCATION} ":" {WHITE_SPACE} {yybegin(ID_STATE); }
{WHITE_SPACE}? {EOL}                            {yybegin(EAT_TIL_END); }
}

<ID_STATE>{
{WHITE_SPACE} | ","                             {}
{IDENTIFIER}                                    {return yytext().toString(); }
{EOL}                                           {yybegin(IN_INSTALL_SECTION); }
}

<EAT_TIL_END>{
{ANYTHING_TILL_END}                             {}
}

/////////////////////////////////////////////////////
///////  Error fallback /////////////////////////////
/////////////////////////////////////////////////////
//
// .|\n|\t|\r                               { return "FLEX_ERROR";
//                                          }
