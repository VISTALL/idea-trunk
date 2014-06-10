/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity;

import com.intellij.lexer.Lexer;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.search.IndexPatternBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.velocity.lexer.VtlLexer;
import com.intellij.velocity.psi.VtlCommentType;
import com.intellij.velocity.psi.VtlElementTypes;
import com.intellij.velocity.psi.files.VtlFile;

/**
 * @author Alexey Chmutov
 */
public class VtlIndexPatternBuilder implements IndexPatternBuilder {
    public Lexer getIndexingLexer(final PsiFile file) {
        return (file instanceof VtlFile) ? new VtlLexer() : null;
    }

    public TokenSet getCommentTokenSet(final PsiFile file) {
        return (file instanceof VtlFile) ? VtlElementTypes.COMMENTS : null;
    }

    public int getCommentStartDelta(final IElementType tokenType) {
        assert tokenType instanceof VtlCommentType;
        return ((VtlCommentType)tokenType).getStartDelta();
    }

    public int getCommentEndDelta(final IElementType tokenType) {
        assert tokenType instanceof VtlCommentType;
        return ((VtlCommentType)tokenType).getEndDelta();
    }
}
