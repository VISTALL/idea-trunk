package org.intellij.j2ee.web.resin.ui;

import com.intellij.javaee.run.configuration.CommonModel;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Factory;
import com.intellij.ui.RawCommandLineEditor;
import org.intellij.j2ee.web.resin.ResinBundle;
import org.intellij.j2ee.web.resin.ResinModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RunConfigurationEditor extends SettingsEditor<CommonModel> {
    private JTextField portField;
    private JPanel mainPanel;
    private JCheckBox debugConfiguration;
    private TextFieldWithBrowseButton resinConfSelector;
    private JTextField charset;
    private JCheckBox readOnlyConfiguration;
    private RawCommandLineEditor additionalParameters;
    private JCheckBox autoBuildClasspath;

    public RunConfigurationEditor() {
        init();
    }

    public RunConfigurationEditor(Factory<CommonModel> factory) {
        super(factory);
        init();
    }

    private void init() {
        initChooser(resinConfSelector, ResinBundle.message("message.text.settings.resin.conf.file.title"),
                    ResinBundle.message("message.text.settings.resin.conf.file.select"));
    }

    protected void resetEditorFrom(CommonModel commonModel) {
        ResinModel resinModel = ((ResinModel)commonModel.getServerModel());
        portField.setText(resinModel.PORT);
        resinConfSelector.setText(resinModel.RESIN_CONF);
        debugConfiguration.setSelected(resinModel.DEBUG_CONFIGURATION);
        readOnlyConfiguration.setSelected(resinModel.READ_ONLY_CONFIGURATION);
        autoBuildClasspath.setSelected(resinModel.AUTO_BUILD_CLASSPATH);
        charset.setText(resinModel.CHARSET == null ? "" : resinModel.CHARSET);
        additionalParameters.setText(resinModel.ADDITIONAL_PARAMETERS == null ? "" : resinModel.ADDITIONAL_PARAMETERS);
    }

    protected void applyEditorTo(CommonModel commonModel) throws ConfigurationException {
        ResinModel resinModel = ((ResinModel)commonModel.getServerModel());
        resinModel.PORT = portField.getText();
        resinModel.RESIN_CONF = resinConfSelector.getText();
        resinModel.DEBUG_CONFIGURATION = debugConfiguration.isSelected();
        resinModel.READ_ONLY_CONFIGURATION = readOnlyConfiguration.isSelected();
        resinModel.AUTO_BUILD_CLASSPATH = autoBuildClasspath.isSelected();
        resinModel.CHARSET = charset.getText();
        resinModel.ADDITIONAL_PARAMETERS = additionalParameters.getText();
    }

    @NotNull
    protected JComponent createEditor() {
        return mainPanel;
    }

    protected void disposeEditor() {

    }

    private void initChooser(TextFieldWithBrowseButton field, String title, String description) {
        field.setText("");
        field.getTextField().setEditable(true);
        field.addBrowseFolderListener(title,
                description,
                null,
                new FileChooserDescriptor(true, false, false, false, false, false));
    }

}
