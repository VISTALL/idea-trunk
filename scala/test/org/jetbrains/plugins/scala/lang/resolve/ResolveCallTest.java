package org.jetbrains.plugins.scala.lang.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunction;
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition;
import org.jetbrains.plugins.scala.lang.psi.api.statements.params.ScParameter;
import org.jetbrains.plugins.scala.util.TestUtils;

/**
 * @author ven
 */
public class ResolveCallTest extends ScalaResolveTestCase {
  public String getTestDataPath() {
    return TestUtils.getTestDataPath() + "/resolve/";
  }

  public void testObjectApply() throws Exception {
    PsiReference ref = configureByFile("call/objectApply.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunction);
    assertEquals("apply", ((ScFunction) resolved).getName());
  }

  public void testObjectGenericApply() throws Exception {
    PsiReference ref = configureByFile("call/ObjectGenericApply.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunction);
    assertEquals("apply", ((ScFunction) resolved).getName());
  }

  public void testStableRefPattern() throws Exception {
    PsiReference ref = configureByFile("call/refPattern.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunction);
    assertEquals("foo", ((ScFunction) resolved).getName());
  }

  public void testSuperConstructorInvocation() throws Exception {
    PsiReference ref = configureByFile("call/SuperConstructorInvocation.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunction);
    assertEquals("c", ((ScFunction) resolved).getContainingClass().getName());
  }

  public void testNamingParam() throws Exception {
    PsiReference ref = configureByFile("call/NamingParam.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScParameter);
  }

  public void testSimpleCallParensOmitted() throws Exception {
    PsiReference ref = configureByFile("call/simpleCallParensOmitted.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunctionDefinition);
  }

  public void testResolveAmbiguousWithTypeParameter() throws Exception {
    PsiReference ref = configureByFile("functions/typeParam1/tp1.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunctionDefinition);
    assertEquals(resolved.getText(), "def gul[A](a:A): A = null.asInstanceOf[A]");
  }

  public void testResolveAmbiguousWithout() throws Exception {
    PsiReference ref = configureByFile("functions/typeParam2/tp2.scala");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof ScFunctionDefinition);
    assertEquals(resolved.getText(), "def gul(i:Int) : Int = i");
  }

}
