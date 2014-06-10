package com.sixrr.ejbmetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricsExecutionContext;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.Metric;
import com.intellij.psi.*;

import java.util.Set;

public class EntityBeanPackageCalculator implements MetricCalculator {
    private MetricsExecutionContext metricsExecutionContext;
    private MetricsResultsHolder metricsResultsHolder;
    private Metric metric;

    private BuckettedCount<PsiPackage> entityBeansPerPackage = new BuckettedCount<PsiPackage>();

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
                entityBeansPerPackage.createBucket(aPackage);
                if(EJBUtil.isEntityBean(psiClass))
                {
                    entityBeansPerPackage.incrementBucketValue(aPackage);
                }
            }
        };
        psiFile.accept(visitor);
    }

    public void endMetricsRun() {
        final Set<PsiPackage> packages = entityBeansPerPackage.getBuckets();
        for (PsiPackage aPackage : packages) {
            final int numBeans = entityBeansPerPackage.getBucketValue(aPackage);
            metricsResultsHolder.postPackageMetric(metric, aPackage, (double) numBeans);
        }
    }

}
