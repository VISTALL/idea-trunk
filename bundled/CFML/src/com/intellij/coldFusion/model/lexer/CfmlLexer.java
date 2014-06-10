package com.intellij.coldFusion.model.lexer;

import com.intellij.coldFusion.model.psi.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Created by Lera Nikolaenko
 * Date: 06.10.2008
 */
/*
public class CfmlLexer extends LexerBase {
    protected static final int NONINITIAL_STATE = 1000;
    private Lexer myOriginal;
    private Lexer myCfscriptLexer = null;

    public CfmlLexer(boolean highlightingMode) {
        myOriginal = new FlexAdapter(new _CfmlLexer());
    }

    public char[] getBuffer(){
      return myOriginal.getBuffer();
    }

    public int getBufferEnd(){
      return myOriginal.getBufferEnd();
    }

    public void start(char[] buffer, int startOffset, int endOffset, int initialState) {
        myOriginal.start(buffer, startOffset, endOffset, initialState & 0xFFFF);    //Overriden method. Auto insertion
        if (isCurrentTokenIsScriptToken()) {
            startScriptLexer(buffer, startOffset, endOffset, initialState >> 16);
        }
    }

    public int getState() {
        return NONINITIAL_STATE;
        /*
        if (myCfscriptLexer == null) {
            return myOriginal.getState() & 0xFFFF;
        } else {
            return myCfscriptLexer.getState() << 16 | myOriginal.getState() & 0xFFFF;
        }
        */
/*    }

    public IElementType getTokenType() {
        if (myCfscriptLexer != null) {
            return myCfscriptLexer.getTokenType();
        }
        return myOriginal.getTokenType();
    }

    public int getTokenStart() {
        if (myCfscriptLexer != null) {
            return myCfscriptLexer.getTokenStart();
        }
        return myOriginal.getTokenStart();
    }

    public int getTokenEnd() {
        if (myCfscriptLexer != null) {
            return myCfscriptLexer.getTokenEnd();
        }
        return myOriginal.getTokenEnd();
    }

    public void advance() {
        if (myCfscriptLexer == null) {
            myOriginal.advance();
            if (myOriginal.getTokenType() == CfmlElementTypes.CF_SCRIPT ||
                    myOriginal.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION) {
                startScriptLexer(super.getBufferSequence(), myOriginal.getTokenStart(), myOriginal.getBufferEnd(), 0);
            }
        } else {
            int end1 = myOriginal.getTokenEnd();
            int end2 = myCfscriptLexer.getTokenEnd();
            int myCurOffset = Math.min(end1, end2);

            if (myCurOffset == end2){
              myCfscriptLexer.advance();
            }

            if (myCurOffset == end1){
              myOriginal.advance();
              if (!isCurrentTokenIsScriptToken()) {
                  myCfscriptLexer = null;
              }
            }
        }
    }

    private void startScriptLexer(char[] buffer, int startOffset, int endOffset, int initialState) {
        if (myOriginal.getTokenType() == CfmlElementTypes.CF_SCRIPT ||
                myOriginal.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION) {
            int endPosition = endOffset;//getEndOfScript();
            // TODO not to create it every time!!!!! Hash!
            myCfscriptLexer = new CfscriptLexer(false);//new CfscriptHighlighter.CfscriptFileHighlighter().getHighlightingLexer();
            myCfscriptLexer.start(buffer, startOffset, Math.min(endPosition, endOffset), initialState >> 16);
        }
    }

    private void startScriptLexer(CharSequence buffer, int startOffset, int endOffset, int initialState) {
        if (myOriginal.getTokenType() == CfmlElementTypes.CF_SCRIPT ||
                myOriginal.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION) {
            int endPosition = endOffset;//getEndOfScript();
            // TODO not to create it every time!!!!! Hash!
            myCfscriptLexer = new CfscriptLexer(false);//new CfscriptHighlighter.CfscriptFileHighlighter().getHighlightingLexer();
            if (endOffset > 0) {
                endPosition = Math.min(endPosition, endOffset);
            }
            myCfscriptLexer.start(buffer, startOffset, endPosition, initialState >> 16);
        }
    }

    public boolean isCurrentTokenIsScriptToken() {
        return myOriginal.getTokenType() == CfmlElementTypes.CF_SCRIPT ||
                myOriginal.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION;
    }

    private class myLexerPosition implements LexerPosition {
        private Object myCfmlConfig;
        private Object myCfscriptConfig;
        private int myOffset;
        private int myState;

        protected myLexerPosition(Object myCfmlConfig, Object myCfscriptConfig, int myOffset, int myState) {
            this.myCfmlConfig = myCfmlConfig;
            this.myCfscriptConfig = myCfscriptConfig;
            this.myOffset = myOffset;
            this.myState = myState;
        }
        
        public int getOffset() {
            return myOffset;
        }

        public int getState() {
            return myState;
        }
    }

    @Override
    public LexerPosition getCurrentPosition() {
        final int offset = getTokenStart();
        final int state = getState();
        return new LexerPosition () {
            public int getOffset() {
                return offset;
            }

            public int getState() {
                return state;
            }
        };
    }

    @Override
    public void restore(LexerPosition position) {
        super.start(super.getBufferSequence(), position.getOffset(), myOriginal.getBufferEnd(),
                position.getState() & 0xFFFF);
        
        if (isCurrentTokenIsScriptToken()) {
            startScriptLexer(super.getBufferSequence(), position.getOffset(), myOriginal.getBufferEnd(),
                position.getState() >> 16);
        }
    }

    /*
    @Override
    public Lexer getOriginal() {
        if (myCfscriptLexer == null) {
            return myOriginal;
        }
        return myCfscriptLexer; 
    }

    /*

    private static final TokenSet TOKENS_TO_MERGE =
            TokenSet.create(CfmlTokenTypes.COMMENT,
                    CfmlTokenTypes.WHITE_SPACE,
                    CfmlTokenTypes.SCRIPT_EXPRESSION);
   */
