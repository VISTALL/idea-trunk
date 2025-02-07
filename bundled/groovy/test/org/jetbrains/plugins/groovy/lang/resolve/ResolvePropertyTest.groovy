/*
 *  Copyright 2000-2007 JetBrains s.r.o.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.jetbrains.plugins.groovy.lang.resolve;


import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PropertyUtil
import org.jetbrains.plugins.groovy.GroovyFileType
import org.jetbrains.plugins.groovy.lang.psi.GrReferenceElement
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase
import org.jetbrains.plugins.groovy.lang.psi.api.GroovyResolveResult
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrAssignmentExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrAccessorMethod
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrEnumConstant
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.GrTopStatement

/**
 * @author ven
 */
public class ResolvePropertyTest extends GroovyResolveTestCase {
  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/testdata/resolve/property/";
  }

  public void testParameter1() throws Exception {
    doTest("parameter1/A.groovy");
  }

  public void testClosureParameter1() throws Exception {
    doTest("closureParameter1/A.groovy");
  }

  public void testClosureOwner() throws Exception {
    PsiReference ref = configureByFile("closureOwner/A.groovy");
    PsiElement resolved = ref.resolve();
    assertInstanceOf(resolved, GrVariable);
    assertEquals(((PsiClassType) ((GrVariable) resolved).getTypeGroovy()).getCanonicalText(), "W");
  }

  public void testLocal1() throws Exception {
    doTest("local1/A.groovy");
  }

  public void testField1() throws Exception {
    doTest("field1/A.groovy");
  }

  public void testField2() throws Exception {
    doTest("field2/A.groovy");
  }

  public void testForVariable1() throws Exception {
    doTest("forVariable1/ForVariable.groovy");
  }

  public void testArrayLength() throws Exception {
    doTest("arrayLength/A.groovy");
  }

  public void testFromGetter() throws Exception {
    PsiReference ref = configureByFile("fromGetter/A.groovy");
    assertTrue(ref.resolve() instanceof GrAccessorMethod);
  }

  public void testFromSetter() throws Exception {
    PsiReference ref = configureByFile("fromSetter/A.groovy");
    assertTrue(ref.resolve() instanceof GrAccessorMethod);
  }

  public void testForVariable2() throws Exception {
    doTest("forVariable2/ForVariable.groovy");
  }

  public void testCatchParameter() throws Exception {
    doTest("catchParameter/CatchParameter.groovy");
  }

  public void testCaseClause() throws Exception {
    doTest("caseClause/CaseClause.groovy");
  }

  public void testGrvy104() throws Exception {
    doTest("grvy104/Test.groovy");
  }

  public void testGrvy270() throws Exception {
    PsiReference ref = configureByFile("grvy270/Test.groovy");
    assertNull(ref.resolve());
  }

  public void testGrvy1483() throws Exception {
    PsiReference ref = configureByFile("grvy1483/Test.groovy");
    assertNotNull(ref.resolve());
  }

  public void testField3() throws Exception {
    GrReferenceElement ref = (GrReferenceElement) configureByFile("field3/A.groovy").getElement();
    GroovyResolveResult resolveResult = ref.advancedResolve();
    assertTrue(resolveResult.getElement() instanceof GrField);
    assertFalse(resolveResult.isValidResult());
  }

  public void testToGetter() throws Exception {
    GrReferenceElement ref = (GrReferenceElement) configureByFile("toGetter/A.groovy").getElement();
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrMethod);
    assertTrue(PropertyUtil.isSimplePropertyGetter((PsiMethod) resolved));
  }

  public void testToSetter() throws Exception {
    GrReferenceElement ref = (GrReferenceElement) configureByFile("toSetter/A.groovy").getElement();
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrMethod);
    assertTrue(PropertyUtil.isSimplePropertySetter((PsiMethod) resolved));
  }

  public void testUndefinedVar1() throws Exception {
    PsiReference ref = configureByFile("undefinedVar1/A.groovy");
    PsiElement resolved = ref.resolve();
    assertInstanceOf(resolved, GrReferenceExpression);
    GrTopStatement statement = ((GroovyFileBase) resolved.getContainingFile()).getTopStatements()[2];
    assertTrue(resolved.equals(((GrAssignmentExpression) statement).getLValue()));
  }

  public void testRecursive1() throws Exception {
    PsiReference ref = configureByFile("recursive1/A.groovy");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrField);
  }

  public void testRecursive2() throws Exception {
    PsiReference ref = configureByFile("recursive2/A.groovy");
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrMethod);
    assertNull(((GrMethod) resolved).getReturnType());
  }

  public void testNotAField() throws Exception {
    PsiReference ref = configureByFile("notAField/A.groovy");
    assertNull(ref.resolve());
  }

  public void testUndefinedVar2() throws Exception {
    doUndefinedVarTest("undefinedVar2/A.groovy");
  }

  public void testDefinedVar1() throws Exception {
    doTest("definedVar1/A.groovy");
  }

  public void testOperatorOverload() throws Exception {
    doTest("operatorOverload/A.groovy");
  }

  public void testEnumConstant() throws Exception {
    PsiReference ref = configureByFile("enumConstant/A.groovy");
    assertTrue(ref.resolve() instanceof GrEnumConstant);
  }

  public void testStackOverflow() throws Exception {
    doTest("stackOverflow/A.groovy");
  }

  public void testFromDifferentCaseClause() throws Exception {
    PsiReference ref = configureByFile("fromDifferentCaseClause/A.groovy");
    assertNull(ref.resolve());
  }

  public void testNotSettingProperty() throws Exception {
    PsiReference ref = configureByFile("notSettingProperty/A.groovy");
    assertNull(ref.resolve());
  }

  public void testGrvy633() throws Exception {
    PsiReference ref = configureByFile("grvy633/A.groovy");
    assertNull(ref.resolve());
  }

  public void testGrvy575() throws Exception {
    doTest("grvy575/A.groovy");
  }

  public void testGrvy747() throws Exception {
    PsiReference ref = configureByFile("grvy747/A.groovy");
    assertTrue(ref.resolve() instanceof GrField);
  }

  public void testClosureCall() throws Exception {
    PsiReference ref = configureByFile("closureCall/ClosureCall.groovy");
    assertTrue(ref.resolve() instanceof GrVariable);
  }

  public void testUnderscoredField() throws Exception {
    PsiReference ref = configureByFile("underscoredField/UnderscoredField.groovy");
    final GrField field = assertInstanceOf(ref.resolve(), GrField.class);
    assertTrue(ref.isReferenceTo(field.getGetters()[0]));
    assertTrue(ref.isReferenceTo(field));
  }

  public void testPropertyWithoutField1() throws Exception {
    PsiReference ref = configureByFile("propertyWithoutField1/PropertyWithoutField1.groovy");
    assertInstanceOf(ref.resolve(), GrMethod.class);
  }

  public void testPropertyWithoutField2() throws Exception {
    PsiReference ref = configureByFile("propertyWithoutField2/PropertyWithoutField2.groovy");
    assertInstanceOf(ref.resolve(), GrMethod.class);
  }

  public void testFieldAssignedInTheSameMethod() throws Exception {
    PsiReference ref = configureByFile("fieldAssignedInTheSameMethod/FieldAssignedInTheSameMethod.groovy");
    assertInstanceOf(ref.resolve(), GrField.class);
  }

  public void testPrivateFieldAssignment() throws Throwable {
    myFixture.configureByText(GroovyFileType.GROOVY_FILE_TYPE, """
                          class Aaaaa {
                            final def aaa

                            def foo() {
                              a<caret>aa = 2
                            }
                          }""")
    def reference = myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset)
    assertInstanceOf(reference.resolve(), GrField.class)
  }

  public void testOverriddenGetter() throws Throwable {
    myFixture.configureByText("a.groovy", """interface Foo {
                                              def getFoo()
                                            }
                                            interface Bar extends Foo {
                                              def getFoo()
                                            }

                                            Bar b
                                            b.fo<caret>o""")
    def reference = myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset)
    assertEquals("Bar", assertInstanceOf(reference.resolve(), GrMethod.class).containingClass.name)
  }

  public void testIDEADEV40403() {
    myFixture.configureByFile("IDEADEV40403/A.groovy");
    def reference = myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset);
    def resolved = reference.resolve()
    assertInstanceOf(resolved, PsiMethod.class);
    def clazz = resolved.containingClass
    assertEquals "Script", clazz.name
  }

  public void testBooleanGetterPropertyAccess() {
    myFixture.configureByText("a.groovy", "print([].em<caret>pty)");
    def ref = myFixture.file.findReferenceAt(myFixture.editor.caretModel.offset)
    def resolved = ref.resolve()
    assertInstanceOf resolved, PsiMethod
  }

  private void doTest(String fileName) throws Exception {
    PsiReference ref = configureByFile(fileName);
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrVariable);
  }

  private void doUndefinedVarTest(String fileName) throws Exception {
    PsiReference ref = configureByFile(fileName);
    PsiElement resolved = ref.resolve();
    assertTrue(resolved instanceof GrReferenceExpression);
  }
}