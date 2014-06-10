package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.CfmlUtil;
import com.intellij.coldFusion.model.psi.CfmlCompositeElementTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import static com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.Stack;

/**
 * Created by Lera Nikolaenko
 * Date: 13.10.2008
 */
public class CfmlParser implements PsiParser {
    //private CfmlExpressionParser CfmlExpressionParser.INSTANCE = new CfmlExpressionParser();
    private CfscriptParser myScriptParser = new CfscriptParser();
    private PsiBuilder myBuilder;

    private class Tag {
        public String myTagName;
        public PsiBuilder.Marker myMarkerOfBegin;
        public PsiBuilder.Marker myMarkerOfContent;

        public Tag(String string, PsiBuilder.Marker marker, PsiBuilder.Marker content) {
            myTagName = string;
            myMarkerOfBegin = marker;
            myMarkerOfContent = content;
        }
    }

    private Stack<Tag> myTagNamesStack = new Stack<Tag>();

    @NotNull
    public ASTNode parse(final IElementType root, final PsiBuilder builder) {
        myBuilder = builder;
        // myBuilder.setDebugMode(true);
        final PsiBuilder.Marker marker = builder.mark();
        while (!builder.eof()) {
            if (builder.getTokenType() == CfmlTokenTypes.OPENER) {
                parseOpenTag();
            } else if (builder.getTokenType() == CfmlTokenTypes.LSLASH_ANGLEBRACKET) {
                parseCloseTag();
            } else {
                advance();
            }
        }
        while (!myTagNamesStack.isEmpty()) {
            Tag tag = myTagNamesStack.pop();
            // tag.myMarkerOfBegin.drop();
            if (CfmlUtil.isUserDefined(tag.myTagName)) {
                tag.myMarkerOfBegin.doneBefore(CfmlCompositeElementTypes.TAG, tag.myMarkerOfContent);
            } else {
                tag.myMarkerOfBegin.doneBefore(CfmlCompositeElementTypes.TAG, tag.myMarkerOfContent,
                        CfmlBundle.message("cfml.parsing.element.is.not.closed", tag.myTagName));
            }
            tag.myMarkerOfContent.drop();
        }
        marker.done(root);
        return builder.getTreeBuilt();
    }

    private void parseExpression() {
        if (myBuilder.getTokenType() == CfmlTokenTypes.START_EXPRESSION) {
            advance();
        } else {
            return;
        }
        CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
        CfmlExpressionParser.INSTANCE.parseBinaryExpression();
        if (myBuilder.getTokenType() != CfmlTokenTypes.END_EXPRESSION) {
            PsiBuilder.Marker marker = myBuilder.mark();
            marker.error(CfmlBundle.message("cfml.parsing.expression.unclosed"));
        } else if (!myBuilder.eof()) {
            advance();
        }
    }

    private void readValue(IElementType typeOfValue) {
        // reading string
        if (typeOfValue != null) {
            if (myBuilder.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE ||
                    myBuilder.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE) {
                advance();
                PsiBuilder.Marker marker = myBuilder.mark();
                int valueStartOffset = myBuilder.getCurrentOffset();
                while (!myBuilder.eof() && myBuilder.getTokenType() != CfmlTokenTypes.SINGLE_QUOTE_CLOSER &&
                        myBuilder.getTokenType() != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER &&
                        !CfmlUtil.isControlToken(myBuilder.getTokenType())) {
                    if (myBuilder.getTokenType() == CfmlTokenTypes.START_EXPRESSION) {
                        parseExpression();
                    } else {
                        advance();
                    }
                }
                if (myBuilder.getCurrentOffset() != valueStartOffset) {
                    marker.done(typeOfValue);
                } else {
                    marker.drop();
                }
                if (myBuilder.getTokenType() != CfmlTokenTypes.SINGLE_QUOTE_CLOSER &&
                        myBuilder.getTokenType() != CfmlTokenTypes.DOUBLE_QUOTE_CLOSER) {
                    myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                } else {
                    advance();
                }
                return;
            }
        }
        // reading what is comming up to the next control token
        while (!myBuilder.eof() && !CfmlUtil.isControlToken(myBuilder.getTokenType()) &&
                myBuilder.getTokenType() != CfmlTokenTypes.ATTRIBUTE) {
            if (myBuilder.getTokenType() == CfmlTokenTypes.START_EXPRESSION) {
                parseExpression();
            } else {
                advance();
            }
        }
    }

