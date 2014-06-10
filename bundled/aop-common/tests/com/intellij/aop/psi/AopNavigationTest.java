/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

/**
 * @author peter
 */
public class AopNavigationTest extends AopNavigationTestCase{
  @Override
  protected String getBasePath() {
    return "/testData/aop/highlighting/navigation";
  }

  public void testAnnoOnMethodClass() throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + ".java");
    checkNavigation(getTestName(false) + ".java", false, "Target#foo()", "MyAspect#before()");
  }

  public void testExecutionOverridingMethods() throws Throwable {
    doTest("Target#bar()\n" +
           "Middle#bar()\n" +
           "Target#foo()",
           "MyAspect#before()",
           "MyAspect#before()",
           "MyAspect#before()");
  }

  private void doTest(String... results) throws Throwable {
    myFixture.copyFileToProject(getTestName(false) + ".java");
    checkNavigation(getTestName(false) + ".java", false, results);
  }

  public void testPointcutParameters() throws Throwable {
    doTest("Target#fooString(String s)",
           "Target#fooLong(long l)",
           "Target#fooLong(long l)",
           "Target#fooLong(long l)",
           "MyAspect#beforeString(String s)",
           "MyAspect#beforeL(long l)\nMyAspect#beforeLong()\nMyAspect#beforeBoxedLong()");
  }

  public void testGenericPointcuts() throws Throwable {
    doTest("Target#raw(List list)\n" +
           "Target#object(List<Object> list)\n" +
           "Target#string(List<String> list)",
           "Target#object(List<Object> list)",
           "Target#string(List<String> list)",

           "Aspect#raw()",
           "Aspect#raw()\nAspect#object()",
           "Aspect#raw()\nAspect#string()");
  }

  public void testGenericWildcardPointcuts() throws Throwable {
    doTest("Target#unbound(List<?> list)",
           "Target#unbound(List<?> list)\n" +
           "Target#extendsObject(List<? extends Object> list)",
           "Target#unbound(List<?> list)\n" +
           "Target#extendsObject(List<? extends Object> list)",

           "Aspect#unbound()\n" +
           "Aspect#object()\n" +
           "Aspect#objectPlus()",
           "Aspect#object()\n" +
           "Aspect#objectPlus()"
    );
  }

  public void testInterfaceImplementations() throws Throwable {
    doTest(
      "Aspect#execImpl()\n" +
      "Aspect#execIntf()\n" +
      "Aspect#withinImpl()",

      "Aspect#execImpl()\n" +
      "Aspect#withinImpl()",

      "Impl#foo()",

      "Impl#bar()\n" +
      "Impl#foo()",

      "",

      "Impl#bar()\n" +
      "Impl#foo()"
    );
    
  }

}
