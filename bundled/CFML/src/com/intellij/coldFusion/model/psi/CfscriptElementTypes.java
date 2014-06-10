package com.intellij.coldFusion.model.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;

/**
 * Created by Lera Nikolaenko
 * Date: 25.11.2008
 */
public interface CfscriptElementTypes {
    IElementType FILE_CONTENT = new CfmlCompositeElementType("FILE_CONTENT");

    IElementType VALUE = new CfmlCompositeElementType("VALUE");
    IElementType IFEXPRESSION = new CfmlCompositeElementType("IFEXPRESSION");
    IElementType WHILEEXPRESSION = new CfmlCompositeElementType("WHILEEXPRESSION");
    IElementType DOWHILEEXPRESSION = new CfmlCompositeElementType("DOWHILEEXPRESSION");
    IElementType FOREXPRESSION = new CfmlCompositeElementType("FOREXPRESSION");

    IElementType FUNCTION_DEFINITION = new CfmlCompositeElementType("FUNCTION_DEFINITION");
    IElementType FUNCTION_DEFINITION_NAME = new CfmlCompositeElementType("FUNCTION_DEFINITION_NAME");

    IElementType FUNCTIONBODY = new CfmlCompositeElementType("FUNCTIONBODY");
    IElementType SWITCHEXPRESSION = new CfmlCompositeElementType("SWITCHEXPRESSION");
    IElementType CASEEXPRESSION = new CfmlCompositeElementType("CASEEXPRESSION");
    IElementType TRYCATCHEXPRESSION = new CfmlCompositeElementType("TRYCATCHEXPRESSION");
    IElementType STATEMENT = new CfmlCompositeElementType("STATEMENT");
    IElementType BLOCK_OF_STATEMENTS = new CfmlCompositeElementType("BLOCK_OF_STATEMENTS");
    IElementType CATCHEXPRESSION = new CfmlCompositeElementType("CATCH_EXPRESSION");
    IElementType ARGUMENTS_LIST = new CfmlCompositeElementType("ARGUMENTS_LIST");

    IElementType VAR_DEF = new CfmlCompositeElementType("VAR_DEF");
    IElementType FUNCTION_ARGUMENT = new CfmlCompositeElementType("FUNCTION_ARGUMENT") {
      @Override
      public PsiElement createPsiElement(ASTNode node) {
          return new CfmlFunctionParameter(node);
      }
    };
}
