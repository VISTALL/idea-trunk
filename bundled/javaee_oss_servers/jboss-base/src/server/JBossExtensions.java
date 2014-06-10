/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.intellij.facet.FacetTypeId;
import com.intellij.javaee.application.facet.JavaeeApplicationFacet;
import com.intellij.javaee.ejb.facet.EjbFacet;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.util.io.FileUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.*;

@State(name = "JBossExtensions", storages = {@Storage(id = "jboss", file = "$APP_CONFIG$/jboss.extensions.xml")})
class JBossExtensions implements PersistentStateComponent<Element> {

    private final Map<FacetTypeId<? extends JavaeeFacet>, String> types = new HashMap<FacetTypeId<? extends JavaeeFacet>, String>();

    private final Map<String, Set<String>> extensions = new HashMap<String, Set<String>>();

    static JBossExtensions getInstance() {
        return ServiceManager.getService(JBossExtensions.class);
    }

    JBossExtensions() {
        initialize("app", JavaeeApplicationFacet.ID, "ear");
        initialize("ejb", EjbFacet.ID, "jar", "sar", "har", "beans", "spring", "ejb3", "par", "rar", "aop", "deployer", "esb");
        initialize("web", WebFacet.ID, "war");
    }

    public Element getState() {
        @NonNls Element element = new Element("JBossExtensions");
        for (Map.Entry<String, Set<String>> entry : extensions.entrySet()) {
            for (String extension : entry.getValue()) {
                @NonNls Element child = new Element("entry");
                child.setAttribute("type", entry.getKey());
                child.setAttribute("extension", extension);
                element.addContent(child);
            }
        }
        return element;
    }

    @SuppressWarnings({"unchecked"})
    public void loadState(@NonNls Element element) {
        for (@NonNls Element child : (Iterable<? extends Element>) element.getChildren("entry")) {
            String id = child.getAttributeValue("type");
            if (!extensions.containsKey(id)) {
                extensions.put(id, new HashSet<String>());
            }
            extensions.get(id).add(child.getAttributeValue("extension"));
        }
    }

    boolean isValidExtension(JavaeeFacet facet, File file) {
        Set<String> set = extensions.get(types.get(facet.getTypeId()));
        return (set != null) && set.contains(FileUtil.getExtension(file.getName()));
    }

    private void initialize(@NonNls String id, FacetTypeId<? extends JavaeeFacet> type, @NonNls String... extension) {
        types.put(type, id);
        if (!extensions.containsKey(id)) {
            extensions.put(id, new HashSet<String>(Arrays.asList(extension)));
        }
    }
}
