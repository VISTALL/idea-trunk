package com.sixrr.ejbmetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricsExecutionContext;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.Metric;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiClass;

import java.util.Set;

public class MessageDrivenBeanPackageCalculator implements MetricCalculator {
    private MetricsExecutionContext metricsExecutionContext;
    private MetricsResultsHolder metricsResultsHolder;
    private Metric metric;

    private BuckettedCount<PsiPackage> messageDrivenBeansPerPackage = new BuckettedCount<PsiPackage>();

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
                final PsiPackage aPackage = PackageUtil.findPackage(psiClass);
                messageDrivenBeansPerPackage.createBucket(aPackage);
                if(EJBUtil.isMessageDrivenBean(psiClass))
                {
                    messageDrivenBeansPerPackage.incrementBucketValue(aPackage);
                }
            }
        };
        psiFile.accept(visitor);
    }

    public void endMetricsRun() {
        final Set<PsiPackage> packages = messageDrivenBeansPerPackage.getBuckets();
        for (PsiPackage aPackage : packages) {
            final int numBeans = messageDrivenBeansPerPackage.getBucketValue(aPackage);
            metricsResultsHolder.postPackageMetric(metric, aPackage, (double) numBeans);
        }
    }

}
