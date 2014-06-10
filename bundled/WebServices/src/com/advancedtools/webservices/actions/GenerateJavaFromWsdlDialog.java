package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.rest.RestWSEngine;
import com.advancedtools.webservices.utils.Base64Converter;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.ui.GenerateJavaCodeDialogBase;
import com.advancedtools.webservices.websphere.WebSphereWSEngine;
import com.advancedtools.webservices.wsengine.DialogWithWebServicePlatform;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ComboboxWithBrowseButton;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

/**
 * @by maxim
 */
class GenerateJavaFromWsdlDialog extends GenerateJavaCodeDialogBase implements DialogWithWebServicePlatform {
  JCheckBox generateTestCase;
  ComboBox packagePrefix;
  JCheckBox addLibs;
  private JPanel panel;
  ComboboxWithBrowseButton wsdlUrl;
  ComboBox outputPathes;
  JPasswordField password;
  JTextField user;

  private ComboBox webServicePlatform;
  private JLabel webServicePlatformText;
  private WSEngine currentEngine;
  private JLabel wsdlUrlText;
  private JLabel userText;
  private JLabel passwordText;
  private JLabel outputPathesText;
  private JLabel packagePrefixText;

  private static final String FAKE_WSDL_URL;
  static {
    FAKE_WSDL_URL = AxisUtil.getWebServiceNS("", "simpleservice") + "?wsdl";
  }

  private JLabel status;

  private JLabel statusText;
  JCheckBox generateClassesForSchemaArrays;
  ComboBox typeMappingVersion;
  private JLabel typeMappingText;

  JCheckBox wrappedDocumentSupport;
  JCheckBox generateUnreferencedElements;
  ComboBox outputMode;
  private JLabel outputModeText;
  static final String CLIENT_OUTPUT_MODE = "client";
  static final String SERVER_OUTPUT_MODE = "server";

  private ComboBox binding;
  private JLabel bindingText;
  private JCheckBox useExtensions;
  Runnable onSuccess;

  @NonNls
  private static final String WSDL_URL_IS_NOT_VALID = "Wsdl url is not valid";
  private final Module myForcedModule;

  public GenerateJavaFromWsdlDialog(Project _project, @Nullable GenerateJavaFromWsdlDialog previousDialog,
                                    @Nullable Module module, @Nullable Runnable _onSuccess) {
    super(_project);

    onSuccess = _onSuccess;
    if (onSuccess == null && previousDialog != null) {
      onSuccess = previousDialog.onSuccess;
    }

    myForcedModule = module;
    setTitle(GenerateJavaFromWsdlAction.GENERATE_JAVA_CODE_FROM_WSDL);

    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    final List<String> lastUrls = instance.getLastWsdlUrls();

    configureComboBox(wsdlUrl.getComboBox(), lastUrls);
    if (lastUrls.size() == 0) {
      wsdlUrl.getComboBox().addItem(FAKE_WSDL_URL);
      wsdlUrl.getComboBox().setSelectedIndex(0);
    }

    configureBrowseButton(
      myProject,
      wsdlUrl,
      new String[] {
        GenerateJavaFromWsdlAction.WSDL_EXTENSION,
        GenerateJavaFromWsdlAction.WADL_EXTENSION
      },
      WSBundle.message("choose.wsdl.or.wadl.file.dialog.title")
    );

    doInitFor(userText, user, 'u');
    if (previousDialog != null) user.setText(previousDialog.user.getText());

    doInitFor(passwordText, password, 'p');
    if (previousDialog != null) password.setText(previousDialog.password.getText());

    doInitFor(outputModeText, outputMode, 'm');
    if (previousDialog != null) outputMode.setSelectedItem(previousDialog.outputMode.getSelectedItem());
    
    doInitFor(bindingText, binding, 'b');
    if (previousDialog != null) binding.setSelectedItem(previousDialog.binding.getSelectedItem());

    configureComboBox(outputMode,Arrays.asList(CLIENT_OUTPUT_MODE,SERVER_OUTPUT_MODE));
    if (previousDialog != null) outputMode.setSelectedItem(previousDialog.outputMode.getSelectedItem());

    configureTypeMappingVersion(typeMappingVersion, typeMappingText);
    if (previousDialog != null) typeMappingVersion.setSelectedItem(previousDialog.typeMappingVersion.getSelectedItem());

    generateClassesForSchemaArrays.setMnemonic('e');
    if (previousDialog != null) {
      generateClassesForSchemaArrays.setSelected(previousDialog.generateClassesForSchemaArrays.isSelected());
    }

    generateTestCase.setMnemonic('g');
    if (previousDialog != null) {
      generateTestCase.setSelected(previousDialog.generateTestCase.isSelected());
    }

    generateUnreferencedElements.setMnemonic('n');
    if (previousDialog != null) {
      generateUnreferencedElements.setSelected(previousDialog.generateUnreferencedElements.isSelected());
    }

    if (previousDialog != null) wrappedDocumentSupport.setSelected(previousDialog.wrappedDocumentSupport.isSelected());
    wrappedDocumentSupport.setMnemonic('d');

    doInitFor(getUrlText(), getUrl().getComboBox(), 'w');

    if (EnvironmentFacade.isSelenaOrBetter()) addLibs.setVisible(false);

    init();
    restoreCommonFieldsFromPreviousSession(previousDialog);
    WebServicePlatformUtils.initWSPlatforms( this );
    setupWSPlatformSpecificFields();
  }

