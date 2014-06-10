package com.intellij.velocity.lexer;

import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.TokenSet;
import static com.intellij.velocity.psi.VtlElementTypes.MULTILINE_COMMENT;
import static com.intellij.velocity.psi.VtlElementTypes.TEMPLATE_TEXT;

/**
 * @author Alexey Chmutov
 */
public class VtlLexer extends MergingLexerAdapter {
  private static final TokenSet TOKENS_TO_MERGE = TokenSet.create(MULTILINE_COMMENT, TEMPLATE_TEXT);

  public VtlLexer() {
    super(new FlexAdapter(new _VtlLexer()), TOKENS_TO_MERGE);
  }
}
