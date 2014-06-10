package com.intellij.coldFusion;

import com.intellij.coldFusion.model.lexer.CfmlLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.UsefulTestCase;

import java.io.IOException;

/**
 * Created by Lera Nikolaenko
 * Date: 21.01.2009
 */
public class CfscriptLexerTest extends UsefulTestCase {

    public void testSimpleScript() throws Throwable { doTest(); }
    public void testVarVariableName() throws Throwable { doTest(); }
    public void testOneLineComment() throws Throwable { doTest(); }
    public void testMultiLineComment() throws Throwable { doTest(); }

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
        return "/svnPlugins/CFML/tests/testData/cfscript/lexer";
    }
}