/*
}
*/
public class CfmlLexer extends MergingLexerAdapter {
    private Lexer myCfscriptLexer = null;
    private int myStartPosition = 0;

    private static final TokenSet TOKENS_TO_MERGE =
            TokenSet.create(CfmlTokenTypes.COMMENT,
                    CfmlTokenTypes.WHITE_SPACE,
                    CfmlTokenTypes.SCRIPT_EXPRESSION);

    public CfmlLexer(boolean highlightingMode) {
        super(new FlexAdapter(new _CfmlLexer()), TOKENS_TO_MERGE);
    }

    @Override
    public int getState() {
        return 1000;//myCfscriptLexer != null ? myCfscriptLexer.getState() : getState();
    }

    @Override
    public void advance() {
        if (myCfscriptLexer != null) {
            /*
            if (myStateToReturn == START_EXPRESSION) {
                myStateToReturn = ORIGINAL;
            }
            */
            myCfscriptLexer.advance();
            if (myCfscriptLexer.getTokenType() == null) {
                myCfscriptLexer = null;
                // myStateToReturn = END_EXPRESSION;
            }
        } else {
            /*
            if (myStateToReturn == END_EXPRESSION) {
                myStateToReturn = ORIGINAL;
            }
            */
            super.advance();
            if (super.getTokenType() == CfmlElementTypes.CF_SCRIPT ||
                    super.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION) {
                // myStateToReturn = START_EXPRESSION;
                final int startPosition = super.getTokenStart();
                myStartPosition = startPosition;
                int endPosition = super.getTokenEnd();
                while (super.getTokenType() == CfmlTokenTypes.SCRIPT_EXPRESSION ||
                        super.getTokenType() == CfmlElementTypes.CF_SCRIPT) {
                    endPosition = super.getTokenEnd();
                    super.advance();
                }
                myCfscriptLexer = new CfscriptLexer(true);//new CfscriptHighlighter.CfscriptFileHighlighter().getHighlightingLexer();
                myCfscriptLexer.start(super.getBufferSequence().subSequence(startPosition, endPosition),
                    0, endPosition - startPosition, myCfscriptLexer.getState());
            }
        }
    }

    @Override
    public IElementType getTokenType() {
        /*
        if (myStateToReturn == START_EXPRESSION) {
            return CfmlTokenTypes.START_EXPRESSION;
        } else if (myStateToReturn == END_EXPRESSION) {
            return CfmlTokenTypes.END_EXPRESSION;
        }
        */
        if (myCfscriptLexer != null) {
            return myCfscriptLexer.getTokenType();
        }
        return super.getTokenType();
    }

    @Override
    public int getTokenStart() {
        if (myCfscriptLexer != null) {
            return myCfscriptLexer.getTokenStart() + myStartPosition;
        }
        return super.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        /*
        if (myStateToReturn != ORIGINAL) {
            return getTokenStart();
        }
        */
        if (myCfscriptLexer != null) {
            return myCfscriptLexer.getTokenEnd() + myStartPosition;
        }
        return super.getTokenEnd();
    }
}
