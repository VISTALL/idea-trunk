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

package com.intellij.struts.facet;

import com.intellij.javaee.web.WebUtil;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiFile;
import com.intellij.struts.StrutsTest;
import com.intellij.testFramework.builders.WebModuleFixtureBuilder;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * @author Dmitry Avdeev
 */
public class AddStrutsSupportTest extends StrutsTest {

  public void testStrutsSupport() throws Throwable {
    doTest(false, false, "/WEB-INF/struts-config_after.xml");
  }

  public void testStrutsSupportWithTiles() throws Throwable {
    doTest(false, true, "/WEB-INF/struts-with-tiles_after.xml");
  }

  public void testStrutsSupportWithValidator() throws Throwable {
    doTest(true, false, "/WEB-INF/struts-with-validator_after.xml");
  }

  public void testStrutsSupportFull() throws Throwable {
    doTest(true, true, "/WEB-INF/struts-full_after.xml");
  }

  private void doTest(final boolean hasValidatorSupport, final boolean hasTilesSupport, final String expectedFile) throws Throwable {
    myFixture.configureByFiles("/WEB-INF/web.xml");
    final PsiFile file = myFixture.getFile();
    new WriteCommandAction.Simple(myFixture.getProject()) {
      protected void run() throws Throwable {
        final WebFacet webFacet = WebUtil.getWebFacet(file);
        assertTrue(AddStrutsSupportUtil.addSupport(webFacet, hasValidatorSupport, hasTilesSupport, true));
      }
    }.execute();
    myFixture.checkResultByFile("/WEB-INF/web_after.xml");
    myFixture.checkResultByFile("/WEB-INF/struts-config.xml", expectedFile, false);
  }


  protected void configure(WebModuleFixtureBuilder moduleBuilder) {
    moduleBuilder.setWebXml(myFixture.getTempDirPath() + "/WEB-INF/web.xml");
    moduleBuilder.addWebRoot(myFixture.getTempDirPath(), "/");
    addStrutsJar(moduleBuilder);
    new File(myFixture.getTempDirPath() + "/src/").mkdir();
    moduleBuilder.addSourceRoot("src");
  }

  @NonNls
  protected String getBasePath() {
    return getPluginTestDataPath() + "/addSupport/";
  }
}
