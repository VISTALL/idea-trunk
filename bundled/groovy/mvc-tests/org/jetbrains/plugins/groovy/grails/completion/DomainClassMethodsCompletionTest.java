package org.jetbrains.plugins.groovy.grails.completion;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.plugins.groovy.lang.completion.CompletionTestBase;
import org.jetbrains.plugins.groovy.util.TestUtils;
import org.jetbrains.plugins.groovy.grails.GrailsTestUtil;

/**
 * @author ilyas
 */
public class DomainClassMethodsCompletionTest extends CompletionTestBase {

  public void testDyn1() throws Throwable { doTest(); }
  public void testFinder1() throws Throwable { doTest(); }
  public void testFinder10() throws Throwable { doTest(); }
  public void testFinder11() throws Throwable { doTest(); }
  public void testFinder2() throws Throwable { doTest(); }
  public void testFinder3() throws Throwable { doTest(); }
  public void testFinder4() throws Throwable { doTest(); }
  public void testFinder5() throws Throwable { doTest(); }
  public void testFinder6() throws Throwable { doTest(); }
  public void testFinder7() throws Throwable { doTest(); }
  public void testFinder8() throws Throwable { doTest(); }
  public void testFinder9() throws Throwable { doTest(); }
  public void testFinder_full() throws Throwable { doTest(); }
  public void testList_order() throws Throwable { doTest(); }
  public void testNot_ref() throws Throwable { doTest(); }
  public void testQulaified() throws Throwable { doTest(); }
  public void testStatic() throws Throwable { doTest(); }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/oldCompletion/domain/";
  }

  @Override
  protected void tuneFixture(JavaModuleFixtureBuilder fixtureBuilder) {
    fixtureBuilder.addLibraryJars("GRAILS", GrailsTestUtil.getMockGrailsLibraryHome(), TestUtils.GRAILS_JAR);
    fixtureBuilder.addLibraryJars("GROOVY", GrailsTestUtil.getMockGrailsLibraryHome(), TestUtils.GROOVY_JAR);
    String path = FileUtil.toSystemIndependentName(PathManager.getHomePath()) + "/svnPlugins/groovy/mvc-testdata/mockDomainDir/domain";
    fixtureBuilder.addContentRoot(path).addSourceRoot("");
  }
}
