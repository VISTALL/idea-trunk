package com.sixrr.ejbmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.MetricType;
import com.sixrr.metrics.MetricCalculator;
import org.jetbrains.annotations.Nullable;

public class SessionBeanProjectMetric implements Metric {
    public String getID() {
        return "SessionBeanProject";
    }

    public String getDisplayName() {
        return "Number of EJB Session Beans";
    }

    public String getAbbreviation() {
        return "SessB";
    }

    public MetricCategory getCategory() {
        return MetricCategory.Project;
    }

    public MetricType getType() {
        return MetricType.Count;
    }

    @Nullable public String getHelpURL() {
        return "java.sun.com/products/ejb/";
    }

    @Nullable public String getHelpDisplayString() {
        return "Enterprise JavaBeans home page";
    }

    public boolean requiresDependents() {
        return false;
    }

    public MetricCalculator createCalculator() {
        return new SessionBeanProjectCalculator();
    }
}
