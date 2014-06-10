package com.intellij.coldFusion;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;

/**
 * Created by Lera Nikolaenko
 * Date: 17.02.2009
 */
public class CfscriptResolveTest extends LightCodeInsightFixtureTestCase {
    protected PsiElement resolveReferenceAtCaret() throws Throwable {
        return myFixture.getReferenceAtCaretPosition(
                Util.getInputDataFileName(getTestName(true))).resolve();
    }

    public void testInvokeResolveToTagFromTag() throws Throwable {
        doTest("fName");
    }

    public void testInvokeResolveToScriptFromTag() throws Throwable {
        doTest("fName");
    }

    public void testInvokeResolveToTagFromScript() throws Throwable {
        doTest("fName");
    }

    public void testInvokeResolveToScriptFromScript() throws Throwable {
        doTest("fName");
    }

    private void doTest(String resolveName) throws Throwable {
        /*
        PsiElement element = resolveReferenceAtCaret();
        CfmlDefinitionExpression var = assertInstanceOf(element, CfmlDefinitionExpression.class);
        assertEquals(resolveName.toLowerCase(), var.getName().toLowerCase());
        */
    }

    protected String getBasePath() {
        return "/svnPlugins/CFML/tests/testData/resolve";
    }
}
