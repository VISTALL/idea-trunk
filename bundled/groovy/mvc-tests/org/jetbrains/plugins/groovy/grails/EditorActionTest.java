/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.grails;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.groovy.util.TestUtils;

import java.util.List;

/**
 * @author ilyas
 */
public class EditorActionTest extends LightCodeInsightFixtureTestCase {

  private void doTest() throws Throwable {
    final List<String> data = TestUtils.readInput(getTestDataPath() + getTestName(true) + ".test");

    myFixture.configureByText(GspFileType.GSP_FILE_TYPE, "");

    final String fileText = data.get(0);

    for (int i = 0; i < fileText.length(); i++) {
      final char charTyped = fileText.charAt(i);
      myFixture.type(charTyped);
    }
    myFixture.checkResult(data.get(1));
  }

  public void testDir() throws Throwable { doTest(); }
  public void testSimple_type() throws Throwable { doTest(); }

  @Override
  protected String getBasePath() {
    return "/svnPlugins/groovy/mvc-testdata/grails/enterAction/";
  }
}
