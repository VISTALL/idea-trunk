/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model.converter;

import com.fuhrer.idea.javaee.converter.ResourceRefConverter;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

public class JBossResourceRefConverter extends ResourceRefConverter {

    @Override
    @Nullable
    protected JndiEnvironmentRefsGroup getReferenceHolder(ConvertContext context) {
        return JBossReferenceUtil.getReferenceHolder(context);
    }
}
