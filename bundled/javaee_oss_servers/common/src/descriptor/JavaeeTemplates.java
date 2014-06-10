/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.javaee.facet.DescriptorMetaDataRegistry;
import com.intellij.util.descriptors.ConfigFileVersion;

import javax.swing.*;

class JavaeeTemplates implements FileTemplateGroupDescriptorFactory {

    public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
        DescriptorMetaDataRegistry.getInstance().getProvider(JavaeeDescriptor.APP.getFacetType());
        String name = JavaeeIntegration.getInstance().getPresentableName();
        Icon icon = JavaeeIntegration.getInstance().getIcon();
        FileTemplateGroupDescriptor root = new FileTemplateGroupDescriptor(name, icon);
        for (JavaeeDescriptor descriptor : JavaeeDescriptor.ALL) {
            if (descriptor.getMetaData() != null) {
                getFileTemplates(root, descriptor);
            }
        }
        return root;
    }

    private void getFileTemplates(FileTemplateGroupDescriptor root, JavaeeDescriptor descriptor) {
        Icon icon = descriptor.getIcon();
        ConfigFileVersion[] versions = descriptor.getMetaData().getVersions();
        if (versions.length == 1) {
            root.addTemplate(new FileTemplateDescriptor(versions[0].getTemplateName(), icon));
        } else {
            FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(descriptor.getMetaData().getTitle(), icon);
            for (ConfigFileVersion version : versions) {
                group.addTemplate(new FileTemplateDescriptor(version.getTemplateName(), icon));
            }
            root.addTemplate(group);
        }
    }
}
