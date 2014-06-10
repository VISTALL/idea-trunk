package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.ui.GenerateJavaCodeDialogBase;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author maxim
 */
public class GenerateSchemaFromInstanceDocumentDialog extends GenerateJavaCodeDialogBase {
  private JPanel panel;
  private ComboboxWithBrowseButton generateFromUrl;
  private JLabel status;
  private JLabel statusText;
  private JLabel generateFromUrlText;
  private JLabel designTypeText;
  private JTextField detectEnumerationsLimit;
  private ComboBox detectSimpleContentTypes;
  private ComboBox designType;
  private JLabel detectEnumerationsLimitText;
  private JLabel detectSimpleContentTypesText;
  private JLabel resultSchemaFileNameText;
  private JTextField resultSchemaFileName;

  static final String LOCAL_ELEMENTS_GLOBAL_COMPLEX_TYPES = WSBundle.message("local.elements.global.complex.types.option.name");
  static final String LOCAL_ELEMENTS_TYPES = WSBundle.message("local.elements.types.option.name");
  static final String GLOBAL_ELEMENTS_LOCAL_TYPES = WSBundle.message("global.elements.local.types.option.name");

  private static final List<String> designTypes = Arrays.asList(
    LOCAL_ELEMENTS_GLOBAL_COMPLEX_TYPES,
    LOCAL_ELEMENTS_TYPES,
    GLOBAL_ELEMENTS_LOCAL_TYPES
  );

  static final String SMART_TYPE = "smart";
  private static final List<String> simpleContentTypes = Arrays.asList(
    "string",
    SMART_TYPE
  );

  public GenerateSchemaFromInstanceDocumentDialog(Project project, @Nullable GenerateSchemaFromInstanceDocumentDialog previousDialog) {
    super(project);

    setTitle(WSBundle.message("generate.schema.from.instance.document.dialog.title"));

    doInitFor(designTypeText, designType, 'd');
    configureComboBox(designType,designTypes);
    if (previousDialog != null) designType.setSelectedItem(previousDialog.designType.getSelectedItem());

    doInitFor(detectSimpleContentTypesText, detectSimpleContentTypes, 'i');
    configureComboBox(detectSimpleContentTypes, simpleContentTypes);

    if (previousDialog != null) detectSimpleContentTypes.setSelectedItem(previousDialog.detectSimpleContentTypes.getSelectedItem());

    doInitFor(detectEnumerationsLimitText, detectEnumerationsLimit, 'e');
    if (previousDialog != null) {
      detectEnumerationsLimit.setText(previousDialog.detectEnumerationsLimit.getText());
    } else {
      detectEnumerationsLimit.setText("10");
    }

    configureBrowseButton(myProject, generateFromUrl, new String[] {WebServicesPluginSettings.XML_FILE_EXTENSION}, WSBundle.message("select.xml.document.dialog.title"));
    doInitFor(generateFromUrlText, generateFromUrl.getComboBox(), 'n');

    doInitFor(resultSchemaFileNameText, resultSchemaFileName, 'f');

    init();
    startTrackingCurrentClassOrFile();

    if (previousDialog != null) {
      generateFromUrl.getComboBox().setSelectedItem(previousDialog.generateFromUrl.getComboBox().getSelectedItem());
    }

    if (previousDialog != null) {
      resultSchemaFileName.setText(previousDialog.resultSchemaFileName.getText());
    } else {
      final VirtualFile file = findFileWithUrl();
      resultSchemaFileName.setText(
        (file != null ? file.getName():"schema") + "." + WebServicesPluginSettings.XSD_FILE_EXTENSION
      );
    }
  }

  protected boolean isMultipleFileSelection() {
    return true;
  }

  protected boolean isAcceptableFile(VirtualFile virtualFile) {
    return GenerateSchemaFromInstanceDocumentAction.isAcceptableFileForGenerateSchemaFromInstanceDocument(virtualFile);
  }

  protected JCheckBox getAddLibs() {
    return null;
  }

  protected JComboBox getPackagePrefix() {
    return null;
  }

  protected JComboBox getOutputPathes() {
    return null;
  }

  protected JLabel getOutputPathesText() {
    return null;
  }

  protected JLabel getPackagePrefixText() {
    return null;
  }

  protected ComboboxWithBrowseButton getUrl() {
    return generateFromUrl;
  }

  protected JLabel getUrlText() {
    return generateFromUrlText;
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

  String getDesignType() {
    return (String) designType.getSelectedItem();
  }

  String getSimpleContentType() {
    return (String) detectSimpleContentTypes.getSelectedItem();
  }

  String getEnumerationsLimit() {
    return detectEnumerationsLimit.getText();
  }

  public Boolean checkParametersAreStillValid() {
    return isAcceptableFile(findFileWithUrl());
  }

  private VirtualFile findFileWithUrl() {
    Object o = getUrl().getComboBox().getSelectedItem();
    return o != null ? EnvironmentFacade.getInstance().findRelativeFile((String) o, null):null;
  }

  public String getTargetSchemaName() {
    return resultSchemaFileName.getText();
  }

  class MyValidationData extends GenerateJavaCodeDialogBase.MyValidationData {
    String enumerationsLimit;
    String simpleContenTypes;
    String designType;
    String resultSchemaFileName;

    protected void doAcquire() {
      super.doAcquire();
      enumerationsLimit = getEnumerationsLimit();
      simpleContenTypes = getSimpleContentType();
      designType = getDesignType();
      resultSchemaFileName = getTargetSchemaName();
    }
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateSchemaFromInstanceDocument.html";
  }

  protected GenerateJavaCodeDialogBase.MyValidationData createValidationData() {
    return new MyValidationData();
  }

  protected ValidationResult doValidate(ValidationData _data) {
    ValidationResult validationResult = GenerateJavaFromXmlBeansSchemasDialog.checkXmlBeansPathSet(this);
    if (validationResult != null) return validationResult;
    return super.doValidate(_data);
  }
  
  protected ValidationResult doValidateWithData(GenerateJavaCodeDialogBase.MyValidationData _data) {
    MyDialogWrapper.ValidationResult validationResult = super.doValidateWithData(_data);
    if (validationResult != null) return validationResult;

    MyValidationData data = (MyValidationData) _data;
    try {
      int i = Integer.parseInt(data.enumerationsLimit);
      if (i < 0) return new ValidationResult(WSBundle.message("negative.number.validation.problem"),detectEnumerationsLimit);
    } catch(NumberFormatException ex) {
      return new ValidationResult(WSBundle.message("invalid.number.validation.problem"),detectEnumerationsLimit);
    }

    if (isNotValidUrl(data.resultSchemaFileName)) {
      return new ValidationResult(WSBundle.message("result.schema.file.name.is.empty.validation.problem"),resultSchemaFileName);
    }
    return null;
  }
}
