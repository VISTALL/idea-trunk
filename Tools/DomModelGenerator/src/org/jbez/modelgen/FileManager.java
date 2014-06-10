/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package org.jbez.modelgen;

import java.io.File;

public interface FileManager {
  public File releaseOutputFile(File outFile);
  public File getOutputFile(File target);
}
