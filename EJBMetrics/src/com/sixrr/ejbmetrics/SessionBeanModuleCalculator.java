package com.sixrr.ejbmetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricsExecutionContext;
import com.sixrr.metrics.MetricsResultsHolder;
import com.sixrr.metrics.Metric;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.openapi.module.Module;

import java.util.Set;

public class SessionBeanModuleCalculator implements MetricCalculator {
    private MetricsExecutionContext metricsExecutionContext;
    private MetricsResultsHolder metricsResultsHolder;
    private Metric metric;

    private BuckettedCount<Module> sessionBeansPerModule = new BuckettedCount<Module>();

    public void beginMetricsRun(Metric metric,
                                MetricsResultsHolder metricsResultsHolder,
                                MetricsExecutionContext metricsExecutionContext) {
        this.metric = metric;
        this.metricsResultsHolder = metricsResultsHolder;
        this.metricsExecutionContext = metricsExecutionContext;
    }

    public void processFile(PsiFile psiFile) {
        final PsiRecursiveElementVisitor visitor = new PsiRecursiveElementVisitor() {

            public void visitFile(PsiFile psiFile) {
                //This gaurantees that every module shows up in the results, even if there
                // are no session beans in it.
                super.visitFile(psiFile);
                final Module module = ModuleUtil.calculateModuleForFile(psiFile);
                sessionBeansPerModule.createBucket(module);
            }

            public void visitClass(PsiClass psiClass) {
                super.visitClass(psiClass);
                if(EJBUtil.isSessionBean(psiClass))
                {
                    final Module module = ModuleUtil.calculateModuleForClass(psiClass);
                    sessionBeansPerModule.incrementBucketValue(module);
                }
            }
        };
        psiFile.accept(visitor);
    }

    public void endMetricsRun() {
        final Set<Module> modules = sessionBeansPerModule.getBuckets();
        for (Module module : modules) {
            final int numBeans = sessionBeansPerModule.getBucketValue(module);
            metricsResultsHolder.postModuleMetric(metric, module, (double) numBeans);
        }
    }
}
