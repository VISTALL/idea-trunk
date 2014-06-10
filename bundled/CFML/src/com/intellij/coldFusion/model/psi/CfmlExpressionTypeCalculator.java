package com.intellij.coldFusion.model.psi;

import com.intellij.psi.*;
import static com.intellij.psi.util.TypeConversionUtil.isNumericType;
import static com.intellij.psi.util.TypeConversionUtil.unboxAndBalanceTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 28.04.2009
 */
public abstract class CfmlExpressionTypeCalculator {
    private CfmlExpressionTypeCalculator() {
    }

    public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
        return checkAndReturnNumeric(leftOperand, rightOperand);
    }

    public PsiType calculateUnary(@NotNull CfmlExpression expression) {
        throw new AssertionError(this);
    }

    private static PsiType checkAndReturnNumeric(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
        PsiType rightType = rightOperand.getPsiType();
        if (rightType == null) {
            return null;
        }
        PsiType leftType = leftOperand.getPsiType();
        if (leftType == null) {
            return null;
        }
        if (isNumericType(leftType) && isNumericType(rightType)) {
            PsiClassType boxedType = ((PsiPrimitiveType)unboxAndBalanceTypes(leftType, rightType)).getBoxedType(leftOperand.getManager(), leftOperand.getResolveScope());
            return boxedType;
        }
        return null;
    }

    public static final CfmlExpressionTypeCalculator PLUS_CALCULATOR = new CfmlExpressionTypeCalculator() {
        public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
            PsiType rightType = rightOperand.getPsiType();
            if (rightType == null || rightType.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                return rightType;
            }
            PsiType leftType = leftOperand.getPsiType();
            if (leftType == null || leftType.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                return leftType;
            }
            return checkAndReturnNumeric(leftOperand, rightOperand);
        }
    };

    public static final CfmlExpressionTypeCalculator MINUS_CALCULATOR = new CfmlExpressionTypeCalculator() {
        public PsiType calculateUnary(@NotNull CfmlExpression operand) {
            PsiType type = operand.getPsiType();
            return type != null && isNumericType(type) ? type : null;
        }
    };

    public static final CfmlExpressionTypeCalculator MULTIPLICATIVE_CALCULATOR = new CfmlExpressionTypeCalculator() {
    };

    public static final CfmlExpressionTypeCalculator CONCATINATION_CALCULATOR = new CfmlExpressionTypeCalculator() {
        public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
            return JavaPsiFacade.getInstance(leftOperand.getProject()).getElementFactory().createTypeByFQClassName(CommonClassNames.JAVA_LANG_STRING, leftOperand.getResolveScope());
        }
    };

    public static final CfmlExpressionTypeCalculator BOOLEAN_CALCULATOR = new CfmlExpressionTypeCalculator() {
        public PsiType calculateBinary(@NotNull CfmlExpression leftOperand, @NotNull CfmlExpression rightOperand) {
            return PsiType.BOOLEAN;
        }

        public PsiType calculateUnary(@NotNull CfmlExpression operand) {
            return PsiType.BOOLEAN;
        }
    };

}
