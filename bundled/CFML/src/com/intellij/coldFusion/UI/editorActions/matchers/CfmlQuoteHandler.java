package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;

public class CfmlQuoteHandler implements QuoteHandler {
  public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
    return iterator.getTokenType() == CfscriptTokenTypes.DOUBLE_QUOTE_CLOSER ||
            iterator.getTokenType() == CfscriptTokenTypes.SINGLE_QUOTE_CLOSER ||
            iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE_CLOSER ||
            iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE_CLOSER;
  }

  public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
      return iterator.getTokenType() == CfscriptTokenTypes.SINGLE_QUOTE ||
              iterator.getTokenType() == CfscriptTokenTypes.DOUBLE_QUOTE ||
              iterator.getTokenType() == CfmlTokenTypes.SINGLE_QUOTE ||
              iterator.getTokenType() == CfmlTokenTypes.DOUBLE_QUOTE;
  }

  public boolean hasNonClosedLiteral(Editor editor, HighlighterIterator iterator, int offset) {
    return true;
  }

  public boolean isInsideLiteral(HighlighterIterator iterator) {
    return false;
  }
}
