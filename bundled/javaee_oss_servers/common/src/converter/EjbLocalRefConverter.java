/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.converter;

import com.intellij.javaee.model.xml.EjbLocalRef;
import com.intellij.javaee.model.xml.JndiEnvironmentRefsGroup;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public abstract class EjbLocalRefConverter extends ResolvingConverter<EjbLocalRef> {

    @Override
    @Nullable
    public EjbLocalRef fromString(String value, ConvertContext context) {
        return ElementPresentationManager.findByName(getVariants(context), value);
    }

    @Override
    @Nullable
    public String toString(EjbLocalRef value, ConvertContext context) {
        return (value != null) ? value.getEjbRefName().getValue() : null;
    }

    @Override
    @NotNull
    public Collection<EjbLocalRef> getVariants(ConvertContext context) {
        JndiEnvironmentRefsGroup group = getReferenceHolder(context);
        return (group != null) ? group.getEjbLocalRefs() : Collections.<EjbLocalRef>emptyList();
    }

    @Nullable
    protected abstract JndiEnvironmentRefsGroup getReferenceHolder(ConvertContext context);
}
