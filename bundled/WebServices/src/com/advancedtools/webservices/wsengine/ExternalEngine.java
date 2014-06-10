package com.advancedtools.webservices.wsengine;

import com.intellij.openapi.module.Module;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 31.07.2006
 * Time: 0:28:14
 * To change this template use File | Settings | File Templates.
 */
public interface ExternalEngine {
  interface LibraryDescriptorContext {
    boolean isForRunningGeneratedCode(); // false -> to run engine
    String getBindingType();
    Module getTargetModule();
  }

  interface LibraryDescriptor {
    String getName();
    String[] getLibJars();
    boolean isToIncludeInJavaEEContainerDeployment();

    LibraryDescriptor[] EMPTY_ARRAY = new LibraryDescriptor[0];
  }

  String getName();
  LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context);
  String getBasePath();
}
