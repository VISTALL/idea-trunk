package com.intellij.coldFusion.UI.editorActions.matchers;

import com.intellij.codeInsight.highlighting.BraceMatcher;
import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.BracePair;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageBraceMatching;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeExtensionPoint;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Lera Nikolaenko
 * Date: 09.10.2008
 */
public class CfmlBraceMatcher implements BraceMatcher {
    private static final BracePair[] PAIRS = new BracePair[]{
            // new BracePair(CfmlTokenTypes.OPENER, CfmlTokenTypes.CLOSER, true),
            new BracePair(CfscriptTokenTypes.L_BRACKET, CfscriptTokenTypes.R_BRACKET, false),
            new BracePair(CfscriptTokenTypes.L_SQUAREBRAKET, CfscriptTokenTypes.R_SQUAREBRAKET, false),
            new BracePair(CfscriptTokenTypes.L_CURLYBRAKET, CfscriptTokenTypes.R_CURLYBRAKET, false),
            new BracePair(CfscriptTokenTypes.OPENSHARP, CfscriptTokenTypes.CLOSESHARP, true),/*
            new BracePair(CfscriptTokenTypes.DOUBLE_QUOTE, CfscriptTokenTypes.DOUBLE_QUOTE_CLOSER, false),
            new BracePair(CfscriptTokenTypes.SINGLE_QUOTE, CfscriptTokenTypes.SINGLE_QUOTE_CLOSER, false),*/
            new BracePair(CfmlTokenTypes.START_EXPRESSION, CfmlTokenTypes.END_EXPRESSION, true)/*,
            new BracePair(CfmlTokenTypes.DOUBLE_QUOTE, CfmlTokenTypes.DOUBLE_QUOTE_CLOSER, false),
            new BracePair(CfmlTokenTypes.SINGLE_QUOTE, CfmlTokenTypes.SINGLE_QUOTE_CLOSER, false)*/
    };

    public int getBraceTokenGroupId(IElementType tokenType) {
        final Language l = tokenType.getLanguage();
        return l.hashCode();
        /*
        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(l);
        if (matcher != null) {
          BracePair[] pairs = matcher.getPairs();
          for (BracePair pair : pairs) {
            if (pair.getLeftBraceType() == tokenType || pair.getRightBraceType() == tokenType ) {
              return l.hashCode();
            }
          }
        }
        FileType tokenFileType = tokenType.getLanguage().getAssociatedFileType();
        if (tokenFileType != CfmlFileType.INSTANCE) {
            for(FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
                if (tokenFileType.getName().equals(ext.filetype)) {
                    return ext.getInstance().getBraceTokenGroupId(tokenType);
                }
            }
        }
        return l.hashCode();
        */
    }

