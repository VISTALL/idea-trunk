/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts;

import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 */
public class StrutsPathMappingTest extends StrutsTest {

  public void testStrutsConfig() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/struts-config.xml");
  }

  public void testStrutsPage() throws Throwable {
    myFixture.testHighlighting("/index.jsp");
  }

  public void testActionCompletion() throws Throwable {
    myFixture.testCompletion("/actionCompletion.jsp", "/actionCompletion_after.jsp");
  }

  @NonNls
  public String getBasePath() {
    return getPluginTestDataPath() + "/strutsPathMapping/";
  }

  protected String[] getLibraries() {
    return new String[] { "struts.jar", "commons-beanutils.jar" };
  }

}
