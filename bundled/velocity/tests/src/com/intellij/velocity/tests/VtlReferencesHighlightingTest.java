/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.velocity.inspections.VtlFileReferencesInspection;
import com.intellij.velocity.inspections.VtlReferencesInspection;

import java.util.Properties;

/**
 * @author Alexey Chmutov
 */
public class VtlReferencesHighlightingTest extends VtlHighlightingTestCase {

  @Override
  protected String getBasePath() {
    return "/svnPlugins/velocity/tests/testData/highlighting/references/";
  }

  public void testImplicitVariableAssignment() throws Throwable {
    doTest();
  }

  public void testResolvedImplicitVariable() throws Throwable {
    doTest();
  }

  public void testResolvedMacroCall() throws Throwable {
    doTest();
  }

  public void testNotAMacroCall() throws Throwable {
    doTest();
  }

  public void testUnresolvedImplicitVariable() throws Throwable {
    doTest();
  }

  public void testUnresolvedMacroCall() throws Throwable {
    doTest();
  }

  public void testVariableDeclaredByAssignment() throws Throwable {
    doTest();
  }

  public void testJavaMethod() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testJavaProperty() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testJavaPropertyReadOnly() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testJavaPropertyWriteOnly() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testJavaReferencesChains() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testLiteralExpressionTypes() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testImplicitVariableType() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testAssignedVariableType() throws Throwable {
    Util.addJavaClassTo(myFixture);
    doTest();
  }

  public void testIncludeFileReference() throws Throwable {
    myFixture.getTempDirFixture().createFile("file.xml");
    myFixture.getTempDirFixture().createFile("dir/file.html");
    doTest(new VtlFileReferencesInspection());
  }

  public void testParseFileReference() throws Throwable {
    myFixture.getTempDirFixture().createFile("bbb/valid/a.xml");
    myFixture.getTempDirFixture().createFile("bbb/b.ftl");
    myFixture.getTempDirFixture().createFile("c.ftl");
    myFixture.enableInspections(new VtlFileReferencesInspection(), new VtlReferencesInspection());
    myFixture.testHighlighting(true, true, true, getInputDataFilePath(), "file1.vm", "file2.vm");
  }

  public void testIdeTemplateFile() throws Throwable {
    VirtualFile vFile = myFixture.copyFileToProject(getInputDataFilePath());
    PsiFile file = myFixture.getPsiManager().findFile(vFile);
    file.getViewProvider().putUserData(FileTemplateManager.DEFAULT_TEMPLATE_PROPERTIES, new Properties());
    myFixture.enableInspections(new VtlFileReferencesInspection(), new VtlReferencesInspection());
    myFixture.testHighlighting(true, true, true, vFile);
  }

  public void testJavaMethodResolveByNumberOfArgs() throws Throwable {
    Util.addJavaInterfaceWithOverloadedMethodTo(myFixture);
    doTest();
  }

  private void doTest() throws Throwable {
    doTest(new VtlReferencesInspection());
  }
}
