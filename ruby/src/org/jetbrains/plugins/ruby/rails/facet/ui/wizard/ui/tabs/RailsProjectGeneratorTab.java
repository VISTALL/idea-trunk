/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.tabs;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.ProjectJdk;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.ExternalRailsSettings;
import org.jetbrains.plugins.ruby.rails.RailsUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsApplicationSettings;
import org.jetbrains.plugins.ruby.rails.facet.ui.RailsUIUtil;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.RailsWizardSettingsHolder;
import static org.jetbrains.plugins.ruby.rails.facet.ui.wizard.RailsWizardSettingsHolder.Generate;
import org.jetbrains.plugins.ruby.rails.facet.ui.wizard.ui.TabbedSettingsContext;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Apr 5, 2008
 */
public class RailsProjectGeneratorTab extends TabbedSdkDependSettingsEditorTab implements RailsUIUtil.RailsVersionComponent {
    private static final String TITLE = RBundle.message("rails.facet.wizard.tab.rails.project.generator.title");

    private JRadioButton generateNewRButton;
    private JRadioButton useExistingRButton;
    private JLabel myRailsVersionLabel;
    private JPanel myContentPane;
    private EvaluatingComponent<String> myECRailsVersionLabel;
    private JCheckBox myCBPreconfigureForSelectedDB;
    private JComboBox myCBoxDBName;
    private JTextField myRailsRootPath;

    private String myRailsVersion;
    private final RailsWizardSettingsHolder mySettingsHolder;
    /**
     * Form is closed state
     */
    private volatile boolean myIsClosed;

    public RailsProjectGeneratorTab(final RailsWizardSettingsHolder settingsHolder) {
        mySettingsHolder = settingsHolder;

        myCBPreconfigureForSelectedDB.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent e) {
                final boolean preconfigureDB = myCBPreconfigureForSelectedDB.isSelected();
                myCBoxDBName.setEnabled(preconfigureDB);
            }
        });

        myCBoxDBName.setModel(new DefaultComboBoxModel(ExternalRailsSettings.getRailsScriptDataBaseTypes()));
        
        setUpDBFromSavedSettings();
    }

    private void setUpDBFromSavedSettings() {
        final String preconfigureDBName = RailsApplicationSettings.getInstance().wizardRailsFacetPreconfigureDBName;

        myCBPreconfigureForSelectedDB.setSelected(preconfigureDBName != null);
        myCBoxDBName.setEnabled(preconfigureDBName != null);

        myCBoxDBName.setSelectedItem(preconfigureDBName);
        if (myCBoxDBName.getSelectedItem() == null) {
            myCBoxDBName.setSelectedIndex(0);
        }
    }

    @Nls
    public String getDisplayName() {
        return TITLE;
    }

    public void setContext(@NotNull final TabbedSettingsContext context) {
        final TabbedSettingsContext oldContext = getContext();
        super.setContext(context);

        if (oldContext == null || context.getSdk() != oldContext.getSdk()) {
            myRailsVersion = null;
        }
    }

    public void beforeShow() {
        myIsClosed = false;

        if (myRailsVersion == null) {

            final TabbedSettingsContext tabbedSettingsContext = getContext();
            final ProjectJdk sdk = tabbedSettingsContext.getSdk();
            final boolean isRailsSDK = RailsUtil.hasRailsSupportInSDK(sdk);

            generateNewRButton.setEnabled(isRailsSDK);
            useExistingRButton.setEnabled(true);

            generateNewRButton.setSelected(isRailsSDK);
            useExistingRButton.setSelected(!isRailsSDK);

            RailsUIUtil.setupRailsVersionEvaluator(sdk, myRailsVersionLabel, myECRailsVersionLabel, this);
        }
    }

    public JComponent createComponent() {
        return myContentPane;
    }

    /**
     * N/A
     * @return true
     */
    public boolean isModified() {
        return true;
    }

    public void apply() throws ConfigurationException {
        if (generateNewRButton.isSelected()) {
            mySettingsHolder.setAppGenerateWay(Generate.NEW);
        } else if (useExistingRButton.isSelected()) {
            mySettingsHolder.setAppGenerateWay(Generate.NOT);
        }

        final String relativePath = FileUtil.toSystemIndependentName(myRailsRootPath.getText().trim());
        mySettingsHolder.setRailsApplicationHomeDirRelativePath(relativePath);

        if (myCBPreconfigureForSelectedDB.isSelected()) {
            mySettingsHolder.setDBNameToPreconfigure((String)myCBoxDBName.getSelectedItem());
        } else {
            mySettingsHolder.setDBNameToPreconfigure(null);
        }

        //save wizard settings
        RailsApplicationSettings.getInstance().wizardRailsFacetPreconfigureDBName = mySettingsHolder.getDBNameToPreconfigure();

        myIsClosed = true;
    }


    /**
     * Resets cached sdk version
     */
    public void resetSdkSettings() {
        myRailsVersion = null;
    }

    public void reset() {
        final RailsWizardSettingsHolder.Generate appGenerateWay = mySettingsHolder.getAppGenerateWay();
        generateNewRButton.setSelected(appGenerateWay == Generate.NEW);
        useExistingRButton.setSelected(appGenerateWay != Generate.NEW);

        final String homeDirRelativePath = mySettingsHolder.getRailsApplicationHomeDirRelativePath();
        myRailsRootPath.setText(FileUtil.toSystemDependentName(homeDirRelativePath == null ? "" : homeDirRelativePath));

        setUpDBFromSavedSettings();
    }

    public void disposeUIResources() {
        // Do nothing
    }

    private void createUIComponents() {
        myRailsVersionLabel = new JLabel("");
        myECRailsVersionLabel = new EvaluatingComponent<String>(myRailsVersionLabel);   
    }

    public boolean isCloosed() {
        return myIsClosed;
    }

    public void setRailsVersion(@Nullable final String railsVersion) {
        myRailsVersion = railsVersion;
    }
}
