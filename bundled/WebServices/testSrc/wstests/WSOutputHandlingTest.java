package wstests;

import com.advancedtools.webservices.utils.InvokeExternalCodeUtil;
import com.advancedtools.webservices.xfire.XFireWSEngine;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.intellij.openapi.util.io.FileUtil;
import junit.framework.TestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @by maxim
 * @by Konstantin Bulenkov
 */
public class WSOutputHandlingTest extends TestCase {
  public void testCxf() throws Throwable {
    String out = loadFile("Cxf.out");
    String err = loadFile("Cxf.err");
    InvokeExternalCodeUtil.OutputConsumer consumer = XFireWSEngine.createCxfOutputFilter();
    assertFalse("Cxf should reject this output", consumer.handle("",err));

    assertOutputRejectedWithException(out, err, consumer, "Cxf should reject this output");

    err = loadFile("Cxf2.err");
    assertOutputRejectedWithException(out, err, consumer, "Cxf should reject this output");
  }

  public void testJaxWS() throws Throwable {
    String out = loadFile("JaxWS_WSDL2Java_1.6.out");
    String err = null;
    InvokeExternalCodeUtil.OutputConsumer consumer = JWSDPWSEngine.ERROR_CHECKER;
    assertOutputRejectedWithException(out, err, consumer, "JaxWS should reject this output");
  }

  private static void assertOutputRejectedWithException(String out, String err, InvokeExternalCodeUtil.OutputConsumer consumer, String s) {
    try {
      consumer.handle(out,err);
      assertTrue(s, false);
    } catch (InvokeExternalCodeUtil.ExternalCodeException ex) {//
    }
  }

  private @NonNls @NotNull String loadFile(@NonNls @NotNull String name) throws IOException {
    final String filename = BaseWSTestCase.getPluginBasePath() + "testData/output_handling/" + name;
    final File file = new File(filename);
    assertTrue("File '" + filename + "' is missed", file.exists());
    return new String(FileUtil.loadFileText(file));    
  }
}