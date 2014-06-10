/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo;

import com.fuhrer.idea.geronimo.model.GeronimoAppRoot;
import com.fuhrer.idea.geronimo.model.GeronimoCommonRoot;
import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.descriptors.ConfigFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.Nullable;

public class GeronimoUtil {

    private GeronimoUtil() {
    }

    public static boolean isGeronimo10(GeronimoCommonRoot root) {
        XmlTag tag = DomUtil.getFileElement(root).getRootTag();
        return (tag != null) && tag.getNamespace().endsWith(".0");
    }

    @Nullable
    public static GeronimoAppRoot getAppRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.APP.getRoot(facet, GeronimoAppRoot.class);
    }

    @Nullable
    public static GeronimoEjbRoot getEjbRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.EJB.getRoot(facet, GeronimoEjbRoot.class);
    }

    @Nullable
    public static GeronimoWebRoot getWebRoot(@Nullable JavaeeFacet facet) {
        return JavaeeDescriptor.WEB.getRoot(facet, GeronimoWebRoot.class);
    }

    @Nullable
    public static GeronimoCommonRoot getCommonRoot(DeploymentModel deployment) {
        JavaeeFacet facet = deployment.getFacet();
        if (facet != null) {
            for (ConfigFile config : facet.getDescriptorsContainer().getConfigFiles()) {
                XmlFile file = config.getXmlFile();
                if (file != null) {
                    DomManager manager = DomManager.getDomManager(facet.getModule().getProject());
                    DomFileElement<GeronimoCommonRoot> element = manager.getFileElement(file, GeronimoCommonRoot.class);
                    if (element != null) {
                        return element.getRootElement();
                    }
                }
            }
        }
        return null;
    }
}