    private void parseAttributes(String tagName) {
        if (tagName.equals("cfset")) {
            // parsing statement int cfset tag
            CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
            CfmlExpressionParser.INSTANCE.parseStatement();
            return;
        } else if (tagName.equals("cfif") || tagName.equals("cfelseif")) {
            // parsin expression in condition tags
            CfmlExpressionParser.INSTANCE.parseInit(myBuilder);
            CfmlExpressionParser.INSTANCE.parseBinaryExpression();
            return;
        }

        boolean hasAttrs = !CfmlUtil.getAttributes(tagName).isEmpty();
        while (!myBuilder.eof() && !CfmlUtil.isControlToken(myBuilder.getTokenType())) {
            if (myBuilder.getTokenType() == ATTRIBUTE) {
                @SuppressWarnings({"ConstantConditions"}) String attributeName = myBuilder.getTokenText().toLowerCase();
                PsiBuilder.Marker attrMarker = myBuilder.mark();
                // PsiBuilder.Marker attrNameMarker = myBuilder.mark();
                advance();
                /*
                if (!CfmlUtil.isAttribute(tagName, attributeName)) {
                    attrNameMarker.error(CfmlBundle.message("cfml.parsing.no.such.attribute", tagName));
                } else {
                    attrNameMarker.drop();
                }
                */
                if (myBuilder.getTokenType() != CfmlTokenTypes.ASSIGN) {
                    attrMarker.done(CfmlCompositeElementTypes.ATTRIBUTE);
                    myBuilder.error(CfmlBundle.message("cfml.parsing.no.value"));
                    continue;
                }
                advance();
                PsiBuilder specialMark = null;
                if ("name".equals(attributeName)) {
                    readValue(CfmlCompositeElementTypes.ATTRIBUTE_VALUE);
                    attrMarker.done(CfmlCompositeElementTypes.NAMED_ATTRIBUTE);
                } else if ("method".equals(attributeName) && "cfinvoke".equals(tagName)) {
                    readValue(CfmlCompositeElementTypes.REFERENCE_EXPRESSION);
                    attrMarker.done(CfmlCompositeElementTypes.TAG_FUNCTION_CALL);
                } else {
                    readValue(CfmlCompositeElementTypes.ATTRIBUTE_VALUE);
                    attrMarker.done(CfmlCompositeElementTypes.ATTRIBUTE);
                }
            } /*else if (myBuilder.getTokenType() == IDENTIFIER && hasAttrs) {
                PsiBuilder.Marker attrMarker = myBuilder.mark();
                advance();
                attrMarker.error(CfmlBundle.message("cfml.parsing.no.such.attribute", tagName));
            } */
            else {
                advance();
            }
        }
    }

