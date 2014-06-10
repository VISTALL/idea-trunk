package com.intellij.coldFusion.model.parsers;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.coldFusion.model.psi.CfmlCompositeElementType;
import com.intellij.coldFusion.model.psi.CfmlElementTypes;
import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.coldFusion.model.psi.tokens.CfscriptTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Lera Nikolaenko
 * Date: 09.10.2008
 */
public class CfmlParserDefinition implements ParserDefinition {
    @NotNull
    public Lexer createLexer(Project project) {
        return new CfmlLexer(false);
    }

    public PsiParser createParser(Project project) {
        return new CfmlParser();
    }

    public IFileElementType getFileNodeType() {
        return CfmlElementTypes.CFML_FILE;
    }

    @NotNull
    public TokenSet getWhitespaceTokens() {
        return TokenSet.create(CfmlTokenTypes.WHITE_SPACE, CfscriptTokenTypes.WHITE_SPACE);
    }

    @NotNull
    public TokenSet getCommentTokens() {
        return TokenSet.create(CfmlTokenTypes.COMMENT, CfscriptTokenTypes.COMMENT, CfmlTokenTypes.VAR_ANNOTATION);
    }

    @NotNull
    public TokenSet getStringLiteralElements() {
        return TokenSet.create(CfmlTokenTypes.STRING_TEXT,
                CfmlTokenTypes.SINGLE_QUOTE, CfmlTokenTypes.DOUBLE_QUOTE,
                CfmlTokenTypes.SINGLE_QUOTE_CLOSER, CfmlTokenTypes.DOUBLE_QUOTE_CLOSER,
                CfscriptTokenTypes.STRING_TEXT,
                CfscriptTokenTypes.SINGLE_QUOTE, CfscriptTokenTypes.SINGLE_QUOTE_CLOSER,
                CfscriptTokenTypes.DOUBLE_QUOTE, CfscriptTokenTypes.DOUBLE_QUOTE_CLOSER);
    }

    @NotNull
    public PsiElement createElement(ASTNode node) {
        final IElementType type = node.getElementType();

        if (type instanceof CfmlCompositeElementType) {
            return ((CfmlCompositeElementType)type).createPsiElement(node);
        }
        throw new AssertionError("Unknown type: " + type);


        /*if (type == CfmlElementTypes.FUNCTION_CALL_NAME) {
            return new CfmlFunctionCallExpression(node, false);
            // return new CfmlReferenceExpression(node);
        } else if (type == CfmlElementTypes.FUNCTION_DEFINITION_NAME) {
            return new CfmlFunctionDefinition(node, false);
            // return new CfmlDefinitionExpression(node);
        } else if (type == CfscriptElementTypes.FUNCTION_CALL_NAME) {
            return new CfmlFunctionCallExpression(node, true);
            // return new CfmlReferenceExpression(node);
        } else if (type == CfscriptElementTypes.FUNCTION_DEFINITION_NAME) {
            return new CfmlFunctionDefinition(node, true);
            //return new CfmlDefinitionExpression(node);
        } else if (type == CfscriptElementTypes.FUNCTION_DEFINITION) {
            return new CfscriptFunction(node);
        } else if (type == CfmlElementTypes.NAMED_ATTRIBUTE) {
            return new CfmlVariableDefinition(node, false);
        } else if (type == CfmlElementTypes.REFERENCE) {
            return new CfmlVariableUsing(node, true);
        } else if (type == CfscriptElementTypes.VAR_DEF) {
            return new CfmlVariableDefinition(node, true);
        } else if (type == CfmlElementTypes.ARGUMENT_LIST) {
            return new CfmlArgumentList(node);
        }
        */
        // return new CfmlElementImpl(node);
    }

    public PsiFile createFile(FileViewProvider viewProvider) {
        return new CfmlFile(viewProvider, CfmlLanguage.INSTANCE);
    }

    public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }

    public String toString() {
        return "CfmlParserDefinition";
    }
}

