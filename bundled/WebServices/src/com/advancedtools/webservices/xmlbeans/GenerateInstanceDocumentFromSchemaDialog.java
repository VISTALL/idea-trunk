package com.advancedtools.webservices.xmlbeans;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.ui.GenerateJavaCodeDialogBase;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.ComboboxWithBrowseButton;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @by maxim
 */
public class GenerateInstanceDocumentFromSchemaDialog extends GenerateJavaCodeDialogBase {
  private JPanel panel;
  private ComboboxWithBrowseButton generateFromUrl;
  private JLabel status;
  private JLabel statusText;
  private JLabel generateFromUrlText;
  private ComboBox rootElementChooser;
  private JLabel rootElementChooserText;
  private JCheckBox enableRestrictionCheck;
  private JCheckBox enableUniqueCheck;
  private JTextField outputFileName;
  private JLabel outputFileNameText;
  private String previousUri;

  public GenerateInstanceDocumentFromSchemaDialog(Project project, @Nullable GenerateInstanceDocumentFromSchemaDialog previousDialog) {
    super(project);

    configureBrowseButton(
      myProject,
      generateFromUrl,
      new String[] {WebServicesPluginSettings.XSD_FILE_EXTENSION},
      WSBundle.message("select.schema.document.dialog.title")
    );

    if (previousDialog != null) {
      generateFromUrl.getComboBox().setSelectedItem(
        previousDialog.generateFromUrl.getComboBox().getSelectedItem()
      );
    }

    generateFromUrl.getComboBox().addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateFile();
        addFileUpdateRequest();
      }
    });

    enableUniqueCheck.setMnemonic('u');

    if (previousDialog != null) enableUniqueCheck.setSelected(previousDialog.enableUniqueCheck.isSelected());

    enableRestrictionCheck.setMnemonic('r');

    if (previousDialog != null) enableRestrictionCheck.setSelected(previousDialog.enableRestrictionCheck.isSelected());

    doInitFor(rootElementChooserText, rootElementChooser, 'e');

    if (previousDialog != null) rootElementChooser.setSelectedItem(previousDialog.rootElementChooser.getSelectedItem());

    doInitFor(generateFromUrlText, generateFromUrl.getComboBox(), 'c');
    doInitFor(outputFileNameText, outputFileName, 'o');

    setTitle(WSBundle.message("generate.instance.document.from.schema.dialog.title"));

    init();
    startTrackingCurrentClassOrFile();

    if (previousDialog != null) {
      outputFileName.setText(previousDialog.outputFileName.getText());
    } else {
      VirtualFile file = findVirtualFileFromUrl();
      outputFileName.setText((file != null ? file.getName():"instance") + "." + WebServicesPluginSettings.XML_FILE_EXTENSION);
    }
  }

  protected VirtualFile findFileByUrl(String s) {
    if (s.startsWith("jar:")) {
      try {
        return VfsUtil.findFileByURL(new URL(s));
      } catch (MalformedURLException e) {}
    }
    return super.findFileByUrl(s);
  }

  VirtualFile findVirtualFileFromUrl() {
    String s = (String) getUrl().getComboBox().getSelectedItem();
    return s != null ? findFileByUrl(s):null;
  }

  private void addFileUpdateRequest() {
    myAlarm.addRequest(new Runnable() {
      public void run() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            updateFile();
          }
        });
      }
    }, 2000);
  }

  public void configureComboBox(JComboBox combo, List<String> lastValues) {  // without -editor.selectAll- no focus
    combo.setModel(new DefaultComboBoxModel(lastValues.toArray(new String[lastValues.size()])));
  }
    
  private void updateFile() {
    String uri = (String) generateFromUrl.getComboBox().getSelectedItem();
    boolean hasPrevious = (previousUri != null && previousUri.equals(uri));
    final PsiFile psifile = findFile(uri);
    List<String> myRootValues;

    if (psifile == null) {
      configureComboBox(rootElementChooser, Collections.EMPTY_LIST);
      return;
    }

    final XmlTag rootTag = getRootTag(psifile);

    if (rootTag == null) {
      configureComboBox(rootElementChooser, Collections.EMPTY_LIST);
      rootElementChooser.setSelectedIndex(-1);
      previousUri = uri;
      return;
    }

    myRootValues = Xsd2InstanceUtils.addVariantsFromRootTag(rootTag);

    Object selectedItem = rootElementChooser.getSelectedItem();
    configureComboBox(rootElementChooser, myRootValues);

    if (hasPrevious) {
      rootElementChooser.setSelectedItem(selectedItem);
    } else {
      rootElementChooser.setSelectedIndex(myRootValues.size() > 0 ? 0:-1);
    }
    previousUri = uri;
  }

  private XmlTag getRootTag(PsiFile psifile) {
    return ((XmlFile) psifile).getDocument().getRootTag();
  }

  private PsiFile findFile(String uri) {
    final VirtualFile file = uri != null ? EnvironmentFacade.getInstance().findRelativeFile(uri, null):null;
    return file != null ? PsiManager.getInstance(myProject).findFile(file):null;
  }

  public String getOutputFileName() {
    return outputFileName.getText();
  }

  public Boolean areCurrentParametersStillValid() {
    updateFile();
    return rootElementChooser.getSelectedItem() != null;
  }

  class MyValidationData extends GenerateJavaCodeDialogBase.MyValidationData {
    String rootElementName;
    String outputFileNameValue;

    protected void doAcquire() {
      super.doAcquire();
      rootElementName = (String) rootElementChooser.getEditor().getItem();
      outputFileNameValue = getOutputFileName();
    }
  }

  protected MyValidationData createValidationData() {
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

    addFileUpdateRequest();
    if (data.rootElementName == null || data.rootElementName.length() == 0) {
      return new ValidationResult(WSBundle.message("schema2.instance.no.valid.root.element.name.validation.error"), rootElementChooser);
    }

    final PsiFile psiFile = findFile((String) data.currentUrl);
    if (psiFile instanceof XmlFile) {
      final XmlTag tag = getRootTag(psiFile);
      if (tag != null) {
        final XmlElementDescriptor descriptor = Xsd2InstanceUtils.getDescriptor(tag, data.rootElementName);

        if (descriptor == null) {
          return new ValidationResult(WSBundle.message("schema2.instance.no.valid.root.element.name.validation.error"), rootElementChooser);
        }
      }
    }

    if (isNotValidUrl(data.outputFileNameValue)) {
      return new ValidationResult(WSBundle.message("schema2.instance.output.file.name.is.empty.validation.problem"),outputFileName);
    }
    return validationResult;

  }

  protected boolean isAcceptableFile(VirtualFile virtualFile) {
    return GenerateInstanceDocumentFromSchemaAction.isAcceptableFileForGenerateSchemaFromInstanceDocument(virtualFile);
  }

  public void setCurrentFile(PsiFile aFile) {
    super.setCurrentFile(aFile);
    updateFile();
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

  boolean enableUniquenessCheck() {
    return enableUniqueCheck.isSelected();
  }

  boolean enableRestrictionCheck() {
    return enableRestrictionCheck.isSelected();
  }

  String getElementName() {
    return (String)rootElementChooser.getSelectedItem();
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateInstanceDocumentFromSchema.html";
  }
}
