package com.intellij.velocity.psi;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiType;
import static com.intellij.psi.util.TypeConversionUtil.isNumericType;
import static com.intellij.psi.util.TypeConversionUtil.unboxAndBalanceTypes;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.06.2008
 */
public abstract class VtlExpressionTypeCalculator {
    private VtlExpressionTypeCalculator() {
    }

    public PsiType calculateBinary(@NotNull VtlExpression leftOperand, @NotNull VtlExpression rightOperand) {
        PsiType rightType = rightOperand.getPsiType();
        if (rightType == null) {
            return null;
        }
        PsiType leftType = leftOperand.getPsiType();
        if (leftType == null) {
            return null;
        }
        return checkAndReturnNumeric(leftType, rightType);
    }

    public PsiType calculateUnary(@NotNull VtlExpression expression) {
        throw new AssertionError(this);
    }

    private static PsiType checkAndReturnNumeric(PsiType leftType, PsiType rightType) {
        if (isNumericType(leftType) && isNumericType(rightType)) {
            return unboxAndBalanceTypes(leftType, rightType);
        }
        return null;
    }

    public static final VtlExpressionTypeCalculator PLUS_CALCULATOR = new VtlExpressionTypeCalculator() {
        public PsiType calculateBinary(@NotNull VtlExpression leftOperand, @NotNull VtlExpression rightOperand) {
            PsiType rightType = rightOperand.getPsiType();
            if (rightType == null || rightType.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                return rightType;
            }
            PsiType leftType = leftOperand.getPsiType();
            if (leftType == null || leftType.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                return leftType;
            }
            return checkAndReturnNumeric(leftType, rightType);
        }
    };

    public static final VtlExpressionTypeCalculator MINUS_CALCULATOR = new VtlExpressionTypeCalculator() {
        public PsiType calculateUnary(@NotNull VtlExpression operand) {
            PsiType type = operand.getPsiType();
            return type != null && isNumericType(type) ? type : null;
        }
    };

    public static final VtlExpressionTypeCalculator MULTIPLICATIVE_CALCULATOR = new VtlExpressionTypeCalculator() {
    };

    public static final VtlExpressionTypeCalculator BOOLEAN_CALCULATOR = new VtlExpressionTypeCalculator() {
        public PsiType calculateBinary(@NotNull VtlExpression leftOperand, @NotNull VtlExpression rightOperand) {
            return PsiType.BOOLEAN;
        }

        public PsiType calculateUnary(@NotNull VtlExpression operand) {
            return PsiType.BOOLEAN;
        }
    };
}
