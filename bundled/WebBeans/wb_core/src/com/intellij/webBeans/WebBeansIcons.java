package com.intellij.webBeans;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public interface WebBeansIcons {
  Icon WEB_BEANS_ICON = IconLoader.getIcon("/resources/icons/webBeans.png");
  Icon WEB_BEAN = IconLoader.getIcon("/resources/icons/webBean.png");

  Icon SIMPLE_BEAN = IconLoader.getIcon("/resources/icons/simpleWebBean.png");

  Icon GOTO_OBSERVES = IconLoader.getIcon("/resources/icons/gotoObserves.png");
  Icon GOTO_FIRES = IconLoader.getIcon("/resources/icons/gotoFires.png");

  Icon BINDING_TYPES = IconLoader.getIcon("/resources/icons/binding.png");
  Icon SCOPE_TYPES = IconLoader.getIcon("/resources/icons/scopes.png");
  Icon DEPLOYMENT_TYPES = IconLoader.getIcon("/resources/icons/deployments.png");
  Icon INTERCEPTOR_TYPES = IconLoader.getIcon("/resources/icons/interceptors.png");

}
