package com.advancedtools.webservices.actions.generate;

import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.utils.BaseWSGenerateAction;

/**
 * @by Konstantin Bulenkov
 */
public class GenerateRestWSCall extends BaseWSGenerateAction {
  protected String getTemplateActionName() {
    return WebServicesPlugin.INVOKE_REST_WS_TEMPLATE_NAME;
  }
}
