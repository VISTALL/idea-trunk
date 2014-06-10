package wstests;

import com.advancedtools.webservices.xmlbeans.Xsd2InstanceUtils;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

/**
 * @by maxim
 */
public class GenerateInstanceFromSchemaTest extends BaseWSTestCase {
  @NonNls
  private static final String XSD_EXTENSION = ".xsd";

  public void testProcessFile() throws Throwable {
    doHighlightingForIntentions(getTestName() + XSD_EXTENSION);
    LinkedHashMap<String,String> references = new LinkedHashMap<String, String>();
    final File baseOutFile = new File(getPluginBasePath() + "testData/" + getTestDataPath() + "/" + getTestName());

    String[] expectedChildrenNames = baseOutFile.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(XSD_EXTENSION);
      }
    });
    final Set<String> expectedChildrenNamesSet = new THashSet<String>(expectedChildrenNames != null ? Arrays.asList(expectedChildrenNames):Collections.<String>emptyList());
    
    Xsd2InstanceUtils.processAndSaveAllSchemas(
      (XmlFile) myFixture.getFile(),
      references,
      new Xsd2InstanceUtils.SchemaReferenceProcessor() {
        public void processSchema(String schemaFileName, String schemaContent) {
          final File file = new File(baseOutFile, schemaFileName);
          assertTrue(file.exists());
          try {
            assertTrue(expectedChildrenNamesSet.remove(schemaFileName));
            String expectedText = StringUtil.convertLineSeparators(new String(VfsUtil.loadText(LocalFileSystem.getInstance().findFileByIoFile(file))));
            if (!"xml.xsd".equals(schemaFileName) || EnvironmentFacade.isSelenaOrBetter()) assertEquals(expectedText, schemaContent);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    );
    
    assertEquals(0, expectedChildrenNamesSet.size());
  }
  
  public void testRedefiningSchema() throws Throwable {
    doHighlightingTest(getTestName()+ XSD_EXTENSION, "RedefinedSchema.xsd", "ImportedSchema.xsd", "IncludedSchema.xsd");
    final XmlTag tag = ((XmlFile) myFixture.getFile()).getDocument().getRootTag();
    List<String> elementNameList = Xsd2InstanceUtils.addVariantsFromRootTag(tag);
    Collections.sort(elementNameList);
    
    final String[] elementNames = elementNameList.toArray(new String[elementNameList.size()]);
    assertEquals(1, elementNames.length);

    assertEquals("outerIncluded",elementNames[0]);

    assertNotNull(Xsd2InstanceUtils.getDescriptor(tag, "outerIncluded"));
    if (EnvironmentFacade.isSelenaOrBetter()) {
      assertNotNull(Xsd2InstanceUtils.getDescriptor(tag, "inner"));
  //    assertNotNull(Xsd2InstanceUtils.getDescriptor(tag, "innerImported"));
      assertNotNull(Xsd2InstanceUtils.getDescriptor(tag, "outer"));
    }
  }

  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
  }

  protected void configureInspections() {
  }

  protected @NonNls String getTestDataPath() {
    return "xsd2instance";
  }
}