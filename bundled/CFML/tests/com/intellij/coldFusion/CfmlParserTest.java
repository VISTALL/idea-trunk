package com.intellij.coldFusion;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.io.IOException;

/**
 * Created by Lera Nikolaenko
 * Date: 14.11.2008
 */
public class CfmlParserTest extends LightCodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();    //To change body of overridden methods use File | Settings | File Templates.
    }

    // public void testInit() throws Throwable {}
    public void testUnclosedCfset() throws Throwable {
        doTest();
    }

    public void testOpenCloseTag() throws Throwable {
        doTest();
    }

    public void testSingleTag() throws Throwable {
        doTest();
    }

    public void testExprInAttr() throws Throwable {
        doTest();
    }

    public void testNestedSharps() throws Throwable {
        doTest();
    }

    // tests on incorrect input
    public void testIunbalanceTags() throws Throwable {
        doTest();
    }

    public void testIunbalanceSharps() throws Throwable {
        doTest();
    }

    public void testIquotesInSharps() throws Throwable {
        doTest();
    }

    public void testIbadToken() throws Throwable {
        doTest();
    }

    public void testNonUniqueUnclosedTag() throws Throwable {
        doTest();
    }

    private void doTest() throws IOException {
        String fileName = getTestName(true) + ".cfml";

        String testText = Util.getInputData(getDataSubpath(), getTestName(true));
        final PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText(fileName, testText);
        final String tree = DebugUtil.psiTreeToString(psiFile, true);
        String expected = Util.getExpectedDataFilePath(getDataSubpath(), getTestName(true));

        assertSameLinesWithFile(expected, tree);
    }

    protected String getDataSubpath() {
        return "/svnPlugins/CFML/tests/testData/parser";
    }
}
