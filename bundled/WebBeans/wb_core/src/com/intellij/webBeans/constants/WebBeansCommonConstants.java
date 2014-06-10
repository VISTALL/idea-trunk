package com.intellij.webBeans.constants;

import org.jetbrains.annotations.NonNls;

public interface WebBeansCommonConstants {
  @NonNls String JBOSS_FACET_DETECTION_CLASS = "org.jboss.webbeans.ManagerImpl";
  @NonNls String APACHE_FACET_DETECTION_CLASS = "org.apache.webbeans.component.Component";
  @NonNls String EVENT_CLASS_NAME = "javax.event.Event";

  @NonNls String WEB_BEANS_CONFIG_FILENAME = "web-beans.xml";
  @NonNls String WEB_BEANS_CONFIG_ROOT_TAG_NAME = "web-beans";
  
  @NonNls String STEREOTYPE_REQUARED_TYPES_PARAM = "requiredTypes";
  @NonNls String STEREOTYPE_SUPPORTED_SCOPES_PARAM = "supportedScopes";
}
