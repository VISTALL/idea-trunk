/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author Alexey Chmutov
 */
public abstract class VtlHighlightingTestCase extends JavaCodeInsightFixtureTestCase {
    protected void doTest(LocalInspectionTool... tools) throws Throwable {
        myFixture.enableInspections(tools);
        myFixture.testHighlighting(true, true, true, getInputDataFilePath());
    }

    protected String getInputDataFilePath() {
        return Util.getInputDataFileName(getTestName(true));
    }
}
