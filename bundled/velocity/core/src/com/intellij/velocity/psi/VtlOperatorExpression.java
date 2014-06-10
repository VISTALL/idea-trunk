package com.intellij.velocity.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.velocity.VelocityBundle;

/**
 * Created by IntelliJ IDEA.
* User: Alexey Chmutov
* Date: 27.06.2008
*/
public class VtlOperatorExpression extends VtlCompositeElement implements VtlExpression {

    private final boolean myBinary;

    public VtlOperatorExpression(@NotNull final ASTNode node, boolean binary) {
        super(node);
        myBinary = binary;
    }

    public PsiType getPsiType() {
        VtlExpressionTypeCalculator typeCalculator = getOperationSign().getTypeCalculator();
        VtlExpression operand1 = getOperand1();
        if (operand1 == null) {
            return null;
        }
        if(!myBinary) {
            return typeCalculator.calculateUnary(operand1);
        }
        VtlExpression operand2 = getOperand2();
        if (operand2 == null) {
            return null;
        }
        return typeCalculator.calculateBinary(operand1, operand2);
    }

    @NotNull
    private VtlOperatorTokenType getOperationSign() {
        final ASTNode operationNode = getNode().findChildByType(VtlElementTypes.OPERATIONS);
        assert operationNode != null : getText();
        IElementType tokenType = operationNode.getElementType();
        assert tokenType instanceof VtlOperatorTokenType : getText();
        return (VtlOperatorTokenType) tokenType;
    }

    private VtlExpression getOperand1() {
        return findChildByClass(VtlExpression.class);
    }

    private VtlExpression getOperand2() {
        VtlExpression first = getOperand1();
        if (first == null) {
            return null;
        }
        PsiElement second = first.getNextSibling();
        while (second != null && !(second instanceof VtlExpression)) {
            second = second.getNextSibling();
        }
        return (VtlExpression) second;
    }

    public String getIndefiniteTypeMessage() {
        VtlExpression op1 = getOperand1();
        PsiType opType1 = op1 == null ? null : op1.getPsiType();
        if(opType1 == null) {
            return null;
        }
        if(!myBinary) {
            return VelocityBundle.message("invalid.operand.type", PsiUtil.getPresentableText(opType1));
        }
        VtlExpression op2 = getOperand2();
        PsiType opType2 = op2 == null ? null : op2.getPsiType();
        if(opType2 == null) {
            return null;
        }
        return VelocityBundle.message("invalid.operands.type", PsiUtil.getPresentableText(opType1), PsiUtil.getPresentableText(opType2));
    }
}
