package com.intellij.coldFusion.UI.inspections;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfmlInspectionToolProvider implements InspectionToolProvider {
    public Class[] getInspectionClasses() {
        return new Class[]{
                CfmlReferenceInspection.class/*, CfmlUniqueDefsInspection.class*/
        };
    }
}

