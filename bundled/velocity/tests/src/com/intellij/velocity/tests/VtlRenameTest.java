/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author Alexey Chmutov
 */
public class VtlRenameTest extends JavaCodeInsightFixtureTestCase {


    public void testMacroFromDecl() throws Throwable {
        doTest("newName");
    }

    public void testMacroFromUsage() throws Throwable {
        doTest("newName");
    }

    public void testMacroParamFromDecl() throws Throwable {
        doTest("newName");
    }

    public void testMacroParamFromUsage() throws Throwable {
        doTest("newName");
    }

    public void testImplicitVariableName() throws Throwable {
        doTest("newName");
    }

    public void testImplicitVariableType() throws Throwable {
        Util.addEmptyJavaClassTo(myFixture);
        doTest("NewClass");
    }

    public void testAssignmentFromDecl() throws Throwable {
        doTest("newName");
    }

    public void testAssignmentFromUsage() throws Throwable {
        doTest("newName");
    }

    public void testImplicitAssignment() throws Throwable {
        doTest("newName");
    }

    public void testDontRenameVelocityCount() throws Throwable {
        doTest("newName");
    }

    public void testRenameVelocityCountVariable() throws Throwable {
        doTest("newName");
    }

    public void testRenameLoopVariable() throws Throwable {
        doTest("newName");
    }

    public void testJavaMethod() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTest("returnNew");
    }

    public void testJavaProperty() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTest("getPropNewObj");
    }

    public void testJavaPropertySet() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTest("setPropNew");
    }

    public void testJavaPropertiesFirstCharInBothCases() throws Throwable {
        Util.addJavaClassWith3Getters(myFixture);
        doTest("getUpperCase");
    }

    public void testJavaPropertyFirstCharInUpperCase() throws Throwable {
        Util.addJavaClassWith2Getters(myFixture);
        doTest("getlowerCase");
    }

    public void testPropertyReference() throws Throwable {
        myFixture.addFileToProject("someBundle.properties", "my.prop.ref=prop value");
        doTest("my.prop.newname");
    }

    private void doTest(final String newName) throws Throwable {
        String inputDataFileName = Util.getInputDataFileName(getTestName(true));
        String expectedResultFileName = Util.getExpectedResultFileName(getTestName(true));
        myFixture.testRename(inputDataFileName, expectedResultFileName, newName);
    }

    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/rename/";
    }
}