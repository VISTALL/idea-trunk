package org.jetbrains.plugins.scala.lang.parser.stress;

import com.intellij.lang.*;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.diff.FlyweightCapableTreeStructure;

/**
 * @author ilyas
 */
public class DragBuilderWrapper implements PsiBuilder {

  final PsiBuilder myBuilder;
  final DragStorage myStorage;

  public DragBuilderWrapper(PsiBuilder builder) {
    myBuilder = builder;
    myStorage = new DragStorage();
  }

  public void advanceLexer() {
    String text = myBuilder.getTokenText();
    if (!myBuilder.eof() && text != null) {
      int start = myBuilder.getCurrentOffset();
      int end = start + text.length();
      TextRange range = new TextRange(start, end);
      myStorage.registerRevision(range);
    }
    myBuilder.advanceLexer();
  }

  public Pair<TextRange, Integer>[] getDragInfo(){
    return myStorage.getRangeInfo();
  }

  /**
   * *******************************************
   * Wrap other PsiBuilder's methods
   * ********************************************
   */

  public CharSequence getOriginalText() {
    return myBuilder.getOriginalText();
  }

/*
  public void setTokenTypeRemapper(ITokenTypeRemapper remapper)  {
    myBuilder.setTokenTypeRemapper(remapper);
  }
*/

  public IElementType getTokenType() {
    return myBuilder.getTokenType();
  }

  public String getTokenText() {
    return myBuilder.getTokenText();
  }

  public int getCurrentOffset() {
    return myBuilder.getCurrentOffset();
  }

  public void setTokenTypeRemapper(ITokenTypeRemapper remapper) {
    myBuilder.setTokenTypeRemapper(remapper);
  }

  public Marker mark() {
    return myBuilder.mark();
  }

  public void error(String messageText) {
    myBuilder.error(messageText);
  }

  public boolean eof() {
    return myBuilder.eof();
  }

  public ASTNode getTreeBuilt() {
    return myBuilder.getTreeBuilt();
  }

  public FlyweightCapableTreeStructure<LighterASTNode> getLightTree() {
    return myBuilder.getLightTree();
  }

  public void setDebugMode(boolean dbgMode) {
    myBuilder.setDebugMode(dbgMode);
  }

  public void enforceCommentTokens(TokenSet tokens) {
    myBuilder.enforceCommentTokens(tokens);
  }

  public LanguageDialect getLanguageDialect() {
    return null;
  }

  public <T> T getUserData(Key<T> key) {
    return myBuilder.getUserData(key);
  }

  public <T> void putUserData(Key<T> key, T value) {
    myBuilder.putUserData(key, value);
  }
}
