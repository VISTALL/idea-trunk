package com.intellij.coldFusion.UI.highlighting;

import com.intellij.coldFusion.CfmlBundle;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.psi.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.ide.highlighter.custom.CustomHighlighterColors;
import com.intellij.lexer.LayeredLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.SyntaxHighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.sql.psi.SqlFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
public class CfmlHighlighter extends LayeredLexerEditorHighlighter {
    public CfmlHighlighter(@Nullable final Project project,
                           @Nullable final VirtualFile virtualFile,
                           @NotNull final EditorColorsScheme colors) {
        super(new CfmlFileHighlighter(), colors);
        registerLayer(CfmlElementTypes.TEMPLATE_TEXT, new LayerDescriptor(SyntaxHighlighter.PROVIDER.create(StdFileTypes.HTML, project, virtualFile), ""));
        registerLayer(CfmlElementTypes.SQL,
                new LayerDescriptor(SyntaxHighlighter.PROVIDER.create(SqlFileType.INSTANCE, project, virtualFile), ""));
    }

    static class CfmlFileHighlighter extends SyntaxHighlighterBase {
        private static Map<IElementType, TextAttributesKey> keys1;
        private static Map<IElementType, TextAttributesKey> keys2;

        @NotNull
        public Lexer getHighlightingLexer() {
            return new LayeredLexer(new CfmlLexer(true));
        }

        static final TextAttributesKey CFML_ATTRIBUTE = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.attribute"),
                // SyntaxHighlighterColors.NUMBER.getDefaultAttributes()
                CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_COMMENT = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.comment"),
                SyntaxHighlighterColors.DOC_COMMENT.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_TAG_NAME = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.tag.name"),
                XmlHighlighterColors.HTML_TAG_NAME.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_BRACKETS = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.bracket"),
                SyntaxHighlighterColors.BRACES.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_OPERATOR = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.operator"),
                SyntaxHighlighterColors.OPERATION_SIGN.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_STRING = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.string"),
                SyntaxHighlighterColors.STRING.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_NUMBER = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.number"),
                SyntaxHighlighterColors.NUMBER.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_IDENTIFIER = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.identifier"),
                HighlighterColors.TEXT.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_BAD_CHARACTER = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.badcharacter"),
                HighlighterColors.BAD_CHARACTER.getDefaultAttributes()
        );

        static final TextAttributesKey CFML_SHARP = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.sharp"),
                CustomHighlighterColors.CUSTOM_KEYWORD2_ATTRIBUTES.getDefaultAttributes()
                /*new TextAttributes(Color.getHSBColor(289.0f / 255.0f, 89.0f / 255.0f, 48.0f / 255.0f),
                        Color.white, Color.white, EffectType.BOXED, Font.BOLD)*/
        );

        static final TextAttributesKey CFML_KEYWORD = TextAttributesKey.createTextAttributesKey(
                CfmlBundle.message("cfml.keyword"),
                SyntaxHighlighterColors.KEYWORD.getDefaultAttributes()
        );

        static {
            keys1 = new HashMap<IElementType, TextAttributesKey>();
            keys2 = new HashMap<IElementType, TextAttributesKey>();

            fillMap(keys2, CfmlTokenTypes.BRACKETS, CFML_BRACKETS);
            fillMap(keys1, CfmlTokenTypes.BRACKETS, XmlHighlighterColors.HTML_TAG);

            fillMap(keys2, CfmlTokenTypes.STRING_ELEMENTS, CFML_STRING);
            fillMap(keys1, CfmlTokenTypes.STRING_ELEMENTS, XmlHighlighterColors.HTML_TAG);

            // keys2.put(CfmlCompositeElements.TAG, XmlHighlighterColors.HTML_TAG);
            keys2.put(CfmlTokenTypes.ASSIGN, CFML_OPERATOR);
            keys1.put(CfmlTokenTypes.ASSIGN, XmlHighlighterColors.HTML_TAG);

            keys2.put(CfmlTokenTypes.START_EXPRESSION, CFML_SHARP);
            keys1.put(CfmlTokenTypes.START_EXPRESSION, XmlHighlighterColors.HTML_TAG);

            keys2.put(CfmlTokenTypes.CF_TAG_NAME, CFML_TAG_NAME);
            keys1.put(CfmlTokenTypes.CF_TAG_NAME, XmlHighlighterColors.HTML_TAG);

            keys2.put(CfmlTokenTypes.ATTRIBUTE, CFML_ATTRIBUTE);
            keys1.put(CfmlTokenTypes.ATTRIBUTE, XmlHighlighterColors.HTML_TAG);

            keys2.put(CfmlTokenTypes.END_EXPRESSION, CFML_SHARP);
            keys1.put(CfmlTokenTypes.END_EXPRESSION, XmlHighlighterColors.HTML_TAG);

            keys2.put(CfmlTokenTypes.COMMENT, CFML_COMMENT);
            keys2.put(CfmlTokenTypes.VAR_ANNOTATION, CFML_COMMENT);

            keys1.put(CfmlTokenTypes.WHITE_SPACE, XmlHighlighterColors.HTML_TAG);

            // for script language
            fillMap(keys2, CfscriptTokenTypes.OPERATIONS, CFML_OPERATOR);
            fillMap(keys2, CfscriptTokenTypes.BRACKETS, CFML_BRACKETS);
            fillMap(keys2, CfscriptTokenTypes.STRING_ELEMENTS, CFML_STRING);
            fillMap(keys2, CfscriptTokenTypes.WORD_OPERATIONS, CFML_KEYWORD);
            fillMap(keys2, CfscriptTokenTypes.KEYWORDS, CFML_KEYWORD);

            keys2.put(CfscriptTokenTypes.INTEGER, CFML_NUMBER);
            keys2.put(CfscriptTokenTypes.DOUBLE, CFML_NUMBER);
            keys2.put(CfscriptTokenTypes.COMMENT, CFML_COMMENT);
            keys2.put(CfscriptTokenTypes.IDENTIFIER, CFML_IDENTIFIER);
            keys2.put(CfscriptTokenTypes.BAD_CHARACTER, CFML_BAD_CHARACTER);
            keys2.put(CfscriptTokenTypes.OPENSHARP, CFML_SHARP);
            keys2.put(CfscriptTokenTypes.CLOSESHARP, CFML_SHARP);
        }

        @NotNull
        public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
            return pack(keys1.get(tokenType), keys2.get(tokenType));
        }
    }
}
