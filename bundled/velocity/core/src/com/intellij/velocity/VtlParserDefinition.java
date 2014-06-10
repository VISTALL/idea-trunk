/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity;

import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.velocity.lexer.VtlLexer;
import com.intellij.velocity.psi.VtlCompositeElementType;
import com.intellij.velocity.psi.VtlElementTypes;
import com.intellij.velocity.psi.files.VtlFile;
import com.intellij.velocity.psi.parsers.VtlParser;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexey Chmutov
 */
public class VtlParserDefinition implements ParserDefinition {
    @NotNull
    public Lexer createLexer(final Project project) {
        return new VtlLexer();
    }

    public PsiParser createParser(final Project project) {
        return new VtlParser();
    }

    public IFileElementType getFileNodeType() {
        return VtlElementTypes.VTL_FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(TokenType.WHITE_SPACE);
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return VtlElementTypes.COMMENTS;
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.EMPTY;
    }

    @NotNull
    public PsiElement createElement(final ASTNode node) {
        final IElementType type = node.getElementType();
        if (type instanceof VtlCompositeElementType) {
            return ((VtlCompositeElementType) type).createPsiElement(node);
        }
        throw new AssertionError("Unknown type: " + type);
    }

    public PsiFile createFile(final FileViewProvider viewProvider) {
        return new VtlFile(viewProvider);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
        return SpaceRequirements.MAY;
    }

}
