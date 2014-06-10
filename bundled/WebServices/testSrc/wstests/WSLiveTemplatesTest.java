package wstests;

import com.advancedtools.webservices.actions.generate.*;
import com.advancedtools.webservices.utils.BaseWSGenerateAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Maxim
 */
public class WSLiveTemplatesTest extends BaseWSTestCase {
  protected final @NonNls Set<String> myTestsWithJavaee = new THashSet<String>();
  protected final @NonNls Set<String> myTestsWithAxis = new THashSet<String>();
  {
    myTestsWithJavaee.addAll(Arrays.asList("JWSDPServer2"));
    myTestsWithAxis.addAll(Arrays.asList("AxisServer2"));
  }
  
  public void testJWSDPServer() throws Throwable {
    doTest(GenerateJWSDPWSCall.class);
  }
  
  public void testJWSDPServer2() throws Throwable {
    doTest(GenerateJWSDPWSCall.class, getTestName() + ".java", "JWSDPServer2Ws.java");
  }

  public void testAxisServer() throws Throwable {
    doTest(GenerateAxisWSCall.class);
  }
  
  public void testAxisUntypedServer() throws Throwable {
    doTest(GenerateAxisUntypedWSCall.class);
  }
  
  public void testAxisServer2() throws Throwable {
    doTest(GenerateAxisWSCall.class, getTestName() + ".java", "AxisServer2Ws.java", "JavaRmi.java");
  }
  
  public void testJaxbMarshal() throws Throwable {
    doTest(GenerateJAXBMarshalCall.class);
  }
  
  public void testJaxbUnmarshal() throws Throwable {
    doTest(GenerateJAXBUnmarshalCall.class);
  }
  
  public void testXmlBeansMarshal() throws Throwable {
    doTest(GenerateXmlBeansMarshalCall.class);
  }
  
  public void testXmlBeansUnmarshal() throws Throwable {
    doTest(GenerateXmlBeansUnmarshalCall.class);
  }
  
  public void testJAXRPCServer() throws Throwable {
    doTest(GenerateJaxRPCWSCall.class);
  }
  
  public void testAxis2Server() throws Throwable {
    doTest(GenerateAxis2WSCall.class);
  }
  
  public void testXFireServer() throws Throwable {
    doTest(GenerateXFireWSCall.class);
  }
  
  public void testXFireUntypedServer() throws Throwable {
    doTest(GenerateXFireUntypedWSCall.class);
  }
  
  public void testWebSphereServer() throws Throwable {
    doTest(GenerateWebSphereWSCall.class);
  }
  
  private void doTest(final Class<? extends BaseWSGenerateAction> clazz, String ... names) throws Throwable {
    if (names.length == 0) names = new String[] { getTestName() + ".java" };
    doHighlightingTest(names);

    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      public void run() {
        CommandProcessor.getInstance().executeCommand(
          myFixture.getProject(), 
          new Runnable() {
            public void run() {
              ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                  try {
                    clazz.newInstance().run(myFixture.getEditor(), myFixture.getProject());
                  } catch (Exception e) {
                    throw new RuntimeException(e);
                  }
                }
              });
            }
          },
          "test",
          null
        );
      }
    }, ModalityState.defaultModalityState());

    myFixture.checkResultByFile(getTestName() + "_after.java");
  }

  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
    if (myTestsWithJavaee.contains(getTestName())) {
      moduleFixtureBuilder.addLibrary("javaee", (getLibPath()+"/javaee.jar").replace(File.separator, "/"));
    }

    if (myTestsWithAxis.contains(getTestName())) {
      moduleFixtureBuilder.addLibrary("axis", (getPluginBasePath() + "build/libs/axis.jar").replace(File.separator, "/"));
    }
  }

  protected void configureInspections() {
  }

  protected @NonNls String getTestDataPath() {
    return "liveTemplates";
  }
}
