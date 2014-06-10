/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.psi.files;

import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.tree.IElementType;
import static com.intellij.velocity.psi.VtlElementTypes.TEMPLATE_TEXT;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VtlEditorHighlighter extends LayeredLexerEditorHighlighter {

    public VtlEditorHighlighter(@Nullable final Project project,
                                @Nullable final VirtualFile virtualFile,
                                @NotNull final EditorColorsScheme colors) {
        super(new VtlSyntaxHighlighter(), colors);
        final SyntaxHighlighter highlighter = getTemplateDataLanguageHighlighter(project, virtualFile);
        registerLayer(TEMPLATE_TEXT, new LayerDescriptor(new SyntaxHighlighter() {
            @NotNull
            public Lexer getHighlightingLexer() {
                return highlighter.getHighlightingLexer();
            }

            @NotNull
            public TextAttributesKey[] getTokenHighlights(final IElementType tokenType) {
                return highlighter.getTokenHighlights(tokenType);
            }
        }, ""));
    }

    @NotNull
    private static SyntaxHighlighter getTemplateDataLanguageHighlighter(final Project project, final VirtualFile virtualFile) {
        final FileType type = project == null || virtualFile == null ? null : VtlFileViewProvider.getTemplateDataLanguage(virtualFile, project).getAssociatedFileType();
        final FileType fileType = type == null ? StdFileTypes.PLAIN_TEXT : type;
        final SyntaxHighlighter highlighter = SyntaxHighlighter.PROVIDER.create(fileType, project, virtualFile);
        assert highlighter != null;
        return highlighter;
    }

}

