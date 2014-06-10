/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.server;

import com.fuhrer.idea.javaee.descriptor.JavaeeDescriptor;
import com.fuhrer.idea.javaee.descriptor.JavaeeFileDescription;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.NotNullFunction;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

abstract class GeronimoFileDescription<T> extends JavaeeFileDescription<T> {

    GeronimoFileDescription(Class<T> type, @NonNls String root, JavaeeDescriptor descriptor) {
        super(type, root, descriptor);
        registerNamespacePolicy("sys", new NotNullFunction<XmlTag, List<String>>() {
            @NotNull
            public List<String> fun(XmlTag tag) {
                for (String namespace : tag.knownNamespaces()) {
                    if (namespace.startsWith("http://geronimo.apache.org/xml/ns/deployment")) {
                        return Collections.singletonList(namespace);
                    }
                }
                return Collections.emptyList();
            }
        });
    }
}
