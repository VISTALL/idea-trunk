/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishBundle;
import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.glassfish.model.GlassfishEjbRoot;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.javaee.model.common.JavaeeCommonConstants;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class GlassfishIntegration extends JavaeeIntegration {

    @Override
    @NotNull
    public String getName() {
        return GlassfishBundle.getText("GlassfishIntegration.name");
    }

    @Override
    @NotNull
    public Icon getIcon() {
        return JavaeeBundle.getIcon("/resources/glassfish.png");
    }

    @Override
    @NotNull
    public Icon getBigIcon() {
        return JavaeeBundle.getIcon("/resources/glassfishbig.png");
    }

    @Override
    public void registerDescriptors() {
        JavaeeDescriptor.APP.init(GlassfishAppRoot.class, "sun-application");
        JavaeeDescriptor.EJB.init(GlassfishEjbRoot.class, "sun-ejb-jar");
        JavaeeDescriptor.CMP.init(GlassfishEjbRoot.class, "sun-cmp-mapping");
        JavaeeDescriptor.WEB.init(GlassfishWebRoot.class, "sun-web");
    }

    @Override
    @Nullable
    @NonNls
    public String getNameFromTemplate(String template) {
        return template.split("_")[0];
    }

    @Override
    @Nullable
    @NonNls
    public String getVersionFromTemplate(String template) {
        return template.replaceAll("[\\w-]+_(\\d)_(\\d(-\\d)?)\\.xml", "$1.$2").replace('-', '.');
    }

    @Override
    @NotNull
    protected String getServerVersion(String home) throws Exception {
        URL[] urls = null;
        if (new File(home, "lib").isDirectory()) {
            urls = new URL[]{new File(home, "lib/appserv-rt.jar").toURI().toURL()};
        } else if (new File(home, "glassfish/modules").isDirectory()) {
            List<URL> list = new ArrayList<URL>();
            for (File file : new File(home, "glassfish/modules").listFiles()) {
                list.add(file.toURI().toURL());
            }
            urls = list.toArray(new URL[list.size()]);
        }
        ClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader());
        Class<?> type = loader.loadClass("com.sun.appserv.server.util.Version");
        Field field = type.getDeclaredField("full_version");
        field.setAccessible(true);
        return (String) field.get(null);
    }

    @Override
    protected void checkValidServerHome(String home, String version) throws Exception {
        if (!GlassfishUtil.isGlassfish3(version)) {
            checkFile(home, "lib/appserv-deployment-client.jar");
            checkFile(home, "lib/appserv-ext.jar");
        }
    }

    @Override
    protected void addLibraryLocations(String home, List<File> locations) {
        locations.add(new File(home, "lib"));
        locations.add(new File(home, "glassfish/modules"));
    }

    @Override
    protected boolean allLibrariesFound(Collection<String> classes, Function<String, String> mapper) {
        boolean all = super.allLibrariesFound(classes, mapper);
        if (!all && (classes.size() == 1)) {
            all = mapper.fun(JavaeeCommonConstants.ENTITY_BEAN_CLASS).equals(classes.iterator().next());
        }
        return all;
    }
}
