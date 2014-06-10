package com.advancedtools.webservices.utils.ui;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisUtil;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.text.MessageFormat;

/**
 * @author maxim
 * Date: 22.12.2005
 */
public abstract class DialogWithServerContext extends MyDialogWrapper {
  protected DialogWithServerContext(Project project) {
    super(project);
  }

  protected ValidationResult doValidate(ValidationData _data) {
    initiateValidation(2000); // Server may go down

    if (!AxisUtil.simpleCheckThatServerOnLocalHostIsRunning()) {
      WebServicesPluginSettings instance = WebServicesPluginSettings.getInstance();

      return new ValidationResult(
        MessageFormat.format(
          WSBundle.message("the.web.server.0.1.is.not.running.launch.the.webservices.web.module.in.local.tomcat.run.debug.session.or.change.server.port.in.plugin.settings.validation.message"), instance.getHostName(), instance.getHostPort()),
        null
      );
    }

    DialogWithServerContext.MyValidationData validationData = (MyValidationData) _data;
 
    return doValidateWithData(validationData);
  }

  protected ValidationResult doValidateWithData(MyValidationData validationData) {
    final String contextName = validationData.contextName;

    for(int i = 0; i < contextName.length(); ++i) {
      final char ch = contextName.charAt(i);
      if (!Character.isLetterOrDigit(ch) && ch != '_') {
        return new ValidationResult(
          WSBundle.message("context.name.should.consist.of.alphanumeric.and.letters.only.validation.message"),
          getContextNamesCombo()
        );
      }
    }

    return doCheckServlet(contextName);
  }

  protected ValidationResult doCheckServlet(String contextName) {
    if (!AxisUtil.simpleHappyServlet(contextName, "AxisServlet")) {
      return new ValidationResult(
        WSBundle.message("nonvalid.context.or.axisservlet.is.not.present.web.service.support.not.enabled.for.module.validation.message"),
        getContextNamesCombo()
      );
    }

    return null;
  }

  protected class MyValidationData extends ValidationData {
    public String contextName;

    protected void doAcquire() {
      contextName = getCurrentValueOf(getContextNamesCombo());
    }
  }

  protected MyValidationData createValidationData() {
    return new MyValidationData();
  }

  protected String getCurrentValueOf(final JComponent comp) {
    if(comp instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox) comp;

      if (comboBox.isEditable()) {
        return comboBox.getEditor().getItem().toString();
      } else {
        Object selectedItem = comboBox.getSelectedItem();
        return selectedItem != null ? selectedItem.toString():null;
      }
    } else if (comp instanceof JTextField) {
      return ((JTextField)comp).getText();
    } else {
      return "";
    }
  }

  protected abstract JComboBox getContextNamesCombo();
}
