package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisWSEngine;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.ui.GenerateDialogBase;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.advancedtools.webservices.utils.ui.ValidationUtils;
import com.advancedtools.webservices.wsengine.DialogWithWebServicePlatform;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author maxim
 * Date: 24.01.2006
 */
class EnableWebServicesSupportDialog extends MyDialogWrapper implements DialogWithWebServicePlatform,
  EnableWebServicesSupportUtils.EnableWebServicesSupportModel {
  private final Project project;
  private JPanel panel;
  private ComboBox modules;
  private ComboBox clientServerSwitch;
  private JLabel chooseModuleText;
  private JLabel status;
  private JLabel statusText;
  private JLabel clientServerSwitchText;
  private static final String SERVER_TYPE = WSBundle.message("server.webservices.support.type");

  private ComboBox webServicePlatform;
  private JLabel webServicePlatformText;
  private JCheckBox addSimpleCodeSwitch;
  private JTextField packageName;
  private JTextField className;
  private JLabel packageNameText;
  private JLabel classNameText;
  private WSEngine currentEngine;

  public EnableWebServicesSupportDialog(Project _project, EnableWebServicesSupportDialog previousDialog) {
    super(_project);

    project = _project;

    doInitFor(chooseModuleText,modules,'m');
    doInitFor(clientServerSwitchText,clientServerSwitch,'t');

    addSimpleCodeSwitch.setMnemonic('a');

    configureComboBox(clientServerSwitch, Arrays.asList(SERVER_TYPE, WSBundle.message("client.webservices.support.type")));

    clientServerSwitch.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        initModules();
        className.setText(
          SERVER_TYPE.equals(clientServerSwitch.getSelectedItem())?
            EnableWebServicesSupportUtils.SIMPLE_WS_NAME :
            EnableWebServicesSupportUtils.SIMPLE_WS_CLIENT_NAME
        );
      }
    });

    doInitFor(packageNameText,packageName,'p');
    doInitFor(classNameText,className,'c');

    addSimpleCodeSwitch.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        boolean state = false;
        if (addSimpleCodeSwitch.isSelected()) {
          state = true;
        }

        updateClassNameAndPackageVisibility(state);
        pack();
      }
    });

    updateClassNameAndPackageVisibility(false);
    packageName.setText(EnableWebServicesSupportUtils.SIMPLE_WS_PACKAGE);
    className.setText(EnableWebServicesSupportUtils.SIMPLE_WS_NAME);

    setTitle(WSBundle.message("enable.web.services.support.dialog.title"));

    WebServicePlatformUtils.initWSPlatforms( this );

    initModules();
    init();
    setupWSPlatformSpecificFields();

    if (previousDialog != null) {
      clientServerSwitch.setSelectedItem(previousDialog.clientServerSwitch.getSelectedItem());
      packageName.setText(previousDialog.packageName.getText());
      className.setText(previousDialog.className.getText());
      modules.setSelectedItem(previousDialog.modules.getSelectedItem());
    }
  }

  private void updateClassNameAndPackageVisibility(boolean state) {
    packageName.setVisible(state);
    packageNameText.setVisible(state);
    classNameText.setVisible(state);
    className.setVisible(state);
  }

  public JComboBox getWebServicePlatformCombo() {
    return webServicePlatform;
  }

  public JLabel getWebServicePlaformText() {
    return webServicePlatformText;
  }

  public String getClassNameToCreate() {
    return className.getText();
  }

  public String getPackageNameToCreate() {
    return packageName.getText();
  }

  public void setupWSPlatformSpecificFields() {
    final String currentPlatform = (String)webServicePlatform.getSelectedItem();
    currentEngine = WebServicesPluginSettings.getInstance().getEngineManager().getWSEngineByName(currentPlatform);
    boolean isAxis = currentPlatform.equals(AxisWSEngine.AXIS_PLATFORM);
  }

  private void initModules() {
    ModuleManager moduleManager = ModuleManager.getInstance(project);
    Module[] modules = moduleManager.getModules();
    List<Module> modulesList = new LinkedList<Module>();

    DataContext dataContext = DataManager.getInstance().getDataContext();
    Module selectedModule = (Module) (dataContext != null ? dataContext.getData(DataConstants.MODULE) : null);

    if (isServerSideSupport()) {
      for (Module module : modules) {
        if (EnvironmentFacade.getInstance().isWebModule(module)) {
          modulesList.add(module);
        }
      }

      if (selectedModule != null && !EnvironmentFacade.getInstance().isWebModule(selectedModule)) {
        selectedModule = null;
      }
    } else {
      modulesList.addAll(Arrays.asList(modules));
    }

    this.modules.setModel(new DefaultComboBoxModel(modulesList.toArray()));
    if (selectedModule != null) this.modules.setSelectedItem(selectedModule);
  }

  public Module getModule() {
    return getSelectedModule();
  }

  public WSEngine getWsEngine() {
    return currentEngine;
  }

  public boolean isServerSideSupport() {
    return SERVER_TYPE.equals(clientServerSwitch.getSelectedItem());
  }

  @Nullable
  public String getBindingType() {
    return null;
  }

  public boolean isToGenerateSampleCode() {
    return addSimpleCodeSwitch.isSelected();
  }

  class ValidationData extends MyDialogWrapper.ValidationData {
    Module module;
    String classnameToCreate;
    String packagenameToCreate;

    protected void doAcquire() {
      module = (Module) modules.getSelectedItem();
      classnameToCreate = getClassNameToCreate();
      packagenameToCreate = getClassNameToCreate();
    }
  }

  protected ValidationResult doValidate(MyDialogWrapper.ValidationData _data) {
    ValidationResult result = WebServicePlatformUtils.checkIfPlatformIsSetUpCorrectly(this, currentEngine);
    if (result != null) {
      return result;
    }

    ValidationData data = new ValidationData();
    data.acquire();

    if (data.module == null) {
      return new ValidationResult(WSBundle.message("invalid.web.module.selected.validation.message"), modules); 
    }

    if (!ValidationUtils.validatePackageName(data.packagenameToCreate)) {
      return new ValidationResult(GenerateDialogBase.validatePackagePrefix(data.packagenameToCreate), packageName);
    }

    if (!ValidationUtils.validateClassName(data.classnameToCreate)) {
      return new ValidationResult(WSBundle.message("classname.is.not.valid.validation.message"), className);
    }

    return null;
  }

  protected MyDialogWrapper.ValidationData createValidationData() {
    return null;
  }

  protected JLabel getStatusTextField() {
    return statusText;
  }

  protected JLabel getStatusField() {
    return status;
  }

  @NotNull
  protected String getHelpId() {
    return "EnableWebServicesSupport.html";
  }

  protected JComponent createCenterPanel() {
    return panel;
  }

  public Module getSelectedModule() {
    return (Module) modules.getSelectedItem();
  }

  public JComboBox getModuleChooser() {
    return modules;
  }

  protected void doOKAction() {
    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    instance.setLastPlatform(getWebServicePlatformCombo().getSelectedItem().toString());
    super.doOKAction();
  }
}
