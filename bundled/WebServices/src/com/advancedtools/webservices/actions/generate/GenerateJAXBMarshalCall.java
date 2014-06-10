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
public class GenerateJAXBMarshalCall extends BaseWSGenerateAction {
  protected String getTemplateActionName() {
    return WebServicesPlugin.JAXB_MARSHAL_TEMPLATE_NAME;
  }
}
