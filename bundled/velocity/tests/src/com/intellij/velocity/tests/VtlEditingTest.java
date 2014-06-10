/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author Alexey Chmutov
 */
public class VtlEditingTest extends JavaCodeInsightFixtureTestCase {

    public void testCloseEmptyBracesForInterpolation() throws Throwable {
        doTest('{');
    }

    public void testCloseBracesForDirective() throws Throwable {
        doTest('{');
    }

    public void testCloseIfWithEnd() throws Throwable {
        doTest('(');
    }

    public void testDontCloseIfWithEnd() throws Throwable {
        doTest('(');
    }

    public void testCloseMacroDeclWithEnd() throws Throwable {
        doTest('(');
    }

    public void testCloseForeachWithEnd() throws Throwable {
        doTest('(');
    }

    public void _testDontCloseForeachWithEnd() throws Throwable {
        doTest('(');
    }

    public void testCloseBracesForInterpolation() throws Throwable {
        doTest('{');
    }

    public void testCloseBracesForSilent() throws Throwable {
        doTest('{');
    }

    public void testDontDoubleClosingBrace() throws Throwable {
        doTest('}');
    }

    public void testInsertInterpolation() throws Throwable {
        doTest('{');
    }

    public void testDontCloseSetWithEnd() throws Throwable {
        doTest('(');
    }

    public void testDoubleQuoteOnInterpolation() throws Throwable {
        doTest('\"');
    }

    public void testSingleQuoteOnInterpolation() throws Throwable {
        doTest('\'');
    }

    public void testBackspaceOnEmptyInterpolation() throws Throwable {
        doTest('\b');
    }

    public void testBackspaceOnTemplateText() throws Throwable {
        doTest('\b');
    }

    public void testBackspaceOnInterpolation() throws Throwable {
        doTest('\b');
    }

    public void testBackspaceOnUnclosedInterpolation() throws Throwable { 
        doTest('\b');
    }

    public void testBackspaceOnDoubleQuote() throws Throwable {
        doTest('\b');
    }

    public void testBackspaceOnSingleQuote() throws Throwable {
        doTest('\b');
    }

    private void doTest(final char typed) throws Throwable {
        myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
        myFixture.type(typed);
        myFixture.checkResultByFile(Util.getExpectedResultFileName(getTestName(true)));
    }

    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/editing/";
    }

}