package com.advancedtools.webservices.actions.generate;

import com.advancedtools.webservices.utils.BaseWSGenerateAction;
import com.advancedtools.webservices.WebServicesPlugin;

/**
 * @author Maxim
 */
public class GenerateWebSphereWSCall extends BaseWSGenerateAction {
  protected String getTemplateActionName() {
    return WebServicesPlugin.INVOKE_WEBSPHERE_WS_TEMPLATE_NAME;
  }
}
