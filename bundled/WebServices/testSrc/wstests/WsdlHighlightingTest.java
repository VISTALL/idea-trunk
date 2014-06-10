package wstests;

import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiClass;
import com.advancedtools.webservices.utils.XmlRecursiveElementVisitor;
import com.advancedtools.webservices.WebServicesPluginSettings;
import org.jetbrains.annotations.NonNls;

/**
 * @author Maxim
 */
public class WsdlHighlightingTest extends BaseWSTestCase {
  public void testHighlighting() throws Throwable {
    doHighlightingTest(getTestName() + ".wsdl");
  }

  public void testJaxWsHighlighting() throws Throwable {
    doHighlightingTest("JaxWsHelloWorld.wsdl","JaxWsHelloWorldPortType.wsdl");
  }

  public void testJaxWsHighlighting2() throws Throwable {
    doHighlightingTest("JaxWsHelloWorldPortType.wsdl", "JaxWsHelloWorldPortType_schema1.xsd");
  }

  public void testClassName() throws Throwable {
    doHighlightingTest("WebService.wsdl", "MyWebService.java");
    
    myFixture.getFile().accept(new XmlRecursiveElementVisitor() {
      protected String tagName;
      protected String namespace;
      protected XmlAttribute attr;
      protected String attrName;

      @Override public void visitXmlAttribute(XmlAttribute xmlAttribute) {
        attrName = xmlAttribute.getName();
        attr = xmlAttribute;
        super.visitXmlAttribute(xmlAttribute);
        attrName = null;
      }

      @Override public void visitXmlTag(XmlTag xmlTag) {
        tagName = xmlTag.getLocalName();
        namespace = xmlTag.getNamespace();
        super.visitXmlTag(xmlTag);
        tagName = null;
        namespace = null;
      }

      @Override public void visitXmlAttributeValue(XmlAttributeValue xmlAttributeValue) {
        if ("type".equals(attrName) && "binding".equals(tagName) &&
          ( WebServicesPluginSettings.HTTP_SCHEMAS_XMLSOAP_ORG_WSDL.equals(namespace) ||
            WebServicesPluginSettings.HTTP_WWW_W3_ORG_2003_03_WSDL.equals(namespace)
          )) {
          final PsiReference[] references = xmlAttributeValue.getReferences();
          assertTrue( references[0].resolve() instanceof XmlAttributeValue);
          assertTrue( references[1].resolve() instanceof XmlTag);
          assertTrue( references[2].resolve() instanceof PsiClass);
        }
      }
    });
  }

  public void testClassName2() throws Throwable {
    doHighlightingTest("WebServiceBadReferences.wsdl");
  }

  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
  }

  protected void configureInspections() {
  }

  protected @NonNls String getTestDataPath() {
    return "highlighting/wsdl";
  }
}
