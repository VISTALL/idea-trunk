package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.lang.BracePair;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 * Date: 27.10.2008
 */
public class CfmlPairedBraceMatcher implements PairedBraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[] {
        new BracePair(CfscriptTokenTypes.L_BRACKET, CfscriptTokenTypes.R_BRACKET, false),
        new BracePair(CfscriptTokenTypes.L_SQUAREBRAKET, CfscriptTokenTypes.R_SQUAREBRAKET, false),
        new BracePair(CfscriptTokenTypes.L_CURLYBRAKET, CfscriptTokenTypes.R_CURLYBRAKET, true),
        new BracePair(CfscriptTokenTypes.OPENSHARP, CfscriptTokenTypes.CLOSESHARP, true)
    };

    public BracePair[] getPairs() {
      return PAIRS;
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType tokenType) {
      return lbraceType != CfscriptTokenTypes.L_CURLYBRAKET;
    }

    public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
      return openingBraceOffset;
    }
}
