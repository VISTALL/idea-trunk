package org.jetbrains.plugins.grails.config;

import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.groovy.mvc.MvcModuleBuilder;

/**
 * @author peter
 */
public class GrailsModuleBuilder extends MvcModuleBuilder {
  public GrailsModuleBuilder() {
    super(GrailsFramework.INSTANCE, GrailsIcons.GRAILS_MODULE_ICON, GrailsLibraryManager.class);
  }
}
