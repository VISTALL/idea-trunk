package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.psi.CfmlCompositeElementTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import static com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes.*;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Created by Lera Nikolaenko
 * Date: 10.12.2008
 */
public class CfmlExpressionParser {
    public static final CfmlExpressionParser INSTANCE = new CfmlExpressionParser();

    private CfmlExpressionParser() {
    }

    public class LexerException extends RuntimeException {
        String myMessage;

        public LexerException(String message) {
            myMessage = message;
        }

        @Override
        public String toString() {
            return myMessage;
        }
    }

    private PsiBuilder myBuilder = null;

    public void parseInit(final PsiBuilder builder) {
        myBuilder = builder;
    }

    /*
        EXPRESSION := VALUE | VALUE BOP VALUE
     */
    /*
    public boolean parseExpression() {
        boolean ifComplex = false;

        if (closeExpressionToken()) return false;
        PsiBuilder.Marker expressionMarker = myBuilder.mark();
        parseOperand();
        while (!closeExpressionToken()) {
            int offset = myBuilder.getCurrentOffset();
            ifComplex = true;
            if (BINARY_OPERATIONS.contains(getTokenType())) {
                advance();
                if (closeExpressionToken()) {
                    myBuilder.error(CfmlBundle.message("cfml.parsing.right.operand.missed"));
                    break;
                }
            } else {
                myBuilder.error(CfmlBundle.message("cfml.parsing.binary.op.expected"));
            }
            parseOperand();
            if (myBuilder.getCurrentOffset() == offset) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                advance();
            }
        }
        if (ifComplex) {
            expressionMarker.done(CfmlCompositeElementTypes.NONE);
        } else {
            expressionMarker.drop();
        }
        return true;
    }
    */
    public boolean parseBinaryExpression() {

        PsiBuilder.Marker expr = myBuilder.mark();
        if (!parseRelationalExpression()) {
            expr.drop();
            return false;
        }
        while (LOGICAL_OPERATIONS.contains(myBuilder.getTokenType())) {
            myBuilder.advanceLexer();
            if (!parseRelationalExpression()) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
            }
            expr.done(CfmlCompositeElementTypes.BINARY_EXPRESSION);
            expr = expr.precede();
        }
        expr.drop();
        return true;
    }

    private boolean parseRelationalExpression() {

        PsiBuilder.Marker expr = myBuilder.mark();
        if (!parseAdditiveExpression()) {
            expr.drop();
            return false;
        }
        while (RELATIONAL_OPERATIONS.contains(myBuilder.getTokenType())) {
            myBuilder.advanceLexer();
            if (!parseAdditiveExpression()) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
            }
            expr.done(CfmlCompositeElementTypes.BINARY_EXPRESSION);
            expr = expr.precede();
        }
        expr.drop();
        return true;
    }

    private boolean parseAdditiveExpression() {
        PsiBuilder.Marker expr = myBuilder.mark();
        if (!parseMultiplicativeExpression()) {
            expr.drop();
            return false;
        }
        while (ADDITIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
            myBuilder.advanceLexer();
            if (!parseMultiplicativeExpression()) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
            }
            expr.done(CfmlCompositeElementTypes.BINARY_EXPRESSION);
            expr = expr.precede();
        }
        expr.drop();
        return true;
    }

    private boolean parseMultiplicativeExpression() {
        PsiBuilder.Marker expr = myBuilder.mark();
        if (!parseUnaryExpression()) {
            expr.drop();
            return false;
        }
        while (MULTIPLICATIVE_OPERATIONS.contains(myBuilder.getTokenType())) {
            myBuilder.advanceLexer();
            if (!parseUnaryExpression()) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
            }
            expr.done(CfmlCompositeElementTypes.BINARY_EXPRESSION);
            expr = expr.precede();
        }
        expr.drop();
        return true;
    }

    private boolean parseUnaryExpression() {
        final IElementType tokenType = myBuilder.getTokenType();
        if (UNARY_OPERATIONS.contains(tokenType)) {
            final PsiBuilder.Marker expr = myBuilder.mark();
            myBuilder.advanceLexer();
            if (!parseUnaryExpression()) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.expression.expected"));
            }
            expr.done(CfmlCompositeElementTypes.UNARY_EXPRESSION);
            return true;
        } else {
            parseOperand();
            return true;
        }
    }
    private boolean parseAssignmentExpression() {
        PsiBuilder.Marker statementMarker = myBuilder.mark();
        int statementMarkerPosition = myBuilder.getCurrentOffset();
        if (!parseLValue()) {
            doneBefore(statementMarker, CfmlBundle.message("cfml.parsing.l.value.expected"));
            return false;
        }
        IElementType tokenType = getTokenType();
        if (tokenType != ASSIGN) {
            doneBefore(statementMarker, CfmlBundle.message("cfml.parsing.assignment.expected"));
            return true;
        } else {
            advance();
        }
        if (!parseStructureDefinition()) {
            if (!parseArrayDefinition()) {
                if (!parseBinaryExpression()) {
                    doneBefore(statementMarker, CfmlBundle.message("cfml.parsing.right.operand.missed"));
                    return true;
                }
            }
        }
        if (statementMarkerPosition != myBuilder.getCurrentOffset()) {
            statementMarker.done(CfmlCompositeElementTypes.ASSIGNMENT);
        } else {
            statementMarker.drop();
        }
        return true;
    }

    public void parsePrefixOperationExpression() {
        if (!CfscriptTokenTypes.PREFIX_OPERATIONS.contains(getTokenType())) {
            return;
        }
        PsiBuilder.Marker prefixExpressionMarker = myBuilder.mark();
        advance();
        if (!parseLValue()) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.l.value.expected"));
        }
        prefixExpressionMarker.done(CfmlCompositeElementTypes.NONE);
    }

    /*
       STATEMENT := ASSIGN | FUNCTION_CALL_NAME | MODIFICATION_EXPRESSION
       ASSIGN := LVALUE ASSIGN_OP (STRUCT_DEF | ARRAY_DEF | EXPRESSION)
       MODIFICATION_EXPRESSION := PREFIX_OP LVALUE
       ASSIGN_OP := = | += | *= ...
    */
    public void parseStatement() {
        if (myBuilder.eof() || closeExpressionToken()) {
            return;
        }
        int offset = myBuilder.getCurrentOffset();
        // parse prefix operation with value
        if (CfscriptTokenTypes.PREFIX_OPERATIONS.contains(getTokenType())) {
            parsePrefixOperationExpression();
            return;
        }
        PsiBuilder.Marker statementMarker = myBuilder.mark();
        if (getTokenType() == VAR_KEYWORD) {
            advance();
            parseAssignmentExpression();
            statementMarker.drop();
            return;
        }
        // parse function call
        if (!parseLValue()) {
            statementMarker.drop();
            return;
        }

        boolean isClearAssign = false;
        if (CfscriptTokenTypes.POSTFIX_OPERATIONS.contains(getTokenType())) {
            advance();
            statementMarker.done(CfmlCompositeElementTypes.NONE);
            return;
        } else if (!CfscriptTokenTypes.ASSIGN_OPERATORS.contains(getTokenType())) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.statemenet.expected"));
            if (CfscriptTokenTypes.OPERATIONS.contains(getTokenType())) {
                advance();
                myBuilder.error(CfmlBundle.message("cfml.parsing.assignment.expected"));
            }
        } else {
            isClearAssign = (getTokenType() == ASSIGN);
            if (myBuilder.eof()) {
                statementMarker.done(CfmlCompositeElementTypes.ASSIGNMENT);
                return;
            }
            advance();
        }
        if (isClearAssign || !parseStructureDefinition()) {
            if (isClearAssign || !parseArrayDefinition()) {
                if (!parseBinaryExpression()) {
                    if (offset == myBuilder.getCurrentOffset()) {
                        statementMarker.drop();
                        myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                        advance();
                        return;
                    }
                    myBuilder.error(CfmlBundle.message("cfml.parsing.right.operand.missed"));
                    statementMarker.drop();
                    return;
                }
            }
        }
        if (offset == myBuilder.getCurrentOffset()) {
            statementMarker.drop();
            myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
            advance();
            return;
        }
        statementMarker.done(CfmlCompositeElementTypes.ASSIGNMENT);
    }

    // TODO: think about whether I can make this code more cfml independent (using CLOSER or ANGLEBRACKET keywords)
    private boolean closeExpressionToken() {
        return getTokenType() == CfmlTokenTypes.END_EXPRESSION || getTokenType() == CLOSESHARP ||
                getTokenType() == CfmlTokenTypes.CLOSER ||
                getTokenType() == CfmlTokenTypes.R_ANGLEBRACKET || myBuilder.eof() ||
                getTokenType() == R_BRACKET || getTokenType() == R_SQUAREBRAKET ||
                getTokenType() == COMMA || getTokenType() == ASSIGN ||
                getTokenType() == CfmlTokenTypes.OPENER ||
                getTokenType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET ||
                getTokenType() == SEMICOLON ||
                getTokenType() == R_CURLYBRAKET ||
                getTokenType() == L_CURLYBRAKET/* ||
                getTokenTYpe() == CfmlTokenTypes.R*/;
    }

    // parse ([EXPRESSION])*
    private void parseArrayAccess() {
        if (getTokenType() != L_SQUAREBRAKET) {
            return;
        }
        advance();
        parseBinaryExpression();
        if (getTokenType() != R_SQUAREBRAKET) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.square.bracket.expected"));
        } else {
            advance();
        }
    }

    private boolean parseID(boolean ifSharpsInIDs) {
        if (!ifSharpsInIDs) {
            if (getTokenType() == IDENTIFIER ||
                    CfscriptTokenTypes.KEYWORDS.contains(getTokenType())) {
                advance();
                return true;
            }
        } else {
            // SHARPED_ID := (#EXPRESSION#)*IDENTIFIER(#EXPRESSION#)*
            if (getTokenType() != OPENSHARP && getTokenType() != IDENTIFIER) {
              return false;
            }
            while (getTokenType() == OPENSHARP || getTokenType() == IDENTIFIER) {
                while (getTokenType() == OPENSHARP) {
                    parseSharpExpr();
                }
                if (getTokenType() == IDENTIFIER) {
                    advance();
                }
                while (getTokenType() == OPENSHARP) {
                    parseSharpExpr();
                }
            }
            return true;
        }
        return false;
    }

    public boolean parseReference(boolean ifSharpsInIDs, boolean isDefinition) {
        boolean isReference = false;
        // for now scope keywords are ignored
        if (getTokenType() == SCOPE_KEYWORD) {
            advance();
            if (getTokenType() == POINT) {
                advance();
            } else {
                myBuilder.error(CfmlBundle.message("cfml.parsing.dot.expected"));
            }
        }

        PsiBuilder.Marker referenceExpression = myBuilder.mark();
        boolean start = true;

        while (getTokenType() == POINT || start) {
            if (!start && getTokenType() == POINT) {
                advance();
            }
            start = false;
            if (!parseID(ifSharpsInIDs)) {
                break;
            }
            isReference = true;
            referenceExpression.done(CfmlCompositeElementTypes.REFERENCE_EXPRESSION);

            if (getTokenType() == L_BRACKET) {
                isReference = false;
                referenceExpression = referenceExpression.precede();
                parseArgumentsList();
                referenceExpression.done(CfmlCompositeElementTypes.FUNCTION_CALL_EXPRESSION);
            }
            // parse ([])*
            while (getTokenType() == L_SQUAREBRAKET) {
                isReference = true;
                referenceExpression = referenceExpression.precede();
                parseArrayAccess();
                // referenceExpression.done(CfmlElementTypes.ARRAY_ACCESS);
                referenceExpression.done(CfmlCompositeElementTypes.NONE);
            }
            referenceExpression = referenceExpression.precede();
        }
        referenceExpression.drop();

        return isReference;
    }

    // Parsing up to the first error the left value of assignment in cfset tag
    // it differs from parseReferenceOrMethodCall just in:
    //   1) it must check if it is not a method call
    //   2) if value is inside quotes than dynamic variable naming available
    private boolean parseLValue() {
        boolean isReference;

        if (getTokenType() == DOUBLE_QUOTE || getTokenType() == SINGLE_QUOTE) {
            advance();
            PsiBuilder.Marker lValueMarker = myBuilder.mark();
            isReference = parseReference(true, true);
            if (!isReference) {
                lValueMarker.error(CfmlBundle.message("cfml.parsing.l.value.expected"));
            } else {
                lValueMarker.drop();
            }
            if (getTokenType() != DOUBLE_QUOTE_CLOSER ||
                    getTokenType() != SINGLE_QUOTE_CLOSER) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.quote.expected"));
                // doneBefore(lValueMarker, "Quote expected");
            } else {
                // lValueMarker.drop();
                advance();
            }
            return true;
        } else {
            boolean result = parseReference(false, true);
            if (POSTFIX_OPERATIONS.contains(getTokenType())) {
                advance();
                return false;
            }
            return result;
        }
    }

    private boolean parseArgumentsList() {
        if (getTokenType() == L_BRACKET) {
            PsiBuilder.Marker argumentList = myBuilder.mark();
            advance();
            if (getTokenType() != R_BRACKET) {
                parseExprList();
            }
            if (getTokenType() != R_BRACKET) {
                doneBefore(argumentList, CfmlBundle.message("cfml.parsing.close.bracket.expected"));
                return true;
            }
            advance();
            argumentList.done(CfmlCompositeElementTypes.ARGUMENT_LIST);
            return true;
        }
        return false;
    }

    /*
        ASSIGNLIST := LVALUE = EXPRESSION, ASSIGNLIST | LVALUE = EXPRESSION
     */
    private void parseAssignsList() {
        parseAssignmentExpression();
        if (getTokenType() == COMMA) {
            advance();
            parseAssignsList();
        }
    }

    /*
        STRUCTURE_DEFINITION := {ASSIGNLIST}
     */
    private boolean parseStructureDefinition() {
        PsiBuilder.Marker structDefMarker = mark();
        IElementType tokenType = getTokenType();
        if (tokenType == L_CURLYBRAKET) {
            advance();
            if (getTokenType() != R_CURLYBRAKET) {
              parseAssignsList();
            }
        } else {
            structDefMarker.drop();
            return false;
        }

        if (getTokenType() != R_CURLYBRAKET) {
            doneBefore(structDefMarker, CfmlBundle.message("cfml.parsing.close.bracket.expected"));
            return true;
        }
        advance();
        structDefMarker.drop();
        return true;
    }

    /*
        ARRAY_DEFINITION := [EXPRESSION_LIST]
     */
    private boolean parseArrayDefinition() {
        PsiBuilder.Marker arrayDefMarker = mark();
        IElementType tokenType = getTokenType();
        if (tokenType == L_SQUAREBRAKET) {
            advance();
            if (getTokenType() != R_SQUAREBRAKET) {
              parseExprList();
            }
        } else {
            arrayDefMarker.drop();
            return false;
        }

        if (getTokenType() != R_SQUAREBRAKET) {
            doneBefore(arrayDefMarker, CfmlBundle.message("cfml.parsing.square.bracket.expected"));
            return true;
        }
        advance();
        arrayDefMarker.drop();
        return true;
    }

    private void doneBefore(PsiBuilder.Marker valueMarker, String errorMessage) {
        PsiBuilder.Marker currentMarker = myBuilder.mark();
        valueMarker.doneBefore(CfmlCompositeElementTypes.SCRIPT_EXPRESSION, currentMarker,
                errorMessage);
        currentMarker.drop();
    }

    /*
        VALUE := (EXPRESSION) | SMART_IDENTIFIER | "STRING" | 'STRING' |
            FUNCTION_DEFINITION(EXPR_LIST) | INTEGER | DOUBLE | SHARP_EXPRESSION |
            PREFIX_OP IDENTIFIER | IDENTIFIER POSTFIX_OP
     */
    private void parseOperand() {
        // PsiBuilder.Marker valueMarker = mark();
        IElementType tokenType = getTokenType();
        if (tokenType == L_BRACKET) {
            advance();
            parseBinaryExpression();
            if (getTokenType() != R_BRACKET) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.close.bracket.expected"));
                return;
            }
            advance();
        } else if (tokenType == SINGLE_QUOTE || tokenType == DOUBLE_QUOTE) {
            PsiBuilder.Marker stringLiteral = myBuilder.mark();
            advance();
            if (getTokenType() != SINGLE_QUOTE_CLOSER &&
                    getTokenType() != DOUBLE_QUOTE_CLOSER && !myBuilder.eof()) {
                parseStringText();
            }
            if (getTokenType() != SINGLE_QUOTE_CLOSER && getTokenType() != DOUBLE_QUOTE_CLOSER) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.quote.expected"));
                stringLiteral.done(CfmlCompositeElementTypes.STRING_LITERAL);
                return;
            }
            advance();
            stringLiteral.done(CfmlCompositeElementTypes.STRING_LITERAL);
        } else if (tokenType == INTEGER) {
            PsiBuilder.Marker integerLiteral = myBuilder.mark();
            advance();
            integerLiteral.done(CfmlCompositeElementTypes.INTEGER_LITERAL);
        } else if (tokenType == DOUBLE) {
            PsiBuilder.Marker integerLiteral = myBuilder.mark();
            advance();
            integerLiteral.done(CfmlCompositeElementTypes.DOUBLE_LITERAL);
        } else if (tokenType == OPENSHARP) {
            parseSharpExpr();
        } else if (PREFIX_OPERATIONS.contains(tokenType)) {
            advance();
            parseOperand();
            // parseReference(false, false);
        } else if (tokenType == BAD_CHARACTER) {
            PsiBuilder.Marker badCharMark = myBuilder.mark();
            while (getTokenType() == BAD_CHARACTER) {
                advance();
            }
            badCharMark.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
        } else {
            parseReference(false, false);
            if (POSTFIX_OPERATIONS.contains(getTokenType())) {
                advance();
            }
        }
    }

    /*
        SHARP_EXPRESSION := #EXPRESSION#
     */
    private void parseSharpExpr() {
        // PsiBuilder.Marker sharpExprMarker = mark();

        if (getTokenType() != OPENSHARP) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.sharp.expected"));
            return;
        }
        advance();
        parseBinaryExpression();
        if (getTokenType() != CLOSESHARP) {
            myBuilder.error(CfmlBundle.message("cfml.parsing.sharp.expected"));
        } else {
            advance();
        }
        // sharpExprMarker.done(CfmlElementTypes.SHARPS_EXPRESSION);
    }

    /*
        STRING_TEXT := Eps | CfscriptTokenTypes.STRING_TEXT | STRING_TEXT SHARP_EXPRESSION STRING_TEXT;
     */
    private void parseStringText() {
        IElementType tokenType = getTokenType();
        if ((tokenType != STRING_TEXT && tokenType != OPENSHARP) ||
                tokenType == DOUBLE_QUOTE_CLOSER || tokenType == SINGLE_QUOTE_CLOSER) {
            return;
        }

        if (tokenType == STRING_TEXT) {
            advance();
            parseStringText();
        } else {/*if (tokenType == OPENSHARP) {*/
            parseSharpExpr();

            parseStringText();
        }
    }

    /*
        EXPRESSION_LIST := EXPRESSION | EXPRESSION, EXPRESSION_LIST
     */
    private void parseExprList() {
        parseBinaryExpression();
        if (getTokenType() == COMMA) {
            advance();
            parseExprList();
        }
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
