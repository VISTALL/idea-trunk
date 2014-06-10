/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoEjbRoot;

import javax.swing.*;

class GeronimoEjbRootEditor extends GeronimoRootEditor {

    GeronimoEjbRootEditor(GeronimoEjbRoot root) {
        super(root);
        addMainComponent(new JPanel());
    }
}
