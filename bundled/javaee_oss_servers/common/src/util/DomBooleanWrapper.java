/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.util;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.ui.DomWrapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class DomBooleanWrapper extends DomWrapper<Boolean> {

    private final GenericDomValue<Boolean> element;

    public DomBooleanWrapper(GenericDomValue<Boolean> element) {
        this.element = element;
    }

    @Override
    @NotNull
    public DomElement getExistingDomElement() {
        return element;
    }

    @Override
    public DomElement getWrappedElement() {
        return element;
    }

    @Override
    public void setValue(Boolean value) throws IllegalAccessException, InvocationTargetException {
        element.setValue(value);
    }

    @Override
    public Boolean getValue() throws IllegalAccessException, InvocationTargetException {
        return element.isValid() ? element.getValue() : Boolean.FALSE;
    }
}
