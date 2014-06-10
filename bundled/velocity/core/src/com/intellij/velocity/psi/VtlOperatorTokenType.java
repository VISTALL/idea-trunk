package com.intellij.velocity.psi;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Alexey Chmutov
 * Date: 27.03.2008
 */
public class VtlOperatorTokenType extends VtlTokenType {
    private final VtlExpressionTypeCalculator myTypeCalculator;

    public VtlOperatorTokenType(@NotNull @NonNls final String debugName, @NotNull VtlExpressionTypeCalculator typeCalculator) {
        super(debugName);
        myTypeCalculator = typeCalculator;
    }

    public VtlExpressionTypeCalculator getTypeCalculator() {
        return myTypeCalculator;
    }
}