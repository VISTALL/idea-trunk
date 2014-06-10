/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package org.jbez.modelgen;

import java.io.File;

public interface Emitter {
  public static final String JDOC_OPEN = "/**";
  public static final String JDOC_CONT = " * ";
  public static final String JDOC_CLOSE = " */";


  void emit(FileManager fileManager, ModelDesc model, File outputRoot);
}
