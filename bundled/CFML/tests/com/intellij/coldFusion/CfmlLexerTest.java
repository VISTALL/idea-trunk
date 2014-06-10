package com.intellij.coldFusion;

import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;

import java.io.IOException;

public class CfmlLexerTest extends UsefulTestCase {

    public void testCloseOpenTag() throws Throwable {
        doTest();
    }
    public void testAttributes() throws Throwable {
        doTest();
    }
    public void testTemplateText() throws Throwable {
        doTest();
    }
    public void testTagComment() throws Throwable {
        doTest();
    }
    public void testCommentBalance() throws Throwable {
        Lexer lexer = new CfmlLexer(true);
        String testText1 = Util.getInputData(getDataSubpath(), getTestName(true) + "1");
        String expected1 = Util.getExpectedDataFilePath(getDataSubpath(), getTestName(true) + "1");
        doFileLexerTest(lexer, testText1, expected1);

        String testText2 = Util.getInputData(getDataSubpath(), getTestName(true) + "2");
        String expected2 = Util.getExpectedDataFilePath(getDataSubpath(), getTestName(true) + "2");
        doFileLexerTest(lexer, testText2, expected2);
    }
    public void testSharpedAttributeValue() throws Throwable {
        doTest();
    }
    public void testSharpsInScript() throws Throwable {
        doTest();
    }
    public void testVarVariableName() throws Throwable { doTest(); }
    public void testVarKeyword() throws Throwable { doTest(); }
    private void doFileLexerTest(Lexer lexer, String testText, String expected) {
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
        assertSameLinesWithFile(expected, result);
    }
    private void doTest()  throws IOException {
        Lexer lexer = new CfmlLexer(true);
        String testText = Util.getInputData(getDataSubpath(), getTestName(true));
        String expected = Util.getExpectedDataFilePath(getDataSubpath(), getTestName(true));
        doFileLexerTest(lexer, testText, expected);
    }

    private static String getTokenText(Lexer lexer) {
        return lexer.getBufferSequence().subSequence(lexer.getTokenStart(), lexer.getTokenEnd()).toString();
    }

    protected String getDataSubpath() {
        return "/svnPlugins/CFML/tests/testData/lexer";
    }
}

