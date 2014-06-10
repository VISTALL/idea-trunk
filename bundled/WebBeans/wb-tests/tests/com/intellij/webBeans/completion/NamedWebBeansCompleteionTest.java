package com.intellij.webBeans.completion;

public class NamedWebBeansCompleteionTest extends AbstractWebBeansCompletionTest {

  public void testNamedBeans() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("page_1.jsp", "page_1_after.jsp");
    myFixture.testCompletion("page_2.jsp", "page_2_after.jsp");
  }

  public void testKnownStereotypesWithEncapsulatedNamedAnno() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("page_3.jsp", "page_3_after.jsp");
    myFixture.testCompletion("page_4.jsp", "page_4_after.jsp");
  }

  public void testCustomStereotypesWithEncapsulatedNamedAnno() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("page_5.jsp", "page_5_after.jsp");
    myFixture.testCompletion("page_6.jsp", "page_6_after.jsp");
  }
  
  public void testFieldModelNamedBeans() throws Throwable {
    myFixture.copyDirectoryToProject("beans", "beans");

    myFixture.testCompletion("page_7.jsp", "page_7_after.jsp");
  }

  @Override
  public String getBasePath() {
    return super.getBasePath() + "NamedWebBeansCompleteion/";
  }
}
