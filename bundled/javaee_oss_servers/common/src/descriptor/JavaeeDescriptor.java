/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.fuhrer.idea.javaee.util.DirectoryScanner;
import com.fuhrer.idea.javaee.util.FileWrapper;
import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.descriptors.ConfigFile;
import com.intellij.util.descriptors.ConfigFileMetaData;
import com.intellij.util.descriptors.ConfigFileVersion;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.ElementPresentationManager;
import org.jdom.DocType;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public abstract class JavaeeDescriptor {

    @SuppressWarnings({"ClassReferencesSubclass"})
    public static final JavaeeDescriptor APP = new JavaeeAppDescriptor();

    @SuppressWarnings({"ClassReferencesSubclass"})
    public static final JavaeeDescriptor EJB = new JavaeeEjbDescriptor();

    @SuppressWarnings({"ClassReferencesSubclass"})
    public static final JavaeeDescriptor CMP = new JavaeeCmpDescriptor();

    @SuppressWarnings({"ClassReferencesSubclass"})
    public static final JavaeeDescriptor WEB = new JavaeeWebDescriptor();

    @SuppressWarnings({"PublicStaticArrayField"})
    public static final JavaeeDescriptor[] ALL = {APP, EJB, CMP, WEB};

    private final Icon icon;

    private ConfigFileMetaData meta;

    private final Set<String> namespaces = new HashSet<String>();

    protected JavaeeDescriptor(Icon icon) {
        this.icon = icon;
    }

    public void init(Class<?> type, @NonNls final String name) {
        ElementPresentationManager.registerIcon(type, getIcon());
        final JavaeeIntegration integration = JavaeeIntegration.getInstance();
        final List<ConfigFileVersion> versions = new ArrayList<ConfigFileVersion>();
        new DirectoryScanner(".+\\.xml\\.ft") {
            @Override
            protected void handle(FileWrapper file) throws Exception {
                String template = file.getName().replaceFirst("\\.ft$", "");
                if (name.equals(integration.getNameFromTemplate(template))) {
                    String version = integration.getVersionFromTemplate(template);
                    versions.add(new ConfigFileVersion(version, template));
                    namespaces.add(getNamespace(file));
                }
            }
        }.scan("fileTemplates/j2ee", type);
        ConfigFileVersion[] tmp = versions.toArray(new ConfigFileVersion[versions.size()]);
        Arrays.sort(tmp, new Comparator<ConfigFileVersion>() {
            public int compare(ConfigFileVersion v1, ConfigFileVersion v2) {
                return v1.getName().compareTo(v2.getName());
            }
        });
        meta = new ConfigFileMetaData(getTitle(integration), name + ".xml", getPath(), tmp, null, true, true, true);
    }

    @Nullable
    public <T extends DomElement> T getRoot(@Nullable JavaeeFacet facet, @NotNull Class<T> type) {
        if (facet != null) {
            ConfigFile item = facet.getDescriptorsContainer().getConfigFile(meta);
            if (item != null) {
                XmlFile xml = item.getXmlFile();
                if (xml != null) {
                    DomFileElement<T> element = DomManager.getDomManager(facet.getModule().getProject()).getFileElement(xml, type);
                    if (element != null) {
                        return element.getRootElement();
                    }
                }
            }
        }
        return null;
    }

    boolean hasNamespace(String namespace) {
        return namespaces.contains(namespace);
    }

    ConfigFileMetaData getMetaData() {
        return meta;
    }

    Icon getIcon() {
        LayeredIcon layered = new LayeredIcon(2);
        layered.setIcon(JavaeeIntegration.getInstance().getIcon(), 0);
        layered.setIcon(icon, 1);
        return layered;
    }

    abstract String getTitle(JavaeeIntegration integration);

    abstract FacetTypeId<? extends JavaeeFacet> getFacetType();

    @NonNls
    String getPath() {
        return "META-INF";
    }

    private String getNamespace(FileWrapper file) throws JDOMException, IOException {
        Element root = JDOMUtil.loadDocument(file.getStream()).getRootElement();
        String namespace = root.getNamespaceURI();
        if (StringUtil.isEmpty(namespace)) {
            DocType type = root.getDocument().getDocType();
            namespace = (type != null) ? type.getSystemID() : "";
        }
        return namespace;
    }
}
