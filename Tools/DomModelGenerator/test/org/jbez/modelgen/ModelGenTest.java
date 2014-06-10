package org.jbez.modelgen;

import org.junit.Test;
import org.junit.Assert;

import java.io.File;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.nio.CharBuffer;

/**
 * @author Gregory.Shrago
 */
public class ModelGenTest {

  public static final String ROOT = "test/data";
  public static final String CONFIG = "test/data/config.xml";

  @Test
  public void testJavaEESchemas() throws Exception{
    final File root = new File(ROOT, "javaee");    
    ModelGen gen = new ModelGen(new XSDModelLoader());
    gen.loadConfig(new File(CONFIG));
    gen.loadModel(root);
    Assert.assertEquals(loadFile(new File(root, "model.dump")), getModelDump(gen.getModel()));
  }

  @Test
  public void testSpringDTD() throws Exception{
    final File root = new File(ROOT, "spring-dtd");
    ModelGen gen = new ModelGen(new DTDModelLoader());
    gen.loadConfig(new File(CONFIG));
    gen.loadModel(root);
    Assert.assertEquals(loadFile(new File(root, "model.dump")), getModelDump(gen.getModel()));
  }

  @Test
  public void testSpringXSD() throws Exception{
    final File root = new File(ROOT, "spring-xsd");
    ModelGen gen = new ModelGen(new XSDModelLoader());
    gen.loadConfig(new File(CONFIG));
    gen.loadModel(root);
    Assert.assertEquals(loadFile(new File(root, "model.dump")), getModelDump(gen.getModel()));
  }


  private String getModelDump(ModelDesc model) {
    final StringWriter stringWriter = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(stringWriter);
    model.dump(printWriter);
    return stringWriter.toString();
  }

  private String loadFile(File file) throws Exception {
    FileReader fileReader = null;
    try {
      fileReader = new FileReader(file);
      final long length = file.length();
      final CharBuffer charBuffer = CharBuffer.allocate((int) length);
      final int read = fileReader.read(charBuffer);
      return new String(charBuffer.array());
    } finally {
      if (fileReader != null) {
        fileReader.close();
      }
    }
  }
}
