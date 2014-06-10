package org.jetbrains.plugins.groovy.grails;

import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.groovy.lang.formatter.GroovyFormatterTestCase;
import org.jetbrains.plugins.groovy.util.TestUtils;

import java.util.List;

/**
 * @author ilyas
 */
public class GspFormatterTest extends GroovyFormatterTestCase {
  @Override
  protected void setSettings(Project project) {
    super.setSettings(project);
    CodeStyleSettings.IndentOptions gsp = myTempSettings.getIndentOptions(GspFileType.GSP_FILE_TYPE);
    
    gsp.INDENT_SIZE = 2;
    gsp.CONTINUATION_INDENT_SIZE = 4;
    gsp.TAB_SIZE = 2;
  }

  @Override
  protected void setSettingsBack() {
    myTempSettings.getIndentOptions(GspFileType.GSP_FILE_TYPE).INDENT_SIZE = 200;
    myTempSettings.getIndentOptions(GspFileType.GSP_FILE_TYPE).CONTINUATION_INDENT_SIZE = 200;
    myTempSettings.getIndentOptions(GspFileType.GSP_FILE_TYPE).TAB_SIZE = 200;

    super.setSettingsBack();
  }

  public void doTest() throws Throwable {
    final List<String> data = TestUtils.readInput(getTestDataPath() + getTestName(true).replace('$', '/') + ".test");
    myFixture.configureByText(GspFileType.GSP_FILE_TYPE, data.get(0));
    checkFormatting(data.get(1));
  }
  
  public void testComments$schulz1() throws Throwable { doTest(); }
  public void testComments$ven1() throws Throwable { doTest(); }
  public void testGroovy$groovy() throws Throwable { doTest(); }
  public void testGroovy$plain1() throws Throwable { doTest(); }
  public void testGroovy$sim1() throws Throwable { doTest(); }
  public void testGsp$gsp1() throws Throwable { doTest(); }
  public void testGsp$gsp2() throws Throwable { doTest(); }
  public void testGsp$gsp3() throws Throwable { doTest(); }
  public void testGsp$gsp8() throws Throwable { doTest(); }
  public void testGsp$gsp9() throws Throwable { doTest(); }
  public void testHtml$error$giga_werle() throws Throwable { doTest(); }
  public void testHtml$error$nik1() throws Throwable { doTest(); }
  public void testHtml$error$show() throws Throwable { doTest(); }
  public void testHtml$error$tesr2() throws Throwable { doTest(); }
  public void testHtml$error$werle1() throws Throwable { doTest(); }
  public void testHtml$error$werle30() throws Throwable { doTest(); }
  public void testHtml$inner$GRVY_1165_1() throws Throwable { doTest(); }
  public void testHtml$inner$GRVY_1165() throws Throwable { doTest(); }
  public void testHtml$inner$GRVY_876() throws Throwable { doTest(); }
  public void testHtml$inner$inner2() throws Throwable { doTest(); }
  public void testHtml$inner$inner3() throws Throwable { doTest(); }
  public void testHtml$megabug$GRVY_1046() throws Throwable { doTest(); }
  public void testHtml$megabug$megap2() throws Throwable { doTest(); }
  public void testHtml$megabug$mmm3() throws Throwable { doTest(); }
  public void testHtml$megabug$peter() throws Throwable { doTest(); }
  public void testHtml$megabug$peter2() throws Throwable { doTest(); }
  public void testHtml$megabug$peter3() throws Throwable { doTest(); }
  public void testHtml$megabug$range1() throws Throwable { doTest(); }
  public void testHtml$nested$attr1() throws Throwable { doTest(); }
  public void testHtml$nested$gsp6() throws Throwable { doTest(); }
  public void testHtml$nested$gsp7() throws Throwable { doTest(); }
  public void testHtml$nested$peter7926() throws Throwable { doTest(); }
  public void testHtml$nested$peter_simple() throws Throwable { doTest(); }
  public void testHtml$nested$range() throws Throwable { doTest(); }
  public void testHtml$nested$range2() throws Throwable { doTest(); }
  public void testHtml$nested$range4() throws Throwable { doTest(); }
  public void testHtml$nested$range5() throws Throwable { doTest(); }
  public void testHtml$simple$GRVY_1146() throws Throwable { doTest(); }
  public void testHtml$simple$gsp4() throws Throwable { doTest(); }
  public void testHtml$simple$gsp8() throws Throwable { doTest(); }
  public void testHtml$simple$htm1() throws Throwable { doTest(); }
  public void testHtml$simple$htm2() throws Throwable { doTest(); }
  public void testHtml$simple$range3() throws Throwable { doTest(); }
  public void testHtml$simple$sim2() throws Throwable { doTest(); }
  public void testHtml$trash$bug1() throws Throwable { doTest(); }
  public void testHtml$trash$bug2() throws Throwable { doTest(); }
  public void testHtml$trash$bug3() throws Throwable { doTest(); }
  public void testHtml$trash$bug4() throws Throwable { doTest(); }
  public void testHtml$trash$bug5() throws Throwable { doTest(); }
  public void testHtml$trash$trash1() throws Throwable { doTest(); }
  public void testSpacing$groovy$gr1() throws Throwable { doTest(); }
  public void testSpacing$groovy$gr2() throws Throwable { doTest(); }
  public void testSpacing$htm3() throws Throwable { doTest(); }
  public void testSpacing$spac2() throws Throwable { doTest(); }
  public void testSpacing$spac3() throws Throwable { doTest(); }
  public void testSpacing$tags2() throws Throwable { doTest(); }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/formatter/";
  }
}