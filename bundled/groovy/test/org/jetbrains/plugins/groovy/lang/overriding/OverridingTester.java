package org.jetbrains.plugins.groovy.lang.overriding;

import com.intellij.psi.PsiMethod;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.util.TestUtils;

import java.util.Arrays;
import java.util.List;

/**
 * User: Dmitry.Krasilschikov
 * Date: 31.07.2007
 */
public abstract class OverridingTester extends CodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/testData/overriding/";
  }

  public void doTest() throws Throwable {
    final String testFile = getTestName(true) + ".test";
    final List<String> strings = TestUtils.readInput(getTestDataPath() + "/" + testFile);
    GroovyFileBase psiFile = (GroovyFileBase) myFixture.addFileToProject("foo.groovy", strings.get(0));

    StringBuffer buffer = new StringBuffer();

    GrTypeDefinition[] grTypeDefinitions = psiFile.getTypeDefinitions();
    GrTypeDefinition lastTypeDefinition = psiFile.getTypeDefinitions()[grTypeDefinitions.length - 1];

    PsiMethod[] psiMethods = lastTypeDefinition.getMethods();

    for (PsiMethod method : psiMethods) {
      PsiMethod[] superMethods = findMethod(method);
      String[] classes = sortUseContaingClass(superMethods);

      for (String classAsString : classes) {
        buffer.append(classAsString);
        buffer.append("\n");   //between different super methods
      }
      buffer.append("\n");   //between different methods
    }
    buffer.append("\n");  //metween class definitions

    assertEquals(strings.get(1), buffer.toString().trim());
  }

  private String[] sortUseContaingClass(PsiMethod[] psiMethods) {
    String[] classes = new String[psiMethods.length];

    for (int i = 0; i < psiMethods.length; i++) {
      PsiMethod psiMethod = psiMethods[i];
      classes[i] = psiMethod.getContainingClass().toString() + ": " + psiMethod.getContainingClass().getName() +
          "; " + psiMethod.getHierarchicalMethodSignature().toString();
    }
    Arrays.sort(classes);

    return classes;
  }

  abstract PsiMethod[] findMethod(PsiMethod method);

}