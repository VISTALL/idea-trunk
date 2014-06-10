/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.JavaeeLogger;
import com.fuhrer.idea.javaee.util.DirectoryScanner;
import com.fuhrer.idea.javaee.util.FileWrapper;
import com.intellij.javaee.ResourceRegistrar;
import com.intellij.javaee.StandardResourceProvider;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class JavaeeResources implements StandardResourceProvider {

    public void registerResources(final ResourceRegistrar registrar) {
        try {
            new DirectoryScanner(".+\\.(dtd|xsd)") {
                @Override
                protected void handle(FileWrapper file) throws Exception {
                    registrar.addStdResource(getResourceUri(file), file.getPath(), getClass());
                }
            }.scan("descriptors");
        } catch (IOException e) {
            JavaeeLogger.error(e);
        }
    }

    @NotNull
    @NonNls
    protected abstract String getResourceUri(FileWrapper file) throws Exception;
}
