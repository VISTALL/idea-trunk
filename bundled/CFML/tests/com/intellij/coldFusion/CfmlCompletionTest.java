package com.intellij.coldFusion;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NonNls;

/**
 * Created by Lera Nikolaenko
 * Date: 04.12.2008
 */
public class CfmlCompletionTest extends LightCodeInsightFixtureTestCase {
    public void testAttributeCompletionAtFileEnd() throws Throwable {
        doTest();
    }

    public void testAttributeCompletionAtFileMiddle() throws Throwable {
        doTest();
    }

    public void testAttributeVariants() throws Throwable {
        doTestCompletionVariants("returntype", "returnformat", "roles");
    }

    public void testTagCompletionAtFileEnd() throws Throwable {
        doTestCompletionVariants("cffunction", "cffeed", "cffile", "cfflush", "cfform",
                "cfformgroup", "cfformitem", "cfftp");
    }

    public void testFunctionNamesCompletion() throws Throwable {
        doTestCompletionVariants("mid", "min", "mineFunc", "minute");
    }

    public void testVariableNamesCompletion() throws Throwable {
        doTestCompletionVariants("mid", "min", "mineVariable", "minute");
    }

    public void testVariableAndFunctionNamesCompletion() throws Throwable {
        doTestCompletionVariants("mineFunction", "mineVariable", "mid", "min", "minute");
    }

    public void testFunctionNameWithBracketsCompletion() throws Throwable {
        doTest();
    }

    private void doTestCompletionVariants(@NonNls String... items) throws Throwable {
        String inputDataFileName = Util.getInputDataFileName(getTestName(true));
        myFixture.testCompletionVariants(inputDataFileName, items);
    }

    private void doTest() throws Throwable {
        String inputDataFileName = Util.getInputDataFileName(getTestName(true));
        String expectedResultFileName = Util.getExpectedDataFileName(getTestName(true));
        String[] input = {inputDataFileName};
        myFixture.testCompletion(input, expectedResultFileName);
    }

    @Override
    protected String getBasePath() {
        return "/svnPlugins/CFML/tests/testData/completion";
    }
    /*
    protected String getBasePath() {
        return "/tests/testData/resolveFunction/";
    }
    */

}
