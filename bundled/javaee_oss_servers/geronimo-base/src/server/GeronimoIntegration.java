/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.geronimo.GeronimoBundle;
import com.fuhrer.idea.geronimo.model.GeronimoAppRoot;
import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;
import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;
import com.fuhrer.idea.javaee.JavaeeBundle;
import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.jar.JarFile;

class GeronimoIntegration extends JavaeeIntegration {

    @NonNls
    private static final String PATTERN = "([\\w-]+)-(\\d\\.\\d(:?-\\w+)?)\\.xml";

    @Override
    @NotNull
    public String getName() {
        return GeronimoBundle.getText("GeronimoIntegration.name");
    }

    @Override
    @NotNull
    public Icon getIcon() {
        return JavaeeBundle.getIcon("/resources/geronimo.png");
    }

    @Override
    @NotNull
    public Icon getBigIcon() {
        return JavaeeBundle.getIcon("/resources/geronimobig.png");
    }

    @Override
    public void registerDescriptors() {
        JavaeeDescriptor.APP.init(GeronimoAppRoot.class, "geronimo-application");
        JavaeeDescriptor.EJB.init(GeronimoEjbRoot.class, "openejb-jar");
        JavaeeDescriptor.WEB.init(GeronimoWebRoot.class, "geronimo-web");
    }

    @Override
    @Nullable
    @NonNls
    public String getNameFromTemplate(String template) throws Exception {
        return template.replaceAll(PATTERN, "$1");
    }

    @Override
    @Nullable
    @NonNls
    public String getVersionFromTemplate(String template) throws Exception {
        return template.replaceAll(PATTERN, "$2");
    }

    @Override
    @NotNull
    protected String getServerVersion(String home) throws Exception {
        @NonNls String name = "org/apache/geronimo/system/serverinfo/geronimo-version.properties";
        JarFile file = new JarFile(getFile(home));
        Properties properties = new Properties();
        properties.load(file.getInputStream(file.getEntry(name)));
        return properties.getProperty("version");
    }

    @Override
    protected void checkValidServerHome(String home, String version) throws Exception {
    }

    @Override
    protected void addLibraryLocations(String home, List<File> locations) {
        locations.add(new File(home, "repository"));
    }

    @Nullable
    private File getFile(String home) {
        File file = getFile(new File(home, "lib"));
        if (file == null) {
            file = getFile(home, "repository/org/apache/geronimo/modules/geronimo-system");
            if (file == null) {
                file = getFile(home, "repository/org/apache/geronimo/framework/geronimo-system");
            }
        }
        return file;
    }

    @Nullable
    private File getFile(String home, String path) {
        File dir = new File(home, path);
        if (dir.isDirectory()) {
            return getFile(dir.listFiles()[0]);
        }
        return null;
    }

    @Nullable
    private File getFile(File dir) {
        for (File file : dir.listFiles()) {
            if (file.getName().matches("geronimo-system-.*\\.jar")) {
                return file;
            }
        }
        return null;
    }
}
