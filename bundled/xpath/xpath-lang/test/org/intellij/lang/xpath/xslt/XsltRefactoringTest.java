package org.intellij.lang.xpath.xslt;

import org.intellij.lang.xpath.TestBase;
import org.intellij.lang.xpath.xslt.refactoring.extractTemplate.XsltExtractTemplateAction;

import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.openapi.fileTypes.StdFileTypes;

/*
* Created by IntelliJ IDEA.
* User: sweinreuter
* Date: 13.02.2009
*/
public class XsltRefactoringTest extends TestBase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final CodeStyleSettings settings = CodeStyleSettingsManager.getInstance(myFixture.getProject()).getCurrentSettings();
        settings.XML_SPACE_INSIDE_EMPTY_TAG = true;
        settings.getIndentOptions(StdFileTypes.XML).INDENT_SIZE = 2;
    }

    public void testExtractTemplate() throws Throwable {
        doExtractTemplate();
    }

    public void testExtractTemplateOneVar() throws Throwable {
        doExtractTemplate();
    }

    public void testExtractTemplateOneVar2() throws Throwable {
        doExtractTemplate();
    }

    public void testExtractTemplateTwoVars() throws Throwable {
        doExtractTemplate();
    }

    public void testExtractTemplateTwoVars2() throws Throwable {
        doExtractTemplate();
    }

    public void testExtractTemplateUnresolvedVar() throws Throwable {
        doExtractTemplate();
    }

    public void testExtractTemplateUnresolvedVar2() throws Throwable {
        doExtractTemplate();
    }

    private void doExtractTemplate() throws Throwable {
        myFixture.configureByFile(getTestFileName() + ".xsl");

        final XsltExtractTemplateAction action = new XsltExtractTemplateAction();
        assertTrue(action.invokeImpl(myFixture.getEditor(), myFixture.getFile(), "foo"));

        myFixture.checkResultByFile(getTestFileName() + "_after.xsl");
    }

    @Override
    protected String getSubPath() {
        return "xslt/refactoring";
    }
}