    private boolean parseCloseTag() {
        // PsiBuilder.Marker closingTagMarker = myBuilder.mark();
        Tag tag = null;
        advance();
        if (myBuilder.getTokenType() == CF_TAG_NAME) {
            @SuppressWarnings({"ConstantConditions"}) String closeTag = myBuilder.getTokenText().toLowerCase();
            // drop all unexpected user defined tags
            if (!CfmlUtil.isUserDefined(closeTag)) {
                while (!myTagNamesStack.empty() && CfmlUtil.isUserDefined(myTagNamesStack.peek().myTagName)) {
                    Tag custom_tag = myTagNamesStack.pop();
                    custom_tag.myMarkerOfBegin.doneBefore(CfmlCompositeElementTypes.TAG, custom_tag.myMarkerOfContent);
                    custom_tag.myMarkerOfContent.drop();
                }
            }
            // canParse = if in the stack somewhere (not necessary on the top) there is the same tag name
            boolean canParse = false;
            for (Tag t : myTagNamesStack) {
                if (t.myTagName.equals(closeTag)) {
                    canParse = true;
                    break;
                }
            }
            // if same tag name is on the top of the stack
            boolean isCorrect = !myTagNamesStack.isEmpty() &&
                    myTagNamesStack.peek().myTagName.equals(myBuilder.getTokenText());
            // Tag lastUnclosed = isCorrect ? null : myTagNamesStack.peek();
            // drop all markers with unclosed tags
            if (canParse) {
                while (!myTagNamesStack.empty() && !((tag = myTagNamesStack.pop()).myTagName.equals(closeTag))) {
                    if (CfmlUtil.isUserDefined(tag.myTagName)) {
                        tag.myMarkerOfBegin.doneBefore(CfmlCompositeElementTypes.TAG, tag.myMarkerOfContent);
                    } else {
                        tag.myMarkerOfBegin.doneBefore(CfmlCompositeElementTypes.TAG, tag.myMarkerOfContent,
                                CfmlBundle.message("cfml.parsing.element.is.not.closed", tag.myTagName));
                    }
                    tag.myMarkerOfContent.drop();
                }
                // closingTagMarker.drop();
            } else {
                myBuilder.error(CfmlBundle.message("cfml.parsing.closing.tag.matches.nothing"));
                // closingTagMarker.error("Closing tag matches nothing");
            }
            advance();
            if (!myBuilder.eof() && !CfmlUtil.isControlToken(myBuilder.getTokenType())) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                advance();
            }
            while (!myBuilder.eof() && !CfmlUtil.isControlToken(myBuilder.getTokenType())) {
                advance();
            }
            if (myBuilder.getTokenType() == CLOSER) {
                advance();
                if (canParse) {
                    tag.myMarkerOfContent.drop();
                    tag.myMarkerOfBegin.done(CfmlCompositeElementTypes.TAG);
                    return true;
                } else {
                    // closingTagMarker.error("No open tag specified");
                    return false;
                }
            } else if (canParse) {
                myBuilder.error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
                tag.myMarkerOfContent.drop();
                tag.myMarkerOfBegin.done(CfmlCompositeElementTypes.TAG);
                return false;
            }
        }
        // closingTagMarker.error("Unformatted closing tag");
        return false;
    }

    private void parseOpenTag() {
        PsiBuilder.Marker marker;
        String currentTagName = "";

        while (!myBuilder.eof()) {
            marker = myBuilder.mark();
            advance();

            // parsing tag name
            if (myBuilder.getTokenType() == CF_TAG_NAME) {
                currentTagName = myBuilder.getTokenText().toLowerCase();
                advance();
            } else {
                error(CfmlBundle.message("cfml.parsing.unexpected.token"));
                marker.drop();
                continue;
            }

            parseAttributes(currentTagName);

            // if CLOSER found than the check, if tag can be single performed,
            // if closing tag found, check if it matches the opening one, otherwise continue cicle
            if (myBuilder.eof()) {
                error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
                marker.done(CfmlCompositeElementTypes.TAG);
                return;
            }
            if (myBuilder.getTokenType() == CLOSER) {
                advance();
                marker.done(CfmlCompositeElementTypes.TAG);
            } else if (myBuilder.getTokenType() == R_ANGLEBRACKET) {
                advance();
                myTagNamesStack.push(new Tag(currentTagName, marker, myBuilder.mark()));
            } else {
                /*
                PsiBuilder.Marker contentMarker = myBuilder.mark();
                marker.doneBefore(CF_TAG_NAME, contentMarker,
                        CfmlBundle.message("cfml.parsing.element.is.not.closed", currentTagName));
                contentMarker.drop();
                */
                error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
                myTagNamesStack.push(new Tag(currentTagName, marker, myBuilder.mark()));
                // marker.error(CfmlBundle.message("cfml.parsing.tag.is.not.done"));
            }
            if (currentTagName.toLowerCase().equals("cfscript")) {
                myScriptParser.parseInit(myBuilder);
                myScriptParser.parseScript(true);
            }
            while (!myBuilder.eof() && myBuilder.getTokenType() != OPENER) {
                if (myBuilder.getTokenType() == LSLASH_ANGLEBRACKET) {
                    parseCloseTag();
                } else if (myBuilder.getTokenType() == START_EXPRESSION) {
                    parseExpression();
                } else {
                    advance();
                }
            }
        }
    }

    protected final void advance() {
        myBuilder.advanceLexer();
    }

    private void error(final String message) {
        myBuilder.error(message);
    }

    private PsiBuilder.Marker mark() {
        return myBuilder.mark();
    }

    private IElementType token() {
        return myBuilder.getTokenType();
    }

    public String toString() {
        return "CfmlParser";
    }
}
