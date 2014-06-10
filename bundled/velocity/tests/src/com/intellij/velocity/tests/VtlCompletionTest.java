/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.velocity.tests;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.velocity.inspections.DefineInCommentIntention;
import junit.framework.Assert;
import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Alexey Chmutov
 */
public class VtlCompletionTest extends JavaCodeInsightFixtureTestCase {

    public void testImplicitVariable() throws Throwable {
        doTest();
    }

    public void testImplicitVariableDeclarationType() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTest();
    }

    public void testAssignmentDeclaration() throws Throwable {
        doTest();
    }

    public void testImplicitVariableAssignment() throws Throwable {
        doTest();
    }

    public void testDirectiveName() throws Throwable {
        doTestCompletionVariants("define", "evaluate", "foreach", "if", "include", "literal", "macro", "parse", "set", "stop");
    }

    public void testDirectiveAndMacroName() throws Throwable {
        List<String> nameList = new ArrayList<String>(Arrays.asList("define", "evaluate", "foreach", "if", "include", "literal", "macro", "parse", "set", "stop", "mymacro"));
        doTestCompletionVariants(nameList.toArray(new String[nameList.size()]));
    }

    public void testDirectiveNameFormal() throws Throwable {
        doTestCompletionVariants("define", "evaluate", "foreach", "if", "include", "literal", "macro", "parse", "set", "stop");
    }

    public void testDirectiveAndMacroNameFormal() throws Throwable {
        List<String> nameList = new ArrayList<String>(Arrays.asList("define", "evaluate", "foreach", "if", "include", "literal", "macro", "parse", "set", "stop", "mymacro"));
        doTestCompletionVariants(nameList.toArray(new String[nameList.size()]));
    }

    public void testDirectiveNameWithinElseDirective() throws Throwable {
        doTestCompletionVariants("define", "evaluate", "foreach", "if", "include", "literal", "end", "parse", "set", "stop");
    }

    public void testDirectiveNameWithinForeachDirective() throws Throwable {
        doTest();
    }

    public void testDirectiveNameWithinIfDirective() throws Throwable {
        doTestCompletionVariants("define", "evaluate", "foreach", "if", "include", "literal", "else", "elseif", "end", "parse", "set", "stop");
    }

    public void testDirectiveNameBreak() throws Throwable {
        doTestCompletionVariants("define", "evaluate", "foreach", "if", "include", "literal", "break", "end", "parse", "set", "stop");
    }

    public void testDirectiveNameWithBraceAndParentheses() throws Throwable {
        doTest();
    }

    public void testDirectiveNameWithBracesAndParentheses() throws Throwable {
        doTest();
    }

    public void testDirectiveNameWithParentheses() throws Throwable {
        doTest();
    }

    public void testDirectiveNameWithSpacesAndParentheses() throws Throwable {
        doTest();
    }

    public void testDirectiveNameWithBracesSpacesAndParentheses() throws Throwable {
        doTest();
    }

    public void testMacroNameWithBrace() throws Throwable {
        doTest();
    }

    public void testBeanPropertyTypeName() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTestVariantTypeNames("int", "int", "MyClass");
    }

    public void testBeanPropertyTypeNameFromSetter() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTestVariantTypeNames("int", "int");
    }

    public void testVelocityVariableTypeName() throws Throwable {
        Util.addEmptyJavaClassTo(myFixture);
        doTestVariantTypeNames("Double", "Bar", "Object", "Integer", "Boolean");
    }

    public void testVoidTypeAssignedVariable() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTestVariantTypeNames("void", "MyClass");
    }

    public void testUnqualifyTypeNames() throws Throwable {
        Util.addEmptyJavaClassTo(myFixture);
        doTestVariantTypeNames("Map<Bar, String>", "String", "Integer", "Boolean");
    }

  private void doTestVariantTypeNames(String... expected) throws Throwable {
    doTest();
    final Function<LookupElement, String> typeNameExtractor = new Function<LookupElement, String>() {
      public String fun(final LookupElement element) {
        final LookupElementPresentation mockPresentation = new LookupElementPresentation(true);
        element.renderElement(mockPresentation);
        return mockPresentation.getTypeText();
      }
    };
    LookupElement[] elements = myFixture.completeBasic();
    String[] typeNames = ContainerUtil.map2Array(elements, String.class, typeNameExtractor);
    assertOrderedEquals(typeNames, expected);
  }

    public void testJavaField() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTest();
        LookupElement[] completionVariants = myFixture.completeBasic();
        assertEmpty(completionVariants);
    }

    public void testJavaMethod() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTest();
    }

    public void testJavaPropertyGetting() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTestCompletionVariants("prop1", "prop2", "propObject");
    }

    public void testJavaPropertySetting() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTestCompletionVariants("prop1", "prop3");
    }

    public void testJavaPropertySettingSmart() throws Throwable {
        Util.addJavaClassTo(myFixture);
        doTestCompletionVariants(CompletionType.SMART ,"prop1", "prop3");
    }

    public void testExposePublicVariantsOnly() throws Throwable {
        Util.addJavaSubclassTo(myFixture);
        doTestCompletionVariants("getClass", "getProp1", "getProp2", "getPropObject", "class", "prop1", "prop2", "propObject", "equals", "hashCode", "meth1", "meth11", "returnObj", "setProp1", "setProp3", "toString");
    }

    public void testDontExposeWaitNotifyFromObject() throws Throwable {
        doTestCompletionVariants("getClass", "class", "equals", "hashCode", "toString");
    }

    public void testDontExposeInnerClasses() throws Throwable {
        Util.addJavaClassWithInnerClassTo(myFixture);
        doTestCompletionVariants("getClass", "class", "equals", "hashCode", "meth11", "toString");
    }

    public void testImplicitVariableExternal() throws Throwable {
        myFixture.copyFileToProject(DefineInCommentIntention.VELOCITY_IMPLICIT_VM);
        doTestCompletionVariants("someVarExternal", "someVar");
    }

    public void testMacroFromExternalLibrary() throws Throwable {
        myFixture.copyFileToProject(DefineInCommentIntention.VELOCITY_IMPLICIT_VM);
        myFixture.copyFileToProject("macroLibrary.vm");
        doTestCompletionVariants("secondMacro", "set", "stop");
    }

    public void testMacroFromVelocityPropertiesLibrary() throws Throwable {
        myFixture.copyFileToProject("velocity.properties");
        myFixture.copyFileToProject("macroLibrary.vm");
        doTestCompletionVariants("secondMacro", "set", "stop");
    }

    public void testFileNameUsingVelocityProperties() throws Throwable {
        myFixture.copyFileToProject("velocity.properties");
        myFixture.copyDirectoryToProject("lib", "lib");
        doTestCompletionVariants("fileNameUsingVelocityProperties.test.vm", "fileToParse.vm", "lib", "velocity.properties");
    }

    public void testSpringMacroLibrary() throws Throwable {
        myFixture.addFileToProject(
                "org/springframework/web/servlet/view/velocity/spring.vm",
                "#macro( mockSpringMacro $param1 )!mock!#end");
        doTest();
    }

    private void doTestCompletionVariants(@NonNls String... expectedItems) throws Throwable {
        String inputDataFileName = Util.getInputDataFileName(getTestName(true));
        myFixture.testCompletionVariants(inputDataFileName, expectedItems);
    }

    private void doTestCompletionVariants(final CompletionType type, @NonNls String... expectedItems) throws Throwable {
        String inputDataFileName = Util.getInputDataFileName(getTestName(true));
        List<String> result = getCompletionVariants(inputDataFileName, type);
        UsefulTestCase.assertSameElements(result, expectedItems);
    }

    private List<String> getCompletionVariants(final String fileBefore, final CompletionType type) throws Throwable {
        myFixture.configureByFiles(fileBefore);
        final LookupElement[] items = myFixture.complete(type);
        Assert.assertNotNull("No lookup was shown, probably there was only one lookup element that was inserted automatically", items);
        return myFixture.getLookupElementStrings();
    }

    private void doTest() throws Throwable {
        String inputDataFileName = Util.getInputDataFileName(getTestName(true));
        String expectedResultFileName = Util.getExpectedResultFileName(getTestName(true));
        myFixture.testCompletion(inputDataFileName, expectedResultFileName);
    }

    @Override
    protected String getBasePath() {
        return "/svnPlugins/velocity/tests/testData/completion/";
    }

    @Override
    protected void tuneFixture(final JavaModuleFixtureBuilder moduleBuilder) {
        moduleBuilder.setMockJdkLevel(JavaModuleFixtureBuilder.MockJdkLevel.jdk15);
    }
}
