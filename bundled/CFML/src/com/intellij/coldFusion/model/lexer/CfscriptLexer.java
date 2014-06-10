package com.intellij.coldFusion.model.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by Lera Nikolaenko
 * Date: 20.11.2008
 */
public class CfscriptLexer extends MergingLexerAdapter {

    public CfscriptLexer(boolean highlightingMode) {
        super(new FlexAdapter(new _CfscriptLexer()), TokenSet.EMPTY);
    }
}