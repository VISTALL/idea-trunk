package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.psi.CfmlCompositeElementTypes;
import com.intellij.coldFusion.model.psi.CfscriptElementTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import static com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes.*;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Created by Lera Nikolaenko
 * Date: 13.01.2009
 */
public class CfscriptParser {
    private PsiBuilder myBuilder = null;

    public void parseInit(final PsiBuilder builder) {
        myBuilder = builder;
    }

    // parse statement or statements block in curly brackets
    // if no curly brackets than parse only one statement
    private void parseExpression() {
        CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
        CfmlExpressionParser.INSTANCE.parseBinaryExpression();
    }

    private void parseStatement() {
        // PsiBuilder.Marker statement = myBuilder.mark();

        CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
        CfmlExpressionParser.INSTANCE.parseStatement();
        eatSemicolon();
        // statement.done(CfscriptElementTypes.STATEMENT);
    }

    private void parseCondition() {
        CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
        CfmlExpressionParser.INSTANCE.parseBinaryExpression();
    }

    private void parseConstant() {
        // TODO: check type
        advance();
    }

    private boolean parseConditionInBrackets() {
        if (getTokenType() != L_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
            return false;
        }
        advance();
        parseCondition();
        if (getTokenType() != R_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
            return true;
        }
        advance();
        return true;
    }

    private void parseIfExpression() {
        if (getTokenType() != IF_KEYWORD) {
            return;
        }
        PsiBuilder.Marker ifExprMarker = myBuilder.mark();
        advance();
        if (!parseConditionInBrackets()) {
            ifExprMarker.drop();
            return;
        }

        parseScript(false);
        if (getTokenType() == ELSE_KEYWORD) {
            advance();
            if (getTokenType() == IF_KEYWORD) {
                parseIfExpression();
            } else {
                parseScript(false);
            }
        }
        ifExprMarker.done(CfscriptElementTypes.IFEXPRESSION);
    }

    private void parseWhileExpression() {
        if (getTokenType() != WHILE_KEYWORD) {
            return;
        }
        PsiBuilder.Marker whileMarker = myBuilder.mark();
        advance();
        parseConditionInBrackets();
        parseScript(false);
        whileMarker.done(CfscriptElementTypes.WHILEEXPRESSION);
    }

