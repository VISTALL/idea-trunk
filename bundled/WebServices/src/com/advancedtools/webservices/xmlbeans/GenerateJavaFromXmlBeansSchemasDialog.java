package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.ui.GenerateDialogBase;
import com.advancedtools.webservices.utils.ui.GenerateJavaCodeDialogBase;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @by maxim
 */
class GenerateJavaFromXmlBeansSchemasDialog extends GenerateJavaCodeDialogBase {
  private JCheckBox addLibs;
  private JPanel panel;
  private ComboboxWithBrowseButton outputFileName;
  private JLabel outputPathesText;

  private JLabel status;
  private JLabel statusText;
  private ComboboxWithBrowseButton generateFromUrl;
  private JLabel generateFromUrlText;
  private boolean mySelectedJarFile;

  public GenerateJavaFromXmlBeansSchemasDialog(Project _project, @Nullable GenerateJavaFromXmlBeansSchemasDialog previousDialog) {
    super(_project);

    setTitle(WSBundle.message("generate.java.code.from.xmlbeans.schemas.dialog.title"));

    configureBrowseButton(
      myProject,
      generateFromUrl,
      new String[] {
        WebServicesPluginSettings.XSD_FILE_EXTENSION,
        WebServicesPluginSettings.WSDL_FILE_EXTENSION
      },
      "Select Xml Schema / Wsdl File For Generation"
    );

    configureBrowseButton(myProject, outputFileName, new String[] { GenerateJavaFromXmlBeansSchemasAction.JAR_FILE_EXTENSION }, "Select Jar File To Overwrite");

    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    final List<String> lastUrls = instance.getLastXmlBeansUrls();

    configureComboBox(getUrl().getComboBox(), lastUrls);
    doInitFor(getUrlText(), getUrl().getComboBox(), 'c');

    doInitFor(outputPathesText,outputFileName.getComboBox(), 'o');

    if (previousDialog != null) {
      outputFileName.getComboBox().setSelectedItem(previousDialog.outputFileName.getComboBox().getSelectedItem());
    } else {
      outputFileName.getComboBox().setSelectedItem("types.jar");
    }

    init();
    startTrackingCurrentClassOrFile();
  }

  protected void initVirtualFile(VirtualFile virtualFile) {
    if (GenerateJavaFromXmlBeansSchemasAction.JAR_FILE_EXTENSION.equals(virtualFile.getExtension())) {
      mySelectedJarFile = true;
      outputFileName.getComboBox().setSelectedItem(fixIDEAUrl(virtualFile.getUrl()));
    } else {
      super.initVirtualFile(virtualFile);
    }
  }

  protected boolean isAcceptableFile(VirtualFile virtualFile) {
    if (virtualFile != null) {
      return GenerateJavaFromXmlBeansSchemasAction.isAcceptableFileForJavaFromXmlBeans(virtualFile);
    }
    return false;
  }

  protected ValidationResult doValidate(ValidationData _data) {
    ValidationResult validationResult = checkXmlBeansPathSet(this);
    if (validationResult != null) return validationResult;
    return super.doValidate(_data);
  }

  protected ValidationResult doValidateWithData(final GenerateJavaCodeDialogBase.MyValidationData data) {
    MyDialogWrapper.ValidationResult validationResult = super.doValidateWithData(data);
    if (validationResult != null) return validationResult;

    MyValidationData mydata = (MyValidationData) data;
    if (mydata.outputFileNameValue == null ||
        !mydata.outputFileNameValue.endsWith("."+ GenerateJavaFromXmlBeansSchemasAction.JAR_FILE_EXTENSION)
       ) {
      return new ValidationResult("Invalid output JAR file name", outputFileName.getComboBox());
    }

    return ApplicationManager.getApplication().runReadAction(new Computable<ValidationResult>() {
      public ValidationResult compute() {
        final VirtualFile relativeFile = EnvironmentFacade.getInstance().findRelativeFile(VfsUtil.fixURLforIDEA((String) data.currentUrl), null);
        final XmlFile file = (XmlFile) PsiManager.getInstance(myProject).findFile(relativeFile);
        final XmlTag rootTag = file.getDocument().getRootTag();

        if (rootTag != null) {
          // TODO!
          final String xmlnsValue = rootTag.getAttributeValue("xmlns");
          final String targetNsValue = rootTag.getAttributeValue("targetNamespace");
          return null;
        } else {
          return new ValidationResult("No root tag", getUrl());
        }
      }
    });
  }

  class MyValidationData extends GenerateJavaCodeDialogBase.MyValidationData {
    String outputFileNameValue;

    protected void doAcquire() {
      super.doAcquire();
      outputFileNameValue = (String) outputFileName.getComboBox().getEditor().getItem();
    }
  }

  protected GenerateJavaCodeDialogBase.MyValidationData createValidationData() {
    return new MyValidationData();
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateJavaFromXmlBeansSchemas.html";
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
    return null;
  }

  protected JComboBox getOutputPathes() {
    return null;
  }

  protected JLabel getOutputPathesText() {
    return outputPathesText;
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

  static ValidationResult checkXmlBeansPathSet(GenerateDialogBase dialog) {
    if (WebServicesPluginSettings.getInstance().getXmlBeansPath() == null) {
      return dialog.new ValidationResult("Please, specify XmlBeans Path in plugin settings", null, 2000);
    }
    return null;
  }

  public String getOutputFileName() {
    return (String) outputFileName.getComboBox().getSelectedItem();
  }
}
