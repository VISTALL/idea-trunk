/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.jboss.model.converter;

import com.fuhrer.idea.jboss.model.JBossCmpBean;
import com.fuhrer.idea.jboss.model.JBossLoadGroup;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class JBossLoadGroupConverter extends ResolvingConverter<JBossLoadGroup> {

    @Override
    @Nullable
    public JBossLoadGroup fromString(String value, ConvertContext context) {
        if (value != null) {
            for (JBossLoadGroup group : getVariants(context)) {
                if (value.equals(group.getLoadGroupName().getValue())) {
                    return group;
                }
            }
        }
        return null;
    }

    @Override
    @Nullable
    public String toString(JBossLoadGroup value, ConvertContext context) {
        return (value != null) ? value.getLoadGroupName().getValue() : null;
    }

    @Override
    @NotNull
    public Collection<JBossLoadGroup> getVariants(ConvertContext context) {
        JBossCmpBean cmp = context.getInvocationElement().getParentOfType(JBossCmpBean.class, false);
        return (cmp != null) ? cmp.getLoadGroups().getLoadGroups() : Collections.<JBossLoadGroup>emptyList();
    }

    @Override
    @NotNull
    public Set<String> getAdditionalVariants(@NotNull ConvertContext context) {
        return Collections.singleton("*");
    }
}
