package com.sixrr.xrp.base;

import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.IncorrectOperationException;

@SuppressWarnings({"AbstractClassExtendsConcreteClass"})
public abstract class XRPUsageInfo extends UsageInfo {
    protected XRPUsageInfo(PsiElement element) {
        super(element);
    }

    public abstract void fixUsage() throws IncorrectOperationException;

}
