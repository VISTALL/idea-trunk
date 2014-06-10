package com.intellij.coldFusion;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

import java.io.IOException;

/**
 * Created by Lera Nikolaenko
 * Date: 14.01.2009
 */
public class CfscriptParserTest extends LightCodeInsightFixtureTestCase {
    public void testSimpleStatement() throws Throwable {
        doTest();
    }
    public void testTwoSimpleStatements() throws Throwable { doTest(); }
    public void testIfExpression() throws Throwable { doTest(); }
    public void testIfElseExpression() throws Throwable { doTest(); }
    public void testIfElseNestedExpression() throws Throwable { doTest(); }

    public void testWhileExpression() throws Throwable { doTest(); }

    public void testEmptyOneLineComment() throws Throwable { doTest(); }
    public void testOneLineComment() throws Throwable { doTest(); }
    public void testMultiLineComment() throws Throwable { doTest(); }

    public void testInvalidDivisionOperator() throws Throwable { doTest(); }

    public void testCfml_24_try_bug() throws Throwable { doTest(); }

    public void testDotBracesConstruction() throws Throwable { doTest(); }

    public void testCfsetTag() throws Throwable { doTest(); }
    public void testBraceStructure() throws Throwable { doTest(); }

    private void doTest() throws IOException {
        String fileName = getTestName(true) + ".cfml";

        String testText = Util.getInputData(getDataSubpath(), getTestName(true));
        final PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText(fileName, testText);
        final String tree = DebugUtil.psiTreeToString(psiFile, true);
        String expected = Util.getExpectedDataFilePath(getDataSubpath(), getTestName(true));

        assertSameLinesWithFile(expected, tree);
    }

    protected String getDataSubpath() {
        return "/svnPlugins/CFML/tests/testData/cfscript/parser";
    }
}
