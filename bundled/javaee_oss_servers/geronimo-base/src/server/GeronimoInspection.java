/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoBundle;
import com.fuhrer.idea.geronimo.GeronimoUtil;
import com.fuhrer.idea.geronimo.model.GeronimoAppRoot;
import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeInspection;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.NotNull;

class GeronimoInspection extends JavaeeInspection {

    @SuppressWarnings({"unchecked"})
    GeronimoInspection() {
        super(GeronimoAppRoot.class, GeronimoEjbRoot.class, GeronimoWebRoot.class);
    }

    @Override
    protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
        super.checkDomElement(element, holder, helper);
        if (element instanceof GeronimoCommonRoot) {
            checkCommonRoot((GeronimoCommonRoot) element, holder);
            if (element instanceof GeronimoWebRoot) {
                checkContextRoot((GeronimoWebRoot) element, holder);
            }
        }
    }

    private void checkCommonRoot(GeronimoCommonRoot root, DomElementAnnotationHolder holder) {
        if (GeronimoUtil.isGeronimo10(root)) {
            GenericAttributeValue<String> value = root.getConfigId();
            if (StringUtil.isEmpty(value.getValue())) {
                holder.createProblem(root.getConfigId(), GeronimoBundle.getText("Error.config.id.empty"));
            }
        } else if (root instanceof GeronimoAppRoot) {
            GenericDomValue<String> value = root.getEnvironment().getModuleId().getArtifactId();
            if (StringUtil.isEmpty(value.getValue())) {
                holder.createProblem(value, GeronimoBundle.getText("Error.artifact.id.empty"));
            }
        }
    }

    private void checkContextRoot(GeronimoWebRoot root, DomElementAnnotationHolder holder) {
        GenericDomValue<String> value = root.getContextRoot();
        if ("".equals(value.getValue())) {
            holder.createProblem(value, GeronimoBundle.getText("Error.context.root.empty"));
        }
    }

    @NotNull
    public String getShortName() {
        return GeronimoBundle.getText("GeronimoIntegration.name");
    }
}
