package org.intellij.j2ee.web.resin.ui;

import com.intellij.javaee.appServerIntegrations.ApplicationServerPersistentDataEditor;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import org.intellij.j2ee.web.resin.ResinBundle;
import org.intellij.j2ee.web.resin.ResinPersistentData;
import org.intellij.j2ee.web.resin.resin.ResinInstallation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.io.File;

public class SelectResinLocationEditor extends ApplicationServerPersistentDataEditor<ResinPersistentData> {
    //Constants
    private static final String RESIN_CONF_FILE = "/conf/resin.xml";
    private static final String OLD_RESIN_CONF_FILE = "/conf/resin.conf";

    //Variables
    private JPanel mainPanel;
    private TextFieldWithBrowseButton resinHomeSelector;
    private JLabel resinVersionLabel;
    private JCheckBox includeAllResinjarsCheckbox;
    private TextFieldWithBrowseButton defaultResinConf;

    private boolean suggestResinConf = false;

    public SelectResinLocationEditor() {
        initChooser(resinHomeSelector, ResinBundle.message("message.text.locator.resin.home.title"),
                ResinBundle.message("message.text.locator.resin.home.summary"), false, true);
        initChooser(defaultResinConf, ResinBundle.message("message.text.locator.resin.conf.title"),
                ResinBundle.message("message.text.locator.resin.conf.summary"), true, false);

        resinHomeSelector.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                update();
            }
        });

        defaultResinConf.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(DocumentEvent event) {
                //Don't boile user with suggestions when it changes manually resin conf 
                suggestResinConf = defaultResinConf.getText() == null;
            }
        });

        includeAllResinjarsCheckbox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                update();
            }
        });
    }

    private void update() {
        String homeDir = resinHomeSelector.getText();

        ResinInstallation resinInstallation = new ResinInstallation(new File(homeDir));
        if (resinInstallation.isResinHome()) {
            resinVersionLabel.setText(resinInstallation.getVersion().toString());

            //Auto-select resin.conf on creating new app server instance
            if(suggestResinConf){
                File resinConfDef = new File(homeDir + RESIN_CONF_FILE);
                if(resinConfDef.exists())
                    defaultResinConf.setText(resinConfDef.getAbsoluteFile().getAbsolutePath());
                else{
                    resinConfDef = new File(homeDir + OLD_RESIN_CONF_FILE);
                    if(resinConfDef.exists())
                        defaultResinConf.setText(resinConfDef.getAbsoluteFile().getAbsolutePath());
                }
            }
        }
        else {
            resinVersionLabel.setText(ResinBundle.message("location.dlg.detected.version.unknown"));
        }

    }


    protected void resetEditorFrom(ResinPersistentData resinPersistentData) {
        this.suggestResinConf = resinPersistentData.RESIN_HOME == null;

        resinHomeSelector.setText(resinPersistentData.RESIN_HOME);
        includeAllResinjarsCheckbox.setSelected(resinPersistentData.INCLUDE_ALL_JARS);
        defaultResinConf.setText(resinPersistentData.RESIN_CONF);
        update();
    }

    protected void applyEditorTo(ResinPersistentData data) throws ConfigurationException {
        File home = new File(resinHomeSelector.getText()).getAbsoluteFile();
        checkIsDirectory(home);
        checkIsDirectory(new File(home, "bin"));
        checkIsDirectory(new File(home, "lib"));

        data.RESIN_HOME = home.getAbsolutePath();
        data.RESIN_CONF = defaultResinConf.getText();
        data.INCLUDE_ALL_JARS = includeAllResinjarsCheckbox.isSelected();
    }

    private void checkIsDirectory(File file) throws ConfigurationException {
        if (!file.isDirectory()) {
            throw new ConfigurationException(ResinBundle.message("location.dlg.error.dir.not.found", file.getAbsolutePath()));
        }
    }

    @NotNull
    protected JComponent createEditor() {
        return mainPanel;
    }

    protected void disposeEditor() {
    }

    private void initChooser(TextFieldWithBrowseButton field, String title, String description, boolean chooseFiles, boolean chooseDirs) {
        field.setText("");
        field.getTextField().setEditable(true);
        field.addBrowseFolderListener(title, description, null,
                new FileChooserDescriptor(chooseFiles, chooseDirs, false, false, false, false));
    }

}
