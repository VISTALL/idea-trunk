package org.jetbrains.plugins.groovy.mvc.plugins;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Dmitry.Krasilschikov
 * Date: 04.09.2008
 */
public class CachedMvcPlugins {
  public Map<String, MvcPluginDescriptor> availablePluginsMap = new HashMap<String, MvcPluginDescriptor>();
  public Map<String, MvcPluginDescriptor> fullPluginsInfo = new HashMap<String, MvcPluginDescriptor>();

  public CachedMvcPlugins() {
  }
}