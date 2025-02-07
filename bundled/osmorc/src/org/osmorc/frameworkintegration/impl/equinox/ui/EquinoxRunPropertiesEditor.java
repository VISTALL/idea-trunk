/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.frameworkintegration.impl.equinox.ui;

import com.intellij.openapi.options.ConfigurationException;
import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import org.osmorc.frameworkintegration.impl.equinox.EquinoxRunProperties;
import org.osmorc.run.OsgiRunConfiguration;
import org.osmorc.run.ui.FrameworkRunPropertiesEditor;

import javax.swing.*;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class EquinoxRunPropertiesEditor implements FrameworkRunPropertiesEditor {

    public EquinoxRunPropertiesEditor() {
        justTheBundlesRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnablement();
            }
        });
        productRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnablement();
            }
        });
        applicationRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateEnablement();
            }
        });
    }

    public JPanel getUI() {
        return _mainPanel;
    }

    public void resetEditorFrom(OsgiRunConfiguration osgiRunConfiguration) {
        presentationModel.getBean().load(osgiRunConfiguration.getAdditionalProperties());
        if (presentationModel.getBean().getEquinoxProduct() != null && presentationModel.getBean().getEquinoxProduct().length() > 0) {
            productRadioButton.setSelected(true);
        }
        else if (presentationModel.getBean().getEquinoxApplication() != null && presentationModel.getBean().getEquinoxApplication().length() > 0) {
            applicationRadioButton.setSelected(true);
        }
        else {
            justTheBundlesRadioButton.setSelected(true);
        }
        updateEnablement();
    }

    public void applyEditorTo(OsgiRunConfiguration osgiRunConfiguration) throws ConfigurationException {
        osgiRunConfiguration.putAdditionalProperties(presentationModel.getBean().getProperties());
    }

    private void createUIComponents() {
        presentationModel = new PresentationModel<EquinoxRunProperties>(
                new EquinoxRunProperties(new HashMap<String, String>()));
        debugModeCheckBox = BasicComponentFactory
                .createCheckBox(presentationModel.getModel(EquinoxRunProperties.DEBUG_MODE), "");
        startEquinoxOSGIConsoleCheckBox = BasicComponentFactory
                .createCheckBox(presentationModel.getModel(EquinoxRunProperties.START_EQUINOX_OSGICONSOLE), "");
        cleanEquinoxCache = BasicComponentFactory
                .createCheckBox(presentationModel.getModel(EquinoxRunProperties.CLEAN_EQUINOX_CACHE), "");
        recreateConfigIniCheckBox = BasicComponentFactory
                .createCheckBox(presentationModel.getModel(EquinoxRunProperties.RECREATE_CONFIG_INI), "");
        systemPackages =
                BasicComponentFactory.createTextField(presentationModel.getModel(EquinoxRunProperties.SYSTEM_PACKAGES));
        bootDelegation =
                BasicComponentFactory.createTextField(presentationModel.getModel(EquinoxRunProperties.BOOT_DELEGATION));
        productTextField =
                BasicComponentFactory.createTextField(presentationModel.getModel(EquinoxRunProperties.EQUINOX_PRODUCT));
        applicationTextField =
                BasicComponentFactory.createTextField(presentationModel.getModel(EquinoxRunProperties.EQUINOX_APPLICATION));
    }

    private void updateEnablement() {
        if (justTheBundlesRadioButton.isSelected()) {
            productTextField.setText("");
            productTextField.setEnabled(false);
            presentationModel.getBean().setEquinoxProduct("");
            applicationTextField.setText("");
            applicationTextField.setEnabled(false);
            presentationModel.getBean().setEquinoxApplication("");
        }
        else if (productRadioButton.isSelected()) {
            applicationTextField.setText("");
            applicationTextField.setEnabled(false);
            presentationModel.getBean().setEquinoxApplication("");
            productTextField.setEnabled(true);
        }
        else if (applicationRadioButton.isSelected()) {
            productTextField.setText("");
            productTextField.setEnabled(false);
            presentationModel.getBean().setEquinoxProduct("");
            applicationTextField.setEnabled(true);
        }
    }




    private JPanel _mainPanel;
    private JCheckBox startEquinoxOSGIConsoleCheckBox;
    private JCheckBox debugModeCheckBox;
    private JTextField systemPackages;
    private JTextField bootDelegation;
    private JRadioButton productRadioButton;
    private JRadioButton applicationRadioButton;
    private JTextField productTextField;
    private JTextField applicationTextField;
    private JRadioButton justTheBundlesRadioButton;
    private JCheckBox cleanEquinoxCache;
    private JCheckBox recreateConfigIniCheckBox;
    private PresentationModel<EquinoxRunProperties> presentationModel;
}
