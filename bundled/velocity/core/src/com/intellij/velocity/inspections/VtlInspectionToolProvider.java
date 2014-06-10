package com.intellij.velocity.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.velocity.inspections.wellformedness.VtlInterpolationsInspection;

/**
 * @author Alexey Chmutov
 */
public class VtlInspectionToolProvider implements InspectionToolProvider {
    public Class[] getInspectionClasses() {
        return new Class[]{
                VtlReferencesInspection.class,
                VtlInterpolationsInspection.class,
                VtlTypesInspection.class,
                VtlFileReferencesInspection.class,
                VtlDirectiveArgsInspection.class,
        };
    }
}
