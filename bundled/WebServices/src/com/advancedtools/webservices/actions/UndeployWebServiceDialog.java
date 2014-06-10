package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.utils.ui.MyDialogWrapper;
import com.advancedtools.webservices.wsengine.DeploymentDialog;
import com.advancedtools.webservices.wsengine.DialogWithWebServicePlatform;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Maxim.Mossienko
 * Date: Jul 31, 2006
 */
public class UndeployWebServiceDialog extends MyDialogWrapper implements DialogWithWebServicePlatform, DeploymentDialog {
  private ComboBox webServicePlatform;
  private JLabel webServicePlatformText;
  private JLabel status;
  private JLabel statusText;
  private JPanel myPanel;
  private WSEngine currentEngine;

  private JLabel myWSNameText;

  private ComboBox myTargetModule;
  private JLabel myTargetModuleText;
  private ComboBox myWSName;

  public UndeployWebServiceDialog(final Project project, @Nullable UndeployWebServiceDialog previousDialog) {
    super(project);

    setTitle(WSBundle.message("remove.web.service.dialog.title"));
    WebServicePlatformUtils.initWSPlatforms( this );

    doInitFor(myWSNameText, myWSName, 'W');

    ModuleManager moduleManager = ModuleManager.getInstance(project);
    Module[] modules = moduleManager.getModules();
    List<Module> modulesList = new LinkedList<Module>();

    for(Module module:modules) {
      if (EnvironmentFacade.getInstance().isWebModule(module)) {
        modulesList.add(module);
      }
    }

    myTargetModule.setModel(new DefaultComboBoxModel(modulesList.toArray(ArrayUtil.EMPTY_OBJECT_ARRAY)));

    if (modulesList.size() > 0) myTargetModule.setSelectedIndex(0);

    doInitFor(myTargetModuleText, myTargetModule, 'M');
    myTargetModule.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent itemEvent) {
        if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
          setupAvailableServices();
        }
      }
    });

    if (previousDialog != null) {
      myTargetModule.setSelectedItem(previousDialog.myTargetModule.getSelectedItem());
      myWSName.setSelectedItem(previousDialog.myWSName.getSelectedItem());
    }

    setupWSPlatformSpecificFields();
    init();
  }

  class ValidationData extends MyDialogWrapper.ValidationData {
    Module module;
    String wsName;

    protected void doAcquire() {
      module = (Module) myTargetModule.getSelectedItem();
      wsName = (String) myWSName.getSelectedItem();
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
      return new ValidationResult(WSBundle.message("invalid.web.module.selected.validation.message"), myTargetModule);
    }

    if (data.wsName == null) {
      return new ValidationResult(WSBundle.message("invalid.web.service.selected.validation.message"), myWSName);
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

  protected JComponent createCenterPanel() {
    return myPanel;
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

    setupAvailableServices();
  }

  public Module getSelectedModule() {
    return (Module) myTargetModule.getSelectedItem();
  }

  public JComboBox getModuleChooser() {
    return myTargetModule;
  }

  private void setupAvailableServices() {
    final Module module = (Module) myTargetModule.getSelectedItem();
    myWSName.setModel(
      new DefaultComboBoxModel(
        module != null ? currentEngine.getAvailableWebServices(module):ArrayUtil.EMPTY_OBJECT_ARRAY
      )
    );
  }

  public WSEngine getCurrentEngine() {
    return currentEngine;
  }

  protected void doOKAction() {
    final WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();
    instance.setLastPlatform(getWebServicePlatformCombo().getSelectedItem().toString());
    super.doOKAction();
  }

  protected @NonNls @NotNull String getHelpId() {
    return "UndeployWebServices.html";
  }

  public String getWSName() {
    return myWSName.getSelectedItem().toString();
  }

  public Module getWsModule() {
    return (Module) myTargetModule.getSelectedItem();
  }
}
