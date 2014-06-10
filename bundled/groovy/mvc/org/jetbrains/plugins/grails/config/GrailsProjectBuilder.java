package org.jetbrains.plugins.grails.config;

import org.jetbrains.plugins.groovy.mvc.MvcCreateFromSourcesMode;
import org.jetbrains.plugins.groovy.mvc.MvcModuleBuilder;

/**
 * @author peter
 */
public class GrailsProjectBuilder extends MvcCreateFromSourcesMode {
  public GrailsProjectBuilder() {
    super(GrailsFramework.INSTANCE);
  }

  @Override
  protected MvcModuleBuilder createModuleBuilder() {
    return new GrailsModuleBuilder();
  }
}
