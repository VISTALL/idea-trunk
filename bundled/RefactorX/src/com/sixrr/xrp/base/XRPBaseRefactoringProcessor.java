package com.sixrr.xrp.base;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.usageView.FindUsagesCommand;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class XRPBaseRefactoringProcessor extends BaseRefactoringProcessor {
    private static final Logger logger = Logger.getInstance("com.siyeh.rpp.XRPBaseRefactoringProcessor");
    private final boolean previewUsages;

    protected XRPBaseRefactoringProcessor(Project project, boolean previewUsages) {
        super(project);
        this.previewUsages = previewUsages;
    }

    protected boolean isPreviewUsages(UsageInfo[] usageInfos) {
        return previewUsages || super.isPreviewUsages(usageInfos);
    }

    protected void refreshElements(PsiElement[] psiElements) {
    }

    public UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos, FindUsagesCommand findUsagesCommand) {
        return createUsageViewDescriptor(usageInfos);
    }

    protected  abstract UsageViewDescriptor createUsageViewDescriptor(UsageInfo[] usageInfos);

    protected void performRefactoring(UsageInfo[] usageInfos) {
        Arrays.sort(usageInfos, new UsageInfoContainmentComparator());
        for (UsageInfo usageInfo : usageInfos) {
            try {
                ((XRPUsageInfo) usageInfo).fixUsage();
            } catch (IncorrectOperationException e) {
                logger.error(e);
            }
        }
    }

    @NotNull
    protected UsageInfo[] findUsages() {
        final List<XRPUsageInfo> usages = new ArrayList<XRPUsageInfo>();
        findUsages(usages);
        final int numUsages = usages.size();
        final XRPUsageInfo[] usageArray = usages.toArray(new XRPUsageInfo[numUsages]);
        Arrays.sort(usageArray, new UsageInfoContainmentComparator());
        return usageArray;
    }

    protected abstract void findUsages(@NotNull List<XRPUsageInfo> usages);
}
