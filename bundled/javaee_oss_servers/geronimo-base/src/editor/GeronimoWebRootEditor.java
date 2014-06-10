/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.geronimo.editor;

import com.fuhrer.idea.geronimo.model.GeronimoWebRoot;

import javax.swing.*;

class GeronimoWebRootEditor extends GeronimoRootEditor {

    GeronimoWebRootEditor(GeronimoWebRoot web) {
        super(web);
        addMainComponent(createSplitter(new GeronimoWebSettingsEditor(web), new JPanel(), false));
    }
}
