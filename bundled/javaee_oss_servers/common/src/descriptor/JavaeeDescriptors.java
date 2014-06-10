/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.descriptor;

import com.fuhrer.idea.javaee.server.JavaeeIntegration;
import com.intellij.javaee.facet.DescriptorMetaDataProvider;
import org.jetbrains.annotations.NotNull;

class JavaeeDescriptors extends DescriptorMetaDataProvider {

    @Override
    public void registerDescriptors(@NotNull MetaDataRegistry registry) {
        JavaeeIntegration integration = JavaeeIntegration.getInstance();
        integration.registerDescriptors();
        for (JavaeeDescriptor descriptor : JavaeeDescriptor.ALL) {
            if (descriptor.getMetaData() != null) {
                registry.register(descriptor.getFacetType(), integration, descriptor.getMetaData());
            }
        }
    }
}