  public void setupWSPlatformSpecificFields() {
    final String currentPlatform = (String)webServicePlatform.getSelectedItem();
    currentEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(currentPlatform);
    boolean isAxis = currentPlatform.equals(AxisWSEngine.AXIS_PLATFORM);
    boolean isWebSphere = currentPlatform.equals(WebSphereWSEngine.WEBSPHERE_PLATFORM);

    generateUnreferencedElements.setVisible(isAxis || isWebSphere);
    generateClassesForSchemaArrays.setVisible(isAxis || isWebSphere);
    wrappedDocumentSupport.setVisible(isAxis || isWebSphere);
    typeMappingText.setVisible(isAxis);
    typeMappingVersion.setVisible(isAxis);
    generateTestCase.setVisible(currentEngine.allowsTestCaseGeneration());
    outputModeText.setVisible(currentEngine.hasSeparateClientServerJavaCodeGenerationOption());
    outputMode.setVisible(currentEngine.hasSeparateClientServerJavaCodeGenerationOption());

    userText.setVisible(isAxis || isWebSphere);
    user.setVisible(isAxis || isWebSphere);
    password.setVisible(isAxis || isWebSphere);
    passwordText.setVisible(isAxis || isWebSphere);

    if (!user.isVisible()) {  // other doesn't support auth?
      password.setText("");
      user.setText("");
    }

    boolean hasBindings = false;
    String[] supportedMappingTypes = currentEngine.getSupportedMappingTypesForJavaFromWsdl();

    if (supportedMappingTypes != null && supportedMappingTypes.length > 0) {
      configureComboBox(binding,Arrays.asList(supportedMappingTypes));
      final String lastBinding = WebServicesPluginSettings.getInstance().getLastBinding();
      if (lastBinding != null) binding.setSelectedItem(lastBinding);
      hasBindings = true;
    }

    binding.setVisible(hasBindings);
    bindingText.setVisible(hasBindings);

    pack();
  }

  @Nullable
  public Module getSelectedModule() {
    final Object item = outputPathes.getSelectedItem();
    return item == null ? null : LibUtils.findModuleByOutputPath(myProject, outputPathes.getSelectedItem().toString());
  }

  public JComboBox getModuleChooser() {
    return null;
  }

  protected boolean isAcceptableFile(VirtualFile virtualFile) {
    if (virtualFile != null) {
      return GenerateJavaFromWsdlAction.isAcceptableFileForGenerateJavaFromWsdl(virtualFile);
    }
    return false;
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateJavaFromWSDL.html";
  }

  public String getWebServicePlatform() {
    return (String) webServicePlatform.getSelectedItem();
  }

