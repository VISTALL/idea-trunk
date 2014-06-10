package org.jetbrains.plugins.groovy.grails.parser;

import org.jetbrains.plugins.grails.fileType.GspFileType;

import java.io.IOException;

/**
 * @author ilyas
 */
public class GspParserTest extends GspParsingTestCase {
  public void testDir$dir1() throws Throwable { doTest(); }
  public void testErr$ter1() throws Throwable { doTest(); }
  public void testInject$escaped1() throws Throwable { doTest(); }
  public void testInject$GRVY_943() throws Throwable { doTest(); }
  public void testSimple$bubug1() throws Throwable { doTest(); }
  public void testSimple$clos1() throws Throwable { doTest(); }
  public void testSimple$clos2() throws Throwable { doTest(); }
  public void testSimple$common() throws Throwable { doTest(); }
  public void testSimple$form1() throws Throwable { doTest(); }
  public void testSimple$megap1() throws Throwable { doTest(); }
  public void testSimple$mmm1() throws Throwable { doTest(); }
  public void testSimple$peter2() throws Throwable { doTest(); }
  public void testTags$act1() throws Throwable { doTest(); }
  public void testTags$act2() throws Throwable { doTest(); }
  public void testTags$gps9() throws Throwable { doTest(); }
  public void testTags$orph1() throws Throwable { doTest(); }
  public void testTags$tag3() throws Throwable { doTest(); }

  private void doTest() throws IOException {
    doTest(GspFileType.GSP_FILE_TYPE.getLanguage());
  }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/parser/gsp/";
  }
}
