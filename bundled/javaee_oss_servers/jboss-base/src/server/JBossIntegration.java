/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.server;

import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.fuhrer.idea.jboss.JBossBundle;
import com.fuhrer.idea.jboss.model.JBossAppRoot;
import com.fuhrer.idea.jboss.model.JBossEjbRoot;
import com.fuhrer.idea.jboss.model.JBossWebRoot;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

class JBossIntegration extends JavaeeIntegration {

    @Override
    @NotNull
    public String getName() {
        return JBossBundle.getText("JBossIntegration.name");
    }

    @Override
    @NotNull
    public Icon getIcon() {
        return JavaeeBundle.getIcon("/resources/jboss.png");
    }

    @Override
    @NotNull
    public Icon getBigIcon() {
        return JavaeeBundle.getIcon("/resources/jbossbig.png");
    }

    @Override
    public void registerDescriptors() {
        JavaeeDescriptor.APP.init(JBossAppRoot.class, "jboss-app");
        JavaeeDescriptor.EJB.init(JBossEjbRoot.class, "jboss");
        JavaeeDescriptor.CMP.init(JBossEjbRoot.class, "jbosscmp-jdbc");
        JavaeeDescriptor.WEB.init(JBossWebRoot.class, "jboss-web");
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
        return template.replaceAll("[\\w-]+_(\\d)_(\\d)\\.xml", "$1.$2");
    }

    @Override
    @NotNull
    protected String getServerVersion(String home) throws Exception {
        File file = new File(home, "bin/run.jar");
        Attributes attributes = new JarFile(file).getManifest().getMainAttributes();
        return attributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION).split(" ")[0];
    }

    @Override
    protected void checkValidServerHome(String home, String version) throws Exception {
        checkFile(home, "client/jbossall-client.jar");
    }

    @Override
    protected void addLibraryLocations(String home, List<File> locations) {
        File[] files = new File(home, "server").listFiles();
        if (files != null) {
            for (File server : files) {
                locations.add(new File(server, "lib"));
                locations.add(new File(server, "deploy"));
            }
        }
        locations.add(new File(home, "common/lib"));
    }
}