  public JComboBox getWebServicePlatformCombo() {
    return webServicePlatform;
  }

  public JLabel getWebServicePlaformText() {
    return webServicePlatformText;
  }

  public String getBindingType() {
    return (String) binding.getSelectedItem();
  }

  class ValidationData extends MyValidationData {
    String currentUser;
    String currentPassword;
    String currentPlatform;

    protected void doAcquire() {
      super.doAcquire();

      currentUser = user.getText();
      currentPassword = new String(password.getPassword());
      currentPlatform = (String) webServicePlatform.getSelectedItem();
    }
  }

  protected ValidationResult doValidUrlCheck(MyValidationData data) {
    return null; // we do advanced url validation below
  }

  protected ValidationResult doValidateWithData(MyValidationData _data) {
    final ValidationData data = (ValidationData) _data;
    ValidationResult validationResult = super.doValidateWithData(data);
    if (validationResult != null) {
      return validationResult;
    }

    try {
      String spec = ((String) data.currentUrl).trim();
      String userName = data.currentUser.trim();
      URL url = new URL(spec);
      final URLConnection urlConnection = url.openConnection();

      urlConnection.setRequestProperty("Accepts", "text/xml");
      if (userName.length() > 0) {
        String userPassword = userName + ":" + data.currentPassword;
        String encoding = Base64Converter.encode (userPassword.getBytes());

        urlConnection.setRequestProperty(
          "Authorization",
          "Basic " + encoding
        );
      }

      String contentType = urlConnection.getContentType();
      InputStream inputStream = null;

      try {
        inputStream = urlConnection.getInputStream();
        // ensure some initial response will come to the client to ensure absence of messages like
        // 'software aborted initiated connection' for JaxWS
        new BufferedInputStream(inputStream).read();
      } finally {
        if (inputStream != null) inputStream.close();
      }

      if ("text/html".equals(contentType)) {
        return new ValidationResult("Path to wsdl is not valid",wsdlUrl, 2000);
      }

      ValidationResult result = WebServicePlatformUtils.checkIfPlatformIsSetUpCorrectly(this, currentEngine);
      if (result != null) {
        return result;
      }

      final boolean sunRestEngine = data.currentPlatform.equals(RestWSEngine.NAME);

      if (sunRestEngine && spec.endsWith(GenerateJavaFromWsdlAction.WSDL_EXTENSION)) {
        return new ValidationResult("Only wadl files accepted for RESTful Web Services",wsdlUrl);
      } else if (!sunRestEngine && spec.endsWith(GenerateJavaFromWsdlAction.WADL_EXTENSION)) {
        return new ValidationResult("Web service engine does not support wadl files",wsdlUrl);
      }
      initiateValidation(2000); // server may go down
    } catch(MalformedURLException ex) {
      LOG.debug(ex);
      return new ValidationResult(WSDL_URL_IS_NOT_VALID,wsdlUrl);
    } catch(IllegalArgumentException ex) {
      LOG.debug(ex);
      return new ValidationResult(WSDL_URL_IS_NOT_VALID,wsdlUrl);
    } catch(IOException ex) {
      LOG.debug(ex);
      return new ValidationResult("Wsdl url connection exception", wsdlUrl, 2000);
    }

    String item = (String)packagePrefix.getSelectedItem();
    if (packagePrefix.isVisible() && (item == null || item.trim().length() == 0)) {
      return new ValidationResult("Package shouldn't be empty", packagePrefix);
    }

    return null;
  }

  public Runnable getOnSuccess() {
    return onSuccess;
  }

  protected ValidationData createValidationData() {
    return new ValidationData();
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

  protected Module getForcedModule() {
    return myForcedModule;
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
    return wsdlUrl;
  }

  protected JLabel getUrlText() {
    return wsdlUrlText;
  }

  protected void doOKAction() {
    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    instance.setLastPlatform(getWebServicePlatform());
    final String bindingType = getBindingType();
    if (bindingType != null) instance.setLastBinding(bindingType);
    super.doOKAction();
  }

  public boolean useExtensions() {
    return useExtensions.isSelected();
  }
}
