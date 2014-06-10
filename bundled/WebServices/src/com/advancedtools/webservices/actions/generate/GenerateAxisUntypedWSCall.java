package com.advancedtools.webservices.actions.generate;

import com.advancedtools.webservices.utils.BaseWSGenerateAction;
import com.advancedtools.webservices.WebServicesPlugin;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim
 * Date: Oct 9, 2006
 * Time: 2:08:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenerateAxisUntypedWSCall extends BaseWSGenerateAction {
  protected String getTemplateActionName() {
    return WebServicesPlugin.INVOKE_AXIS_UWS_TEMPLATE_NAME2;
  }
}
