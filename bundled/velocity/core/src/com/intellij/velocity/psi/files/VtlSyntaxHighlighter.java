/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.velocity.psi.files;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.JspHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.intellij.velocity.lexer.VtlLexer;
import com.intellij.velocity.psi.VtlElementTypes;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NonNls;

import java.util.Map;

/**
 * @author Alexey Chmutov
*/
public class VtlSyntaxHighlighter extends SyntaxHighlighterBase {

    public static final TextAttributesKey VELOCITY_DOT = createTextAttributesKey("VELOCITY_DOT", SyntaxHighlighterColors.DOT);
    public static final TextAttributesKey VELOCITY_PARENTHS = createTextAttributesKey("VELOCITY_PARENTHS", SyntaxHighlighterColors.PARENTHS);
    public static final TextAttributesKey VELOCITY_BRACKETS = createTextAttributesKey("VELOCITY_BRACKETS", SyntaxHighlighterColors.BRACKETS);
    public static final TextAttributesKey VELOCITY_BRACES = createTextAttributesKey("VELOCITY_BRACES", SyntaxHighlighterColors.BRACES);
    public static final TextAttributesKey VELOCITY_OPERATION_SIGN = createTextAttributesKey("VELOCITY_OPERATION_SIGN", SyntaxHighlighterColors.OPERATION_SIGN);
    public static final TextAttributesKey VELOCITY_STRING = createTextAttributesKey("VELOCITY_STRING", SyntaxHighlighterColors.STRING);
    public static final TextAttributesKey VELOCITY_ESCAPE = createTextAttributesKey("VELOCITY_ESCAPE", SyntaxHighlighterColors.VALID_STRING_ESCAPE);
    public static final TextAttributesKey VELOCITY_NUMBER = createTextAttributesKey("VELOCITY_NUMBER", SyntaxHighlighterColors.NUMBER);
    public static final TextAttributesKey VELOCITY_KEYWORD = createTextAttributesKey("VELOCITY_KEYWORD", SyntaxHighlighterColors.KEYWORD);
    public static final TextAttributesKey VELOCITY_COMMA = createTextAttributesKey("VELOCITY_COMMA", SyntaxHighlighterColors.COMMA);
    public static final TextAttributesKey VELOCITY_SEMICOLON = createTextAttributesKey("VELOCITY_SEMICOLON", SyntaxHighlighterColors.JAVA_SEMICOLON);
    public static final TextAttributesKey VELOCITY_DIRECTIVE = createTextAttributesKey("VELOCITY_DIRECTIVE", JspHighlighterColors.JSP_ACTION_AND_DIRECTIVE_NAME);
    public static final TextAttributesKey VELOCITY_REFERENCE = createTextAttributesKey("VELOCITY_REFERENCE", JspHighlighterColors.JSP_ATTRIBUTE_NAME);
    public static final TextAttributesKey VELOCITY_COMMENT = createTextAttributesKey("VELOCITY_COMMENT", JspHighlighterColors.JSP_COMMENT);
    public static final TextAttributesKey VELOCITY_BAD_CHARACTER = createTextAttributesKey("VELOCITY_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    public static final TextAttributesKey VELOCITY_SCRIPTING_BACKGROUND = createTextAttributesKey("VELOCITY_SCRIPTING_BACKGROUND", JspHighlighterColors.JSP_SCRIPTING_BACKGROUND);

    private static TextAttributesKey createTextAttributesKey(@NonNls String externalName, TextAttributesKey defaultTextAttr) {
        return TextAttributesKey.createTextAttributesKey(externalName, defaultTextAttr.getDefaultAttributes());
    }

    private static final Map<IElementType, TextAttributesKey> ourMap;

    static {
        ourMap = new THashMap<IElementType, TextAttributesKey>();
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_DOT, VtlElementTypes.DOT, VtlElementTypes.JAVA_DOT);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_PARENTHS, VtlElementTypes.LEFT_PAREN, VtlElementTypes.RIGHT_PAREN);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_BRACKETS, VtlElementTypes.LEFT_BRACKET, VtlElementTypes.RIGHT_BRACKET);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_BRACES, VtlElementTypes.LEFT_BRACE_IN_EXPR, VtlElementTypes.RIGHT_BRACE_IN_EXPR);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_OPERATION_SIGN, VtlElementTypes.PLUS, VtlElementTypes.MINUS, VtlElementTypes.ASTERISK, VtlElementTypes.DIVIDE, VtlElementTypes.PERCENT, VtlElementTypes.AND, VtlElementTypes.OR, VtlElementTypes.EXCLAIM, VtlElementTypes.QUESTION, VtlElementTypes.RANGE);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_STRING, VtlElementTypes.STRING_TEXT, VtlElementTypes.DOUBLE_QUOTE, VtlElementTypes.SINGLE_QUOTE);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_ESCAPE, VtlElementTypes.CHAR_ESCAPE);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_NUMBER, VtlElementTypes.INTEGER, VtlElementTypes.DOUBLE);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_KEYWORD, VtlElementTypes.BOOLEAN, VtlElementTypes.IN);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_COMMA, VtlElementTypes.COMMA);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_SEMICOLON, VtlElementTypes.SEMICOLON);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_DIRECTIVE, VtlElementTypes.SHARP_ELSE, VtlElementTypes.SHARP_END, VtlElementTypes.SHARP_STOP, VtlElementTypes.SHARP_BREAK, VtlElementTypes.SHARP_MACROCALL, VtlElementTypes.START_REFERENCE, VtlElementTypes.START_REF_FORMAL, VtlElementTypes.LEFT_BRACE, VtlElementTypes.RIGHT_BRACE);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_DIRECTIVE, VtlElementTypes.DIR_STARTERS.getTypes());
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_REFERENCE, VtlElementTypes.IDENTIFIER);
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_COMMENT, VtlElementTypes.COMMENTS.getTypes());
        SyntaxHighlighterBase.fillMap(ourMap, VELOCITY_BAD_CHARACTER, TokenType.BAD_CHARACTER);
    }

    @NotNull
    public Lexer getHighlightingLexer() {
        return new VtlLexer();
    }

    @NotNull
    public TextAttributesKey[] getTokenHighlights(final IElementType tokenType) {
        return SyntaxHighlighterBase.pack(ourMap.get(tokenType), VELOCITY_SCRIPTING_BACKGROUND);
    }
}
