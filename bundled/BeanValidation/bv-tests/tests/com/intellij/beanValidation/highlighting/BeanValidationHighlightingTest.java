/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
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
 */

package com.intellij.beanValidation.highlighting;

import com.intellij.beanValidation.AbstractBeanValidationTestCase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;

/**
 * @author Konstantin Bulenkov
 */
public class BeanValidationHighlightingTest extends AbstractBeanValidationTestCase{
  private static final String validationXml = "META-INF/validation.xml";
  private static final String constraintMappingsXml = "META-INF/ConstraintMappings.xml";
  private static final String userBean = "com/bean/validation/UserClass.java";
  private static final String customAnnotation = "com/bean/validation/Anno.java";
  @NonNls private static final String USER_BEAN = "com.bean.validation.UserClass";

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.copyDirectoryToProject("", "");
  }

  public void testConstraintMappings() throws Throwable {
    myFixture.testHighlighting(constraintMappingsXml);  
  }

  public void testValidationXml() throws Exception {
    myFixture.testHighlighting(validationXml);
  }

  public void testFindUsages() throws Exception {
    final PsiClass userBean = JavaPsiFacade.getInstance(myProject).findClass(USER_BEAN, myModule.getModuleScope());
    assert userBean != null : "Can't find class " + USER_BEAN;
    final PsiField field = userBean.getFields()[0];
    final Collection<UsageInfo> infoCollection = myFixture.findUsages(field);
    assert infoCollection.size() == 1 : "There should be only one reference";
    final UsageInfo usageInfo = infoCollection.iterator().next();
    final PsiFile psiFile = usageInfo.getFile();
    assert psiFile != null : "Field links to null PsiFile";
    final VirtualFile file = psiFile.getVirtualFile();
    assert file != null : "Cannot resolve PsiFile to VirtualFile";
    assert file.getPath().endsWith(constraintMappingsXml) : "Should resolve into " + constraintMappingsXml;
  }
}
