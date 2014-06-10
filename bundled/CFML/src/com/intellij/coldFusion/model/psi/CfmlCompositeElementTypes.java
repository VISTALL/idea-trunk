package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.psi.tokens.CfmlTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiElement;

public interface CfmlCompositeElementTypes {
    CfmlCompositeElementType SCRIPT_EXPRESSION = new CfmlCompositeElementType("SomeScriptExression");
    CfmlCompositeElementType REFERENCE_EXPRESSION = new CfmlCompositeElementType("ReferenceExpression") {
        public PsiElement createPsiElement(final ASTNode node) {
            return new CfmlReferenceExpression(node);
        }
    };

    CfmlCompositeElementType FUNCTION_CALL_EXPRESSION = new CfmlCompositeElementType("FunctionCallExpression") {
        public PsiElement createPsiElement(final ASTNode node) {
            return new CfmlFunctionCallExpression(node);
        }
    };

    CfmlCompositeElementType FUNCTION_DEFINITION = new CfmlCompositeElementType("FunctionDefinition") {
        public PsiElement createPsiElement(final ASTNode node) {
            return new CfmlFunctionDefinition(node);
        }
    };

    CfmlCompositeElementType TAG_FUNCTION_CALL = new CfmlCompositeElementType("FunctionInvoke") {
        public PsiElement createPsiElement(final ASTNode node) {
            return new CfmlTagFunctionCall(node);
        }
    };

    CfmlCompositeElementType ARGUMENT_LIST = new CfmlCompositeElementType("ArgumentList") {
        public PsiElement createPsiElement(final ASTNode node) {
            return new CfmlArgumentList(node);
        }
    };

    CfmlCompositeElementType TAG = new CfmlCompositeElementType("Tag") {
        public PsiElement createPsiElement(final ASTNode node) {
            String tagName = node.findChildByType(CfmlTokenTypes.CF_TAG_NAME).getText();
            if ("cfinvoke".equals(tagName)) {
                return new CfmlTagFunctionCall(node);
            } else if ("cffunction".equals(tagName)) {
                return new CfmlTagFunctionDefinition(node);
            }
            return new CfmlTag(node);
        }
    };

    CfmlCompositeElementType ATTRIBUTE = new CfmlCompositeElementType("Attribute") {
        public PsiElement createPsiElement(final ASTNode node) {
            return new CfmlAttribute(node);
        }
    };

    CfmlCompositeElementType UNARY_EXPRESSION = new CfmlCompositeElementType("UnaryExpression") {
        @Override
        public PsiElement createPsiElement(ASTNode node) {
            return new CfmlOperatorExpression(node, false);
        }
    };

    CfmlCompositeElementType BINARY_EXPRESSION = new CfmlCompositeElementType("BinaryExpression") {
        @Override
        public PsiElement createPsiElement(ASTNode node) {
            return new CfmlOperatorExpression(node, true);
        }
    };

    CfmlCompositeElementType NAMED_ATTRIBUTE = new CfmlCompositeElementType("NamedAttribute") {
        @Override
        public PsiElement createPsiElement(ASTNode node) {
            return new CfmlNamedAttribute(node);
        }
    };

    CfmlCompositeElementType ATTRIBUTE_VALUE = new CfmlCompositeElementType("AttributeValue");
    CfmlCompositeElementType NONE = new CfmlCompositeElementType("None");
    CfmlCompositeElementType ASSIGNMENT = new CfmlCompositeElementType("Assignment") {
        @Override
        public PsiElement createPsiElement(ASTNode node) {
            return new CfmlAssignment(node);
        }
    };

    CfmlCompositeElementType INTEGER_LITERAL = new CfmlLiteralExpressionType("IntegerLiteral", CommonClassNames.JAVA_LANG_INTEGER);
    CfmlCompositeElementType DOUBLE_LITERAL = new CfmlLiteralExpressionType("DoubleLiteral", CommonClassNames.JAVA_LANG_DOUBLE);
    CfmlCompositeElementType BOOLEAN_LITERAL = new CfmlLiteralExpressionType("BooleanLiteral", CommonClassNames.JAVA_LANG_BOOLEAN);
    CfmlCompositeElementType STRING_LITERAL = new CfmlStringLiteralExpressionType();
}
