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

package com.intellij.webBeans.rename;

public class WebBeansNamedBeanRenameTest extends AbstractWebBeansRenameTestCase {

  public void testRenameNamedBeans() throws Throwable {
    myFixture.configureByFiles("LoginBean.java", "User.java");

    myFixture.testRename("page_2.jsp", "page_2_after.jsp", "renamed_loginBean", "LoginBean.java", "User.java");

    myFixture.checkResultByFile("LoginBean.java", "LoginBean_after2.java", true);
  }

  public void testRenameProducesMethodsNamedBeans() throws Throwable {
    myFixture.configureByFiles("LoginBean.java", "User.java");

    myFixture.testRename("page_1.jsp", "page_1_after.jsp", "currentUser_new", "LoginBean.java", "User.java");

    myFixture.checkResultByFile("LoginBean.java", "LoginBean_after.java", true);
  }


  @Override
  public String getBasePath() {
    return super.getBasePath() + "WebBeansNamedBeanRename/";
  }
}
