package com.advancedtools.webservices.jaxb;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.ui.GenerateJavaCodeDialogBase;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @by maxim
 */
class GenerateJavaFromJAXBSchemasDialog extends GenerateJavaCodeDialogBase {
  private ComboBox packagePrefix;
  private JCheckBox addLibs;
  private JPanel panel;

  private ComboBox outputPathes;

  private JLabel outputPathesText;
  private JLabel packagePrefixText;

  private JLabel status;
  private JLabel statusText;
  private ComboboxWithBrowseButton generateFromUrl;
  private JLabel generateFromUrlText;
  private JCheckBox markGeneratedCode;
  private JCheckBox generatePackageLevelAnnotations;
  private JCheckBox enableSourceLocationSupport;
  private JCheckBox generateSynchronizedAccessors;
  private JCheckBox makeFilesReadOnly;

  public GenerateJavaFromJAXBSchemasDialog(Project _project, @Nullable GenerateJavaFromJAXBSchemasDialog previousDialog) {
    super(_project);

    setTitle(WSBundle.message("generate.java.code.from.xml.schema.using.jaxb.dialog.title"));

    configureBrowseButton(
      myProject,
      generateFromUrl,
      new String[] {
        WebServicesPluginSettings.XSD_FILE_EXTENSION,
        GenerateJavaFromJAXBSchemasAction.DTD_FILE_EXTENTION,
        WebServicesPluginSettings.WSDL_FILE_EXTENSION},
      "Select Xml Schema File For JAXB Generation",
      true
    );

    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    final List<String> lastUrls = instance.getLastJAXBUrls();

    configureComboBox(getUrl().getComboBox(), lastUrls);
    doInitFor(getUrlText(), getUrl().getComboBox(), 'c');

    generatePackageLevelAnnotations.setMnemonic('p');
    if (previousDialog != null) {
      generatePackageLevelAnnotations.setSelected(previousDialog.generatePackageLevelAnnotations.isSelected());
    }
    
    generateSynchronizedAccessors.setMnemonic('y');
    enableSourceLocationSupport.setMnemonic('e');

    enableSourceLocationSupport.setVisible(false);
    generateSynchronizedAccessors.setVisible(false);

    markGeneratedCode.setMnemonic('g');
    makeFilesReadOnly.setMnemonic('r');
    makeFilesReadOnly.setSelected(
      instance.toMakeSelectedFilesReadOnly()
    );

    init();
    restoreCommonFieldsFromPreviousSession(previousDialog);
    startTrackingCurrentClassOrFile();
  }

  protected boolean isAcceptableFile(VirtualFile virtualFile) {
    if (virtualFile != null) {
      return GenerateJavaFromJAXBSchemasAction.isAcceptableFileForGenerateJAXBJavaFromSchema(virtualFile);
    }
    return false;
  }

  protected ValidationResult doValidate(ValidationData _data) {
    Module selectedModule = ApplicationManager.getApplication().runReadAction(new Computable<Module>() {
      public Module compute() {
        return LibUtils.findModuleByOutputPath(myProject, outputPathes.getSelectedItem().toString());
      }
    });
    MyDialogWrapper.ValidationResult validationResult = GenerateJAXBSchemasFromJavaDialog.checkJWSDPPathSet(
      this,
      selectedModule
    );

    if (validationResult != null) return validationResult;
    return super.doValidate(_data);
  }

  protected void doOKAction() {
    WebServicesPluginSettings.getInstance().setToMakeSelectedFilesReadOnly(toMakeGeneratedCodeReadOnly());
    super.doOKAction();
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateJavaFromJAXBSchemas.html";
  }

  protected JLabel getStatusTextField() {
    return statusText;
  }

  protected JLabel getStatusField() {
    return status;
  }

  protected JComponent createCenterPanel() {
    return panel;
  }

  protected JCheckBox getAddLibs() {
    return addLibs;
  }

  protected JComboBox getPackagePrefix() {
    return packagePrefix;
  }

  protected JComboBox getOutputPathes() {
    return outputPathes;
  }

  protected JLabel getOutputPathesText() {
    return outputPathesText;
  }

  protected JLabel getPackagePrefixText() {
    return packagePrefixText;
  }

  protected ComboboxWithBrowseButton getUrl() {
    return generateFromUrl;
  }

  protected JLabel getUrlText() {
    return generateFromUrlText;
  }

  public boolean toMarkGeneratedCodeWithAnnotations() {
    return markGeneratedCode.isSelected();
  }

  public boolean toEnableSourceLocationSupport() {
    return enableSourceLocationSupport.isSelected();
  }

  public boolean toCreateSynchronizedMethods() {
    return generateSynchronizedAccessors.isSelected();
  }

  public boolean toCreatePackageLevelAnnotations() {
    return generatePackageLevelAnnotations.isSelected();
  }

  public boolean toMakeGeneratedCodeReadOnly() {
    return makeFilesReadOnly.isSelected();
  }
}
