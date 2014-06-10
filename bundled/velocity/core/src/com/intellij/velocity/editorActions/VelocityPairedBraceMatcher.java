/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.editorActions;

import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import static com.intellij.velocity.psi.VtlElementTypes.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class VelocityPairedBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[] {
            new BracePair(SHARP_FOREACH, SHARP_END, true),
            new BracePair(SHARP_IF, SHARP_END, true),
            new BracePair(SHARP_MACRODECL, SHARP_END, true),
            new BracePair(SHARP_DEFINE, SHARP_END, true),
            new BracePair(SHARP_LITERAL, SHARP_END, true),
            new BracePair(START_REF_FORMAL, RIGHT_BRACE, false),
            new BracePair(LEFT_BRACE, RIGHT_BRACE, false),
            new BracePair(LEFT_BRACE_IN_EXPR, RIGHT_BRACE_IN_EXPR, false),
            new BracePair(LEFT_PAREN, RIGHT_PAREN, false),
            new BracePair(LEFT_BRACKET, RIGHT_BRACKET, false),
    };

    public BracePair[] getPairs() {
        return PAIRS;
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType type) {
        return lbraceType == LEFT_PAREN && type == null;
    }

    public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }

}
