package com.sixrr.ejbmetrics;

import com.intellij.openapi.components.ApplicationComponent;
import com.sixrr.metrics.MetricProvider;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.PrebuiltMetricProfile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class EJBMetricsPlugin implements ApplicationComponent, MetricProvider {

    @NonNls
    @NotNull
    public String getComponentName() {
        return "EJBMetricsPlugin";
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    public List<Class<? extends Metric>> getMetricClasses() {
        final List<Class<? extends Metric>> out = new ArrayList<Class<? extends Metric>>();
        out.add(SessionBeanProjectMetric.class);
        out.add(SessionBeanPackageMetric.class);
        out.add(SessionBeanModuleMetric.class);
        out.add(EntityBeanProjectMetric.class);
        out.add(EntityBeanPackageMetric.class);
        out.add(EntityBeanModuleMetric.class);
        out.add(MessageDrivenBeanProjectMetric.class);
        out.add(MessageDrivenBeanPackageMetric.class);
        out.add(MessageDrivenBeanModuleMetric.class);
        return out;
    }

    public List<PrebuiltMetricProfile> getPrebuiltProfiles() {
        final PrebuiltMetricProfile profile = new PrebuiltMetricProfile("EJB Metrics");
        profile.addMetric("SessionBeanProject");
        profile.addMetric("SessionBeanModule");
        profile.addMetric("SessionBeanPackage");
        profile.addMetric("MessageDrivenBeanProject");
        profile.addMetric("MessageDrivenBeanModule");
        profile.addMetric("MessageDrivenBeanPackage");       
        profile.addMetric("EntityBeanProject");
        profile.addMetric("EntityBeanModule");
        profile.addMetric("EntityBeanPackage");
        return Collections.singletonList(profile);
    }
}
