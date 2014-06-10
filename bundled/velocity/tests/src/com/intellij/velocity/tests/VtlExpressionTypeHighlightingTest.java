/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.velocity.inspections.VtlTypesInspection;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;

/**
 * @author Alexey Chmutov
 */
public class VtlExpressionTypeHighlightingTest extends VtlHighlightingTestCase {

    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/highlighting/expressionType/";
    }

    @Override
    protected void tuneFixture(final JavaModuleFixtureBuilder moduleBuilder) {
        moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    }

    public void testIntMinusString() throws Throwable {
        doTest();
    }

    public void testIntPlusString() throws Throwable {
        doTest();
    }

    public void testBooleanLtBoolean() throws Throwable {
        doTest();
    }

    public void testBooleanMinusInt() throws Throwable {
        doTest();
    }

    public void testIntDivideDouble() throws Throwable {
        doTest();
    }

    public void testIntOrString() throws Throwable {
        doTest();
    }

    public void testIllegalIterableType() throws Throwable {
        doTest();
    }

    public void testLegalIterableType() throws Throwable {
        doTest();
    }

    private void doTest() throws Throwable {
        myFixture.enableInspections(new VtlTypesInspection());
        myFixture.testHighlighting(true, true, true, getInputDataFilePath());
    }


}