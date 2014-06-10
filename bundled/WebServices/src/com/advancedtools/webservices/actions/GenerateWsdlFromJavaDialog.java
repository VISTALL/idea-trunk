package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.axis2.Axis2WSEngine;
import com.advancedtools.webservices.jaxrpc.JaxRPCWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.utils.DeployUtils;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.ui.GenerateFromJavaCodeDialogBase;
import com.advancedtools.webservices.websphere.WebSphereWSEngine;
import com.advancedtools.webservices.wsengine.DialogWithWebServicePlatform;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @by maxim
 */
class GenerateWsdlFromJavaDialog extends GenerateFromJavaCodeDialogBase implements DialogWithWebServicePlatform {
  private JPanel myPanel;
  private JLabel className;
  private JLabel statusText;
  private JLabel status;

  private ComboBox typeMappingVersion;
  private JLabel typeMappingVersionText;
  private ComboBox soapAction;
  private JLabel soapActionText;

  private ComboBox bindingStyle;
  private ComboBox useItemsInBindings;
  private JLabel bindingStyleText;
  private JLabel useItemsInBindingsText;

  private ComboBox generationType;
  private JLabel generationTypeText;
  private JTable methodsTable;

  private JLabel webServicePlatformText;
  private ComboBox webServicePlatform;
  private WSEngine currentEngine;
  private JScrollPane methodTablePane;
  private JTextField webServiceNamespace;
  private JLabel webServiceNamespaceText;
  private JLabel webServiceURLText;
  private JTextField webServiceURL;

  public GenerateWsdlFromJavaDialog(Project _project, PsiClass clazz, @Nullable GenerateWsdlFromJavaDialog previousDialog) {
    super(_project, clazz);

    setTitle(WSBundle.message("generate.wsdl.from.java.dialog.title"));
    configureTypeMappingVersion(typeMappingVersion, typeMappingVersionText);
    if (previousDialog != null) typeMappingVersion.setSelectedItem(previousDialog.typeMappingVersion.getSelectedItem());

    configureComboBox(soapAction, Arrays.asList("DEFAULT", "OPERATION", "NONE"));
    doInitFor(soapActionText, soapAction, 'o');
    if (previousDialog != null) soapAction.setSelectedItem(previousDialog.soapAction.getSelectedItem());

    configureComboBox(bindingStyle, Arrays.asList(WSEngine.WS_DOCUMENT_STYLE, WSEngine.WS_RPC_STYLE, WSEngine.WS_WRAPPED_STYLE));
    doInitFor(bindingStyleText, bindingStyle, 'b');
    if (previousDialog != null) bindingStyle.setSelectedItem(previousDialog.bindingStyle.getSelectedItem());

    configureComboBox(useItemsInBindings, Arrays.asList(WSEngine.WS_USE_LITERAL, WSEngine.WS_USE_ENCODED));
    doInitFor(useItemsInBindingsText, useItemsInBindings, 'u');
    if (previousDialog != null) useItemsInBindings.setSelectedItem(previousDialog.useItemsInBindings.getSelectedItem());

    configureComboBox(generationType, Arrays.asList("All", "Interface", "Implementation"));
    doInitFor(generationTypeText, generationType, 'g');

    generationType.setVisible(false);
    generationTypeText.setVisible(false);

    doInitFor(webServiceURLText, webServiceURL, 'e');
    if (previousDialog != null) webServiceURL.setText(previousDialog.webServiceURL.getText());

    doInitFor(webServiceNamespaceText, webServiceNamespace, 'n');
    if (previousDialog != null) webServiceNamespace.setText(previousDialog.webServiceNamespace.getText());

    WebServicePlatformUtils.initWSPlatforms( this );

    init();
    setupWSPlatformSpecificFields();
  }

  private boolean requestedOnce;

