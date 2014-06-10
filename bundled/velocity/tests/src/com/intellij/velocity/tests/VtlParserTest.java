package com.intellij.velocity.tests;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Alexey Chmutov
 *         Date: 18.04.2008
 */
public class VtlParserTest extends JavaCodeInsightFixtureTestCase {

    public void testIfElseifElseEnd() throws Throwable {
        doTest();
    }

    public void testBadIfElseifElseEnd() throws Throwable {
        doTest();
    }

    public void testNestedIfElseifElseEnd() throws Throwable {
        doTest();
    }

    public void testForeach() throws Throwable {
        doTest();
    }

    public void testBreakDirective() throws Throwable {
        doTest();
    }

    public void testDefineDirective() throws Throwable {
        doTest();
    }

    public void testNestedReferences() throws Throwable {
        doTest();
    }

    public void testDoubleQuoted() throws Throwable {
        doTest();
    }

    public void testListWithinSet() throws Throwable {
        doTest();
    }

    public void testNestedParentheses() throws Throwable {
        doTest();
    }

    public void testSimpleReference() throws Throwable {
        doTest();
    }

    public void testIncludeDirective() throws Throwable {
        doTest();
    }

    public void testLogicalOperators() throws Throwable {
        doTest();
    }

    public void testLogicalOperatorsAlternative() throws Throwable {
        doTest();
    }

    public void testFormalReference() throws Throwable {
        doTest();
    }

    public void testMacroCall() throws Throwable {
        doTest();
    }

    public void testMacroDecl() throws Throwable {
        doTest();
    }

    public void testDirectiveNames() throws Throwable {
        doTest();
    }

    public void testParseDirective() throws Throwable {
        doTest();
    }

    public void testRangeOperator() throws Throwable {
        doTest();
    }

    public void testListDelimiter() throws Throwable {
        doTest();
    }

    public void testBinaryExpression() throws Throwable {
        doTest();
    }


    public void testMapWithinSet() throws Throwable {
        doTest();
    }

    private void doTest() throws IOException {
        String testText = Util.getInputData(getDataSubpath(), getTestName(true));

        final PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText("nofile.vm", testText);
        final String tree = DebugUtil.psiTreeToString(psiFile, true);
        String expected = Util.getExpectedResultFilePath(getDataSubpath(), getTestName(true));

//        if(!(new File(expected).exists())) {
//            final FileWriter writer = new FileWriter(expected);
//            writer.write(tree);
//            writer.close();
//        }

        assertSameLinesWithFile(expected, tree);
    }

    private String getDataSubpath() {
        return "svnPlugins/velocity/tests/testData/parser";
    }

}
