/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.converter;

import com.intellij.javaee.CommonModelManager;
import com.intellij.javaee.model.xml.ejb.CmpField;
import com.intellij.javaee.model.xml.ejb.EntityBean;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public abstract class CmpFieldConverter extends ResolvingConverter<CmpField> {

    @Override
    @Nullable
    public CmpField fromString(String value, ConvertContext context) {
        EntityBean bean = getEntityBean(context);
        return (bean != null) ? ElementPresentationManager.findByName(bean.getCmpFields(), value) : null;
    }

    @Override
    @Nullable
    public String toString(CmpField value, ConvertContext context) {
        return (value != null) ? value.getFieldName().getValue() : null;
    }

    @Override
    @NotNull
    public Collection<CmpField> getVariants(ConvertContext context) {
        EntityBean bean = getEntityBean(context);
        if (bean != null) {
            final CommonModelManager manager = CommonModelManager.getInstance();
            return ContainerUtil.map2List(bean.getCmpFields(), new Function<com.intellij.javaee.model.common.ejb.CmpField, CmpField>() {
                public CmpField fun(com.intellij.javaee.model.common.ejb.CmpField field) {
                    return manager.getDomElement(field);
                }
            });
        }
        return Collections.emptyList();
    }

    @Nullable
    protected abstract EntityBean getEntityBean(ConvertContext context);
}
