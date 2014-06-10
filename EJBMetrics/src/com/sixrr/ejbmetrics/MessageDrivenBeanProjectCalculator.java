package com.sixrr.ejbmetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricsExecutionContext;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.Metric;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiClass;

public class MessageDrivenBeanProjectCalculator implements MetricCalculator {
    private MetricsExecutionContext metricsExecutionContext;
    private MetricsResultsHolder metricsResultsHolder;
    private Metric metric;

    private int totalMessageDrivenBeans =0;

    public void beginMetricsRun(Metric metric,
                                MetricsResultsHolder metricsResultsHolder,
                                MetricsExecutionContext metricsExecutionContext) {
        this.metric = metric;
        this.metricsResultsHolder = metricsResultsHolder;
        this.metricsExecutionContext = metricsExecutionContext;
    }

    public void processFile(PsiFile psiFile) {
        final PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {

            public void visitClass(PsiClass psiClass) {
                super.visitClass(psiClass);
                if(EJBUtil.isMessageDrivenBean(psiClass))
                {
                    totalMessageDrivenBeans++;
                }
            }
        };
        psiFile.accept(visitor);
    }

    public void endMetricsRun() {
        metricsResultsHolder.postProjectMetric(metric, (double) totalMessageDrivenBeans);
    }
}
