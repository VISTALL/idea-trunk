package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.utils.ui.DialogWithServerContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author maxim
 * Date: 03.01.2006
 */
public class ShowDeployedWebSevicesDialog extends DialogWithServerContext {
  private JPanel panel;
  private ComboBox contextNames;
  private JLabel contextNameText;
  private JLabel statusText;
  private JLabel status;
  private final String helpId;
  private ComboBox port;
  private JLabel portText;
  private final boolean doMonitoring;

  public ShowDeployedWebSevicesDialog(Project project,String title,@NonNls String _helpId, List<String> possibleMonitoringPorts) {
    super(project);

    setTitle(title);
    List lastContexts = WebServicesPlugin.getInstance(project).getLastContexts();
    contextNames.setModel(new DefaultComboBoxModel(lastContexts.toArray()));

    doInitFor(contextNameText, contextNames,'c');

    helpId = _helpId;
    doMonitoring = possibleMonitoringPorts != null;

    if (!doMonitoring) {
      port.setVisible(false);
      portText.setVisible(false);
    } else {
      doInitFor(portText, port, 't');
      configureComboBox(port, possibleMonitoringPorts);
    }

    init();
  }

  protected JComboBox getContextNamesCombo() {
    return contextNames;
  }

  class MyValidationData extends DialogWithServerContext.MyValidationData {
    String portValue;

    protected void doAcquire() {
      super.doAcquire();
      portValue = getCurrentValueOf(port);
    }
  }

  protected MyValidationData createValidationData() {
    return new MyValidationData();
  }

  protected String getServletName() {
    return null;
  }

  protected ValidationResult doCheckServlet(String contextName) {
    if (doMonitoring) return super.doCheckServlet(contextName);
    return null;
  }

  protected ValidationResult doValidateWithData(DialogWithServerContext.MyValidationData validationData) {
    ValidationResult validationResult = super.doValidateWithData(validationData);
    if (validationResult != null) return validationResult;

    final String contextName = validationData.contextName;

    if (doMonitoring) {
      if (!AxisUtil.simpleHappyAxisMonitor(contextName)) {
        return new ValidationResult(WSBundle.message("uncomment.soapmonitor.mapping.in.web.xml.validation.message"), null);
      }

      String portValue = ((MyValidationData) validationData).portValue;
      if (portValue == null || portValue.length() == 0) {
        return new ValidationResult(WSBundle.message("no.web.module.with.web.service.support.configure.one.validation.message"),port);
      }
    }
    return null;
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

  @NotNull
  protected String getHelpId() {
    return helpId;
  }

  public String getContextName() {
    final Object o = contextNames.getSelectedItem();
    return o != null ? o.toString():"";
  }

  public int getPort() {
    return Integer.parseInt(port.getSelectedItem().toString());
  }
}
