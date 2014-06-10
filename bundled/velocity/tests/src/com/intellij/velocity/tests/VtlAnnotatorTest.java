/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.velocity.inspections.VtlFileReferencesInspection;
import com.intellij.velocity.inspections.VtlReferencesInspection;

/**
 * @author Alexey Chmutov
 */
public class VtlAnnotatorTest extends VtlHighlightingTestCase {

    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/highlighting/annotator/";
    }

    public void testPrimitiveTypeInCommentDeclaration() throws Throwable {
        doTest();
    }

    public void testResolvedTypeInCommentDeclaration() throws Throwable {
        doTest();
    }

    public void testUnresolvedTypeInCommentDeclaration() throws Throwable {
        doTest();
    }

    public void testMacroCallWithoutParen() throws Throwable {
        doTest();
    }

    public void testDuplicatedMacro() throws Throwable {
        doTest();
    }

    public void testDuplicatedMacroParameter() throws Throwable {
        doTest();
    }

    public void testDuplicatedMacroInParsedFile() throws Throwable {
        myFixture.enableInspections(new VtlFileReferencesInspection(), new VtlReferencesInspection());
        myFixture.testHighlighting(true, true, true, getInputDataFilePath(), "file1.vm");
    }
}