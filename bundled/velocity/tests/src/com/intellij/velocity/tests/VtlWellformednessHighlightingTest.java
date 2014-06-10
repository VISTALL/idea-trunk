/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.velocity.inspections.wellformedness.VtlInterpolationsInspection;

/**
 * @author Alexey Chmutov
 */
public class VtlWellformednessHighlightingTest extends VtlHighlightingTestCase {

    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/highlighting/wellformedness/";
    }

    public void testFormalNotation() throws Throwable {
        doTest();
    }

    private void doTest() throws Throwable {
        doTest(new VtlInterpolationsInspection());
    }


}