  public void setCurrentClass(final PsiClass psiClass) {
    super.setCurrentClass(psiClass);

    if (webServiceNamespace == null) {
      if (!requestedOnce) {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          public void run() {
            initWebServiceNamespaceAndUrl(psiClass);
          }
        });
        requestedOnce = true;
      }
    } else {
      initWebServiceNamespaceAndUrl(psiClass);
    }
  }

  private void initWebServiceNamespaceAndUrl(PsiClass psiClass) {
    if (psiClass != null && webServiceNamespace.getText().length() == 0) {
      final List<String> lastContexts = WebServicesPlugin.getInstance(myProject).getLastContexts();
      final String mycontext = lastContexts.size() > 0 ? lastContexts.get(0):"";
      final String wsUrl = AxisUtil.getWebServiceUrlReference(mycontext,psiClass.getQualifiedName().replace('.', '/'));

      webServiceNamespace.setText("http://"+ DeployWebServiceDialog.buildNSNameFromClass(psiClass, currentEngine));
      webServiceURL.setText(wsUrl);
    }
  }

  protected JComponent getClassName() {
    return className;
  }

  protected JComponent createCenterPanel() {
    return myPanel;
  }

  protected void doOKAction() {
    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    instance.setLastPlatform(getWebServicePlatformCombo().getSelectedItem().toString());
    super.doOKAction();
  }

  protected ValidationResult doValidate(ValidationData _data) {
    final PsiClass clazz = getCurrentClass();
    final ValidationResult validationResult;

    if (clazz != null && clazz.isInterface()) {
      String uptoDateCheck = DeployUtils.checkIfClassIsUpToDate(myProject, clazz);

      if (uptoDateCheck != null) {
        validationResult = new ValidationResult(uptoDateCheck, null, 1000);
      } else {
        validationResult = null;
      }
    } else {
      validationResult = super.doValidate(_data);
    }

    if (validationResult == null) {
      String notAcceptableClazzMessage = currentEngine.checkNotAcceptableClassForGenerateWsdl(clazz);

      if (notAcceptableClazzMessage != null) {
        return createValidationResult(notAcceptableClazzMessage, null, 1000);
      }

      MyValidationData validationData = (MyValidationData) _data;

      if (currentEngine instanceof AxisWSEngine && validationData.selectedMethods.length == 0) {
        return new ValidationResult("No methods selected", null);
      }

      try {
        final URL url = new URL(validationData.webServiceUrl);
      } catch(MalformedURLException ex) {
        return new ValidationResult(WSBundle.message("invalid.web.service.url.validation.message"), webServiceURL);
      }

      if (validationData.webServiceNS.length() == 0) {
        return new ValidationResult(WSBundle.message("invalid.web.service.namespace.validation.message"), webServiceNamespace);
      }

      ValidationResult result = WebServicePlatformUtils.checkIfPlatformIsSetUpCorrectly(this, currentEngine);
      if (result != null) {
        return result;
      }
    }

    return validationResult;
  }

  protected JLabel getStatusTextField() {
    return statusText;
  }

  protected JLabel getStatusField() {
    return status;
  }

  @NotNull
  protected String getHelpId() {
    return "GenerateWsdlFromJava.html";
  }

  public String getTypeMappingVersion() {
    return typeMappingVersion.getSelectedItem().toString();
  }

  public String getSoapAction() {
    return soapAction.getSelectedItem().toString();
  }

  public String getBindingStyle() {
    return bindingStyle.getSelectedItem().toString();
  }

  public String getUseItemsInBindings() {
    return useItemsInBindings.getSelectedItem().toString();
  }

  public String getGenerationType() {
    return generationType.getSelectedItem().toString();
  }

  protected JTable getMethodsTable() {
    return methodsTable;
  }

  public JComboBox getWebServicePlatformCombo() {
    return webServicePlatform;
  }

  public JLabel getWebServicePlaformText() {
    return webServicePlatformText;
  }

  public void setupWSPlatformSpecificFields() {
    final String currentPlatform = (String)webServicePlatform.getSelectedItem();
    currentEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(currentPlatform);
    final boolean isAxis2 = currentPlatform.equals(Axis2WSEngine.AXIS2_PLATFORM);
    boolean isAxis = currentPlatform.equals(AxisWSEngine.AXIS_PLATFORM);
    boolean jaxRPC = currentPlatform.equals(JaxRPCWSEngine.JAX_RPC);
    boolean jaxws = currentPlatform.equals(JWSDPWSEngine.JWSDP_PLATFORM);
    boolean websphere = currentPlatform.equals(WebSphereWSEngine.WEBSPHERE_PLATFORM);

    soapAction.setVisible(isAxis || websphere);
    soapActionText.setVisible(isAxis || websphere);
    useItemsInBindings.setVisible(isAxis || isAxis2 || websphere);
    useItemsInBindingsText.setVisible(isAxis || isAxis2 || websphere);

    bindingStyle.setVisible(isAxis || isAxis2 || jaxRPC || websphere);
    bindingStyleText.setVisible(isAxis || isAxis2 || jaxRPC || websphere);

    typeMappingVersion.setVisible(isAxis);
    typeMappingVersionText.setVisible(isAxis);
    methodTablePane.setVisible(isAxis || websphere);

    webServiceNamespace.setVisible(!jaxws);
    webServiceNamespaceText.setVisible(!jaxws);

    pack();
  }

  public Module getSelectedModule() {
    return LibUtils.getModuleFromClass(getCurrentClass());
  }

  public JComboBox getModuleChooser() {
    return null;
  }

  public WSEngine getCurrentWsEngine() {
    return currentEngine;
  }

  public JTextField getWebServiceURL() {
    return webServiceURL;
  }

  public JTextField getWebServiceNamespace() {
    return webServiceNamespace;
  }

  class MyValidationData extends GenerateFromJavaCodeDialogBase.MyValidationData {
    String webServiceNS;
    String webServiceUrl;

    protected void doAcquire() {
      super.doAcquire();

      webServiceNS = webServiceNamespace.getText();
      webServiceUrl = webServiceURL.getText();
    }
  }

  protected GenerateFromJavaCodeDialogBase.MyValidationData createValidationData() {
    return new MyValidationData();
  }
}
