/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoAppRoot;

import javax.swing.*;

class GeronimoAppRootEditor extends GeronimoRootEditor {

    GeronimoAppRootEditor(GeronimoAppRoot root) {
        super(root);
        addMainComponent(new JPanel());
    }
}
