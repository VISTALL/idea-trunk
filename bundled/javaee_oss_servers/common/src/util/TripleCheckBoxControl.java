/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.util;

import com.intellij.util.xml.ui.BaseModifiableControl;
import com.intellij.util.xml.ui.DomWrapper;
import org.jetbrains.annotations.Nullable;

import java.awt.event.*;

public class TripleCheckBoxControl extends BaseModifiableControl<TripleCheckBox, Boolean> {

    public TripleCheckBoxControl(DomWrapper<Boolean> wrapper) {
        super(wrapper);
    }

    @Override
    protected TripleCheckBox createMainComponent(TripleCheckBox component) {
        TripleCheckBox box = component;
        if (box == null) {
            box = new TripleCheckBox();
        }
        box.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                setModified();
                commit();
            }
        });
        return box;
    }

    @Override
    @Nullable
    protected Boolean getValue() {
        return getComponent().getValue();
    }

    @Override
    protected void setValue(@Nullable Boolean value) {
        getComponent().setValue(value);
    }
}
