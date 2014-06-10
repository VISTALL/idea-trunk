/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;

/**
 * @author Dmitry Avdeev
 */
public class StrutsMultiTest  extends StrutsTest {

  public void testWebXml() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/web.xml");
  }

  public void testStrutsConfig() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/struts-config.xml");
  }

  public void testSecondConfig() throws Throwable {
    myFixture.testHighlighting("/WEB-INF/struts-config-second.xml");
  }

  public void testStrutsPage() throws Throwable {
    myFixture.testHighlighting("/index.jsp");
  }

  public void testSecondPage() throws Throwable {
    myFixture.testHighlighting("/second/index.jsp");
  }

  protected void configure(WebModuleFixtureBuilder moduleBuilder) {
    super.configure(moduleBuilder);
    moduleBuilder.addSourceRoot("src");
  }

  @NonNls
  public String getBasePath() {
    return getPluginTestDataPath() + "/multiModules/";
  }
}
