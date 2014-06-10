/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.javaee.util;

import com.incors.plaf.alloy.AlloyCheckBoxUI;
import com.incors.plaf.alloy.AlloyIconFactory;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.plaf.metal.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.List;

public class TripleCheckBox extends JCheckBox {

    private static final Icon METAL_ICON = new TripleCheckBoxIcon(MetalIconFactory.getCheckBoxIcon());

    private static final Icon ALLOY_ICON = new TripleCheckBoxIcon(AlloyIconFactory.getCheckBoxIcon());

    private static final List<Boolean> values = Arrays.asList(null, Boolean.TRUE, Boolean.FALSE);

    private int state;

    public TripleCheckBox() {
        setModel(new ToggleButtonModel() {
            @Override
            public void setSelected(boolean selected) {
                state = (state + 1) % 3;
                fireStateChanged();
                fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.SELECTED));
            }

            @Override
            public boolean isSelected() {
                return Boolean.TRUE.equals(getValue());
            }
        });
    }

    @Nullable
    public Boolean getValue() {
        return values.get(state);
    }

    public void setValue(@Nullable Boolean value) {
        state = values.indexOf(value);
        repaint();
    }

    @Override
    public void updateUI() {
        super.updateUI();
        setIcon((getUI() instanceof AlloyCheckBoxUI) ? ALLOY_ICON : METAL_ICON);
    }
}