    private void parseDoWhileExpression() {
        if (getTokenType() != DO_KEYWORD) {
            return;
        }
        PsiBuilder.Marker doWhileMarker = myBuilder.mark();
        advance();
        if (getTokenType() != L_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.open.bracket.expected"));
            doWhileMarker.drop();
            return;
        }
        parseScript(false);
        if (getTokenType() != WHILE_KEYWORD) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.keyword.expected", "while"));
            doWhileMarker.done(CfscriptElementTypes.DOWHILEEXPRESSION);
            return;
        }
        advance();
        parseConditionInBrackets();
        if (getTokenType() != SEMICOLON) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.semicolon.expected"));
        } else {
            advance();
        }
        doWhileMarker.done(CfscriptElementTypes.DOWHILEEXPRESSION);
    }

    private void eatSemicolon() {
        if (getTokenType() != SEMICOLON) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.semicolon.expected"));
        } else {
            advance();
        }
    }

    private void parseForExpression() {
        if (getTokenType() != FOR_KEYWORD) {
            return;
        }
        PsiBuilder.Marker forExpressionMarker = myBuilder.mark();
        advance();

        // eat opening bracket
        if (getTokenType() != L_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
            return;
        }
        advance();

        parseStatement();
        // eatSemicolon();

        parseCondition();
        eatSemicolon();

        CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
        CfmlExpressionParser.INSTANCE.parseStatement();

        if (getTokenType() != R_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
            return;
        }
        advance();

        parseScript(false);

        forExpressionMarker.done(CfscriptElementTypes.FOREXPRESSION);
    }

    private void parseFunctionBody() {
        PsiBuilder.Marker functionBodyMarker = myBuilder.mark();
        if (getTokenType() != L_CURLYBRAKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"));
            functionBodyMarker.drop();
            return;
        }
        parseScript(false);
        // TODO eat return keyword
        functionBodyMarker.done(CfscriptElementTypes.FUNCTIONBODY);
    }

    private void parseArgumentsList() {
        if (getTokenType() == IDENTIFIER) {
            PsiBuilder.Marker marker = myBuilder.mark();
            advance();
            marker.done(CfscriptElementTypes.FUNCTION_ARGUMENT);
            //
            if (getTokenType() == COMMA) {
                advance();
                parseArgumentsList();
            }
        }
    }

    private void parseArgumentsListInBrackets() {
        PsiBuilder.Marker argumentsList = myBuilder.mark();
        eatLeftBracket();
        parseArgumentsList();
        eatRightBracket();
        argumentsList.done(CfscriptElementTypes.ARGUMENTS_LIST);
    }

    private void parseFunctionExpression() {
        if (getTokenType() != FUNCTION_KEYWORD) {
            return;
        }
        PsiBuilder.Marker functionMarker = myBuilder.mark();
        advance();
        if (getTokenType() != IDENTIFIER) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"));
        } else {
            advance();
        }
        parseArgumentsListInBrackets();
        parseFunctionBody();
        functionMarker.done(CfmlCompositeElementTypes.FUNCTION_DEFINITION);
    }

    private void parseDOTDOTExpression() {
        if (getTokenType() != DOTDOT) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.dot.dot.expected"));
        } else {
            advance();
        }
        parseScript(false);
    }

    private void parseCaseExpression() {
        if (getTokenType() != CASE_KEYWORD) {
            return;
        }
        PsiBuilder.Marker caseExpressionMarker = myBuilder.mark();
        advance();
        parseConstant();
        parseDOTDOTExpression();
        caseExpressionMarker.done(CfscriptElementTypes.CASEEXPRESSION);
    }


    private void parseSwitchExpression() {
        PsiBuilder.Marker switchMarker = myBuilder.mark();
        if (getTokenType() != SWITCH_KEYWORD) {
            return;
        }
        advance();
        if (getTokenType() != L_CURLYBRAKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.open.curly.bracket.expected"));
            switchMarker.drop();
            return;
        }
        advance();
        while (getTokenType() == CASE_KEYWORD) {
            parseCaseExpression();
        }
        if (getTokenType() == DEFAULT_KEYWORD) {
            advance();
            parseDOTDOTExpression();
        }
        while (getTokenType() == CASE_KEYWORD) {
            parseCaseExpression();
        }
        if (getTokenType() != R_CURLYBRAKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.close.curly.bracket.expected"));
        } else {
            advance();
        }
        switchMarker.done(CfscriptElementTypes.SWITCHEXPRESSION);
    }

    private boolean eatLeftBracket() {
        if (getTokenType() != L_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.open.bracket.expected"));
            return false;
        }
        advance();
        return true;
    }

    private void eatRightBracket() {
        if (getTokenType() != R_BRACKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
            return;
        }
        advance();
    }

    private void parseType() {
        if (getTokenType() != IDENTIFIER) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.type.expected"));
            return;
        }
        advance();
        while (getTokenType() == POINT) {
            advance();
            if (getTokenType() != IDENTIFIER) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.type.expected"));
                return;
            }
            advance();
        }
    }

    private void parseCatchExpression() {
        if (getTokenType() != CATCH_KEYWORD) {
            return;
        }
        PsiBuilder.Marker catchExpressionMarker = myBuilder.mark();
        advance();
        if (!eatLeftBracket()) {
            catchExpressionMarker.drop();
            return;
        }
        parseType();
        if (getTokenType() != IDENTIFIER) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.identifier.expected"));
            catchExpressionMarker.drop();
            return;
        }
        advance();
        eatRightBracket();
        catchExpressionMarker.done(CfscriptElementTypes.CATCHEXPRESSION);
        parseScript(false);
    }

    private void parseTryCatchExpression() {
        if (getTokenType() != TRY_KEYWORD) {
            return;
        }
        advance();
        parseScript(false);
        while (getTokenType() == CATCH_KEYWORD) {
            parseCatchExpression();
        }
        PsiBuilder.Marker tryCatchMarker = myBuilder.mark();
        tryCatchMarker.done(CfscriptElementTypes.TRYCATCHEXPRESSION);
    }

    public void parseScript(boolean betweenScriptTags) {
        boolean waitForRightBracket = false;
        PsiBuilder.Marker blockOfStatements = null;
        if (getTokenType() == L_CURLYBRAKET) {
            waitForRightBracket = true;
            blockOfStatements = myBuilder.mark();
            advance();
        }
        while (!isEndOfScript()) {
            int lexerPosition = myBuilder.getCurrentOffset();
            if (getTokenType() == VAR_KEYWORD) {
              parseStatement();
            } else if (getTokenType() == IF_KEYWORD) {
                parseIfExpression();
            } else if (getTokenType() == WHILE_KEYWORD) {
                parseWhileExpression();
            } else if (getTokenType() == DO_KEYWORD) {
                parseDoWhileExpression();
            } else if (getTokenType() == FOR_KEYWORD) {
                parseForExpression();
            } else if (getTokenType() == FUNCTION_KEYWORD) {
                parseFunctionExpression();
            } else if (getTokenType() == SWITCH_KEYWORD) {
                parseSwitchExpression();
            } else if (getTokenType() == RETURN_KEYWORD) {
                advance();
                parseExpression();
                eatSemicolon();
            } else if (getTokenType() == BREAK_KEYWORD) {
                advance();
                eatSemicolon();
            } else if (getTokenType() == L_CURLYBRAKET) {
                parseScript(false);
            } else if (getTokenType() == R_CURLYBRAKET) {
                if (waitForRightBracket) {
                    advance();
                    blockOfStatements.done(CfscriptElementTypes.BLOCK_OF_STATEMENTS);
                    return;
                } else {
                    myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                    advance();
                }
            } else if (getTokenType() == TRY_KEYWORD) {
                parseTryCatchExpression();
            } else if (CfscriptTokenTypes.KEYWORDS.contains(getTokenType())) {
                if (getTokenType() == VAR_KEYWORD || getTokenType() == SCOPE_KEYWORD) {
                    parseStatement();
                } else if (getTokenType() != CONTINUE_KEYWORD && getTokenType() != RETURN_KEYWORD &&
                        getTokenType() != BREAK_KEYWORD) {
                    PsiBuilder.Marker errorMarker = myBuilder.mark();
                    advance();
                    errorMarker.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                } else {
                    advance();
                    eatSemicolon();
                }
            } else {
                parseStatement();
            }

            if (!betweenScriptTags && !waitForRightBracket) {
                break;
            }
            if (lexerPosition == myBuilder.getCurrentOffset()) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                advance();
            }

            /*
            IN_KEYWORD,
            VAR_KEYWORD,
            */
        }
        if (blockOfStatements != null) {
            blockOfStatements.drop();
        }
    }

    private boolean isEndOfScript() {
        return getTokenType() == null || getTokenType() == CfmlTokenTypes.OPENER ||
                getTokenType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET;
    }

    // util methods
    private IElementType getTokenType() {
        return myBuilder.getTokenType();
    }

    private void advance() {
        myBuilder.advanceLexer();
    }

    private PsiBuilder.Marker mark() {
        return myBuilder.mark();
    }
}
