/*
 * XSD/DTD Model generator tool
 *
 * By Gregory Shrago
 * 2002 - 2006
 */
package org.jbez.modelgen;

import java.io.File;

/**
 * @author Gregory.Shrago
 */
public class Main {


  public static void main(String argv[]) throws Exception {
    if (argv.length != 4) {
      System.out.println("Usage: Main <XSD or DTD> <input folder> <output folder> <config xml>");
    }
    String mode = argv[0];
    final ModelLoader loader;
    if (mode.equalsIgnoreCase("xsd")) {
      loader = new XSDModelLoader();
    }
    else if (mode.equalsIgnoreCase("dtd")) {
      loader = new DTDModelLoader();
    }
    else {
      System.out.println("'"+mode+"' format not supported");
      System.exit(-1);
      return;
    }
    final File modelRoot = new File(argv[1]);
    final File outputRoot = new File(argv[2]);
    final File configXml = new File(argv[3]);

    outputRoot.mkdirs();
    final ModelGen modelGen = new ModelGen(loader);
    modelGen.loadConfig(configXml);
    modelGen.perform(outputRoot, modelRoot);
  }

}
