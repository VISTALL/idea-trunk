package org.jetbrains.plugins.groovy.griffon;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.plugins.groovy.mvc.MvcModuleBuilder;

import javax.swing.*;

/**
 * @author peter
 */
public class GriffonModuleBuilder extends MvcModuleBuilder {
  private static final Icon GRIFFON_ICON_24x24 = IconLoader.findIcon("/images/griffon/griffon-icon-24x24.png");

  public GriffonModuleBuilder() {
    super(GriffonFramework.INSTANCE, GRIFFON_ICON_24x24, GriffonLibraryManager.class);
  }

}