    public boolean isLBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        final IElementType tokenType = iterator.getTokenType();
        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType.getLanguage());
        if (matcher != null) {
            BracePair[] pairs = matcher.getPairs();
            for (BracePair pair : pairs) {
                if (pair.getLeftBraceType() == tokenType) return true;
            }
        }

        if (!tokenType.getLanguage().equals(CfmlLanguage.INSTANCE)) {
            FileType tokenFileType = iterator.getTokenType().getLanguage().getAssociatedFileType();
            if (tokenFileType != null && tokenFileType != CfmlFileType.INSTANCE) {
                for (FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
                    if (ext.filetype != null && ext.filetype.equals(tokenFileType.getName())) {
                        return ext.getInstance().isLBraceToken(iterator, fileText,
                                tokenFileType instanceof XmlFileType ? StdFileTypes.HTML : tokenFileType);
                    }
                }
            }
        }

        for (BracePair pair : PAIRS) {
            if (pair.getLeftBraceType() == tokenType)
                return true;
        }
        return tokenType.equals(CfmlTokenTypes.OPENER) && findEndTag(fileText, iterator);
    }

    public boolean isRBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType) {
        final IElementType tokenType = iterator.getTokenType();

        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType.getLanguage());
        if (matcher != null) {
            BracePair[] pairs = matcher.getPairs();
            for (BracePair pair : pairs) {
                if (pair.getRightBraceType() == tokenType) return true;
            }
        }

        if (!tokenType.getLanguage().equals(CfmlLanguage.INSTANCE)) {
            FileType tokenFileType = iterator.getTokenType().getLanguage().getAssociatedFileType();
            if (tokenFileType != null && tokenFileType != CfmlFileType.INSTANCE) {
                for (FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
                    if (ext.filetype != null && ext.filetype.equals(tokenFileType.getName())) {
                        return ext.getInstance().isRBraceToken(iterator, fileText,
                                tokenFileType instanceof XmlFileType ? StdFileTypes.HTML : tokenFileType);
                    }
                }
            }
        }

        for (BracePair pair : PAIRS) {
            if (pair.getRightBraceType() == tokenType) return true;
        }
        return tokenType.equals(CfmlTokenTypes.CLOSER) && findBeginTag(fileText, iterator);
    }

    public boolean isPairBraces(IElementType tokenType1, IElementType tokenType2) {
        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType1.getLanguage());
        if (matcher != null) {
            BracePair[] pairs = matcher.getPairs();
            for (BracePair pair : pairs) {
                if (pair.getLeftBraceType() == tokenType1) return pair.getRightBraceType() == tokenType2;
                if (pair.getRightBraceType() == tokenType1) return pair.getLeftBraceType() == tokenType2;
            }
        }

        FileType tokenFileType1 = tokenType1.getLanguage().getAssociatedFileType();
        FileType tokenFileType2 = tokenType2.getLanguage().getAssociatedFileType();
        if (tokenFileType2 != tokenFileType1) {
            return false;
        }
        if (tokenFileType1 != CfmlFileType.INSTANCE && tokenFileType1 != null) {
            for (FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
                if (ext.filetype.equals(tokenFileType1.getName())) {
                    return ext.getInstance().isPairBraces(tokenType1, tokenType2);
                }
            }
        }

        for (BracePair pair : PAIRS) {
            if (pair.getLeftBraceType() == tokenType1) return pair.getRightBraceType() == tokenType2;
            if (pair.getRightBraceType() == tokenType1) return pair.getLeftBraceType() == tokenType2;
        }
        return (tokenType1.equals(CfmlTokenTypes.OPENER) && tokenType2.equals(CfmlTokenTypes.CLOSER)) ||
                (tokenType1.equals(CfmlTokenTypes.CLOSER) && tokenType2.equals(CfmlTokenTypes.OPENER));
    }

    public boolean isStructuralBrace(HighlighterIterator iterator, CharSequence text, FileType fileType) {
        return false;
        /*
        for(FileTypeExtensionPoint<BraceMatcher> ext : Extensions.getExtensions(BraceMatcher.EP_NAME)) {
            if (StdFileTypes.HTML.getName().equals(ext.filetype)) {
                return ext.getInstance().isStructuralBrace(iterator, text, StdFileTypes.HTML);
            }
        }
        IElementType tokenType = iterator.getTokenType();

        PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(tokenType.getLanguage());
        if (matcher != null) {
          BracePair[] pairs = matcher.getPairs();
          for (BracePair pair : pairs) {
            if ((pair.getLeftBraceType() == tokenType || pair.getRightBraceType() == tokenType) &&
                pair.isStructural()) return true;
          }
        }
        for (BracePair pair : PAIRS) {
            if ((pair.getLeftBraceType() == tokenType || pair.getRightBraceType() == tokenType) &&
                    pair.isStructural()) return true;
        }
        return tokenType.equals(CfmlTokenTypes.OPENER) || tokenType.equals(CfmlTokenTypes.CLOSER);
        */
    }

    public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType contextType) {
        return true;
    }

    private boolean findBeginTag(CharSequence fileText, HighlighterIterator iterator) {
        IElementType tokenType;
        String name = getTagName(fileText, iterator);
        int balance = 0;
        int count = 0;
        while (balance < 1) {
            iterator.retreat();
            count++;
            if (iterator.atEnd()) break;
            tokenType = iterator.getTokenType();
            String currentTagName = getTagName(fileText, iterator);
            if (tokenType == CfmlTokenTypes.CLOSER && currentTagName.equals(name)) {
                balance--;
            } else if (tokenType == CfmlTokenTypes.OPENER &&
                    currentTagName.equals(name)) {
                balance++;
            }
        }
        while (count-- > 0) iterator.advance();
        return balance == 1;
    }

    private boolean findEndTag(CharSequence fileText, HighlighterIterator iterator) {
        IElementType tokenType;
        String name = getTagName(fileText, iterator);
        int balance = 0;
        int count = 0;
        while (balance > -1 && !iterator.atEnd()) {
            iterator.advance();
            count++;
            if (iterator.atEnd()) break;
            tokenType = iterator.getTokenType();
            String currrentTagName = getTagName(fileText, iterator);
            if (tokenType == CfmlTokenTypes.OPENER &&
                    currrentTagName.equals(name)) {
                balance++;
            } else if (tokenType == CfmlTokenTypes.CLOSER && currrentTagName.equals(name)) {
                balance--;
            }
        }
        while (count-- > 0) iterator.retreat();
        return balance == -1;
    }

    public String getTagName(CharSequence fileText, HighlighterIterator iterator) {
        final IElementType tokenType = iterator.getTokenType();
        String name = null;
        if (tokenType == CfmlTokenTypes.CLOSER) {
            iterator.retreat();
            IElementType tokenType1 = (!iterator.atEnd() ? iterator.getTokenType() : null);

            if (tokenType1 == CfmlTokenTypes.CF_TAG_NAME) {
                name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
            } else {
                int counter = 0;
                while (!iterator.atEnd()) {
                    if (iterator.getTokenType() == CfmlTokenTypes.CF_TAG_NAME) {
                        name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
                        break;
                    }
                    if (iterator.getTokenType() == CfmlTokenTypes.CLOSER ||
                            iterator.getTokenType() == CfmlTokenTypes.R_ANGLEBRACKET) {
                        break;
                    }
                    iterator.retreat();
                    counter++;
                }
                while (counter-- > 0) iterator.advance();
            }
            iterator.advance();
        } else if (tokenType == CfmlTokenTypes.OPENER) {
            iterator.advance();
            IElementType tokenType1 = (!iterator.atEnd() ? iterator.getTokenType() : null);

            if (tokenType1 == CfmlTokenTypes.CF_TAG_NAME) {
                name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
            }
            iterator.retreat();
        }
        return name == null ? name : name.toLowerCase();
    }

    public IElementType getOppositeBraceTokenType(@NotNull final IElementType type) {
        for (BracePair pair : PAIRS) {
            if (pair.getLeftBraceType() == type) return pair.getRightBraceType();
            if (pair.getRightBraceType() == type) return pair.getLeftBraceType();
        }
        if (type == CfmlTokenTypes.OPENER) return CfmlTokenTypes.CLOSER;
        if (type == CfmlTokenTypes.CLOSER) return CfmlTokenTypes.OPENER;
        return null;
    }

    public int getCodeConstructStart(final PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }
}
