package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiType;
import org.jetbrains.annotations.Nullable;

/**
 * User: vnikolaenko
 * Date: 29.04.2009
 */
public interface ICfmlFunctionCall {
    @Nullable
    PsiType getPsiType();
    CfmlReferenceExpression getReferenceExpression();
    CfmlArgumentList findArgumentList();
    PsiType[] getArgumentTypes();
}
