package com.advancedtools.webservices.actions.generate;

import com.advancedtools.webservices.utils.BaseWSGenerateAction;
import com.advancedtools.webservices.WebServicesPlugin;

/**
 * @by Maxim
 */
public class GenerateJaxRPCWSCall extends BaseWSGenerateAction {
  protected String getTemplateActionName() {
    return WebServicesPlugin.INVOKE_JAXRPC_WS_TEMPLATE_NAME;
  }
}
