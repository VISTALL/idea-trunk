package com.intellij.velocity.tests;

import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.velocity.lexer.VtlLexer;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Alexey Chmutov
 *         Date: 03.04.2008
 */
public class VtlLexerTest extends UsefulTestCase {

    public void testFormalReference() throws Throwable {
        doTest();
    }

    public void testSimpleReference() throws Throwable {
        doTest();
    }

    public void testSpecialSymbols() throws Throwable {
        doTest();
    }

    public void testNestedReferences() throws Throwable {
        doTest();
    }

    public void testComments() throws Throwable {
        doTest();
    }

    public void testNestedComments() throws Throwable {
        doTest();
    }

    public void testDoubleQuoted() throws Throwable {
        doTest();
    }

    public void testForeach() throws Throwable {
        doTest();
    }

    public void testIfElseifElseEnd() throws Throwable {
        doTest();
    }

    public void testListWithinSet() throws Throwable {
        doTest();
    }

    public void testLogicalOperators() throws Throwable {
        doTest();
    }

    public void testLogicalOperatorsAlternative() throws Throwable {
        doTest();
    }

    public void testMacroCall() throws Throwable {
        doTest();
    }

    public void testMacroDecl() throws Throwable {
        doTest();
    }

    public void testSilentReference() throws Throwable {
        doTest();
    }

    public void testSingleQuoted() throws Throwable {
        doTest();
    }

    public void testNestedParentheses() throws Throwable {
        doTest();
    }

    public void testLiterals() throws Throwable {
        doTest();
    }

    public void testDirectiveNames() throws Throwable {
        doTest();
    }

    public void testDirectiveNamesFormal() throws Throwable {
        doTest();
    }

    public void testLexerMultiplePasses() throws Throwable {
        Lexer lexer = new VtlLexer();
        doTest(lexer, getTestInput("1"), getExpected("1"));
        doTest(lexer, getTestInput("2"), getExpected("2"));
    }

    private void doTest() throws IOException {
        doTest(new VtlLexer(), getTestInput(""), getExpected(""));
    }

    private String getTestInput(String postfix) {
        return Util.getInputData(getDataSubpath(), getTestName(true) + postfix);
    }

    private String getExpected(String postfix) {
        return Util.getExpectedResultFilePath(getDataSubpath(), getTestName(true) + postfix);
    }

    private void doTest(Lexer lexer, String testText, String expected) throws IOException {
      lexer.start(testText);
        String result = "";
        for (; ;) {
            IElementType tokenType = lexer.getTokenType();
            if (tokenType == null) {
                break;
            }
            String tokenText = getTokenText(lexer);
            String tokenTypeName = tokenType.toString();
            String line = tokenTypeName + " ('" + tokenText + "')\n";
            result += line;
            lexer.advance();
        }
//        if(!(new File(expected).exists())) {
//            final FileWriter writer = new FileWriter(expected);
//            writer.write(result);
//            writer.close();
//        }

        assertSameLinesWithFile(expected, result);
    }

    private static String getTokenText(Lexer lexer) {
        return lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
    }

    protected String getDataSubpath() {
        return "svnPlugins/velocity/tests/testData/lexer";
    }

}
