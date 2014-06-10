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
package org.jetbrains.plugins.groovy.grails;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.plugins.groovy.lang.GroovyLiveTemplatesTest;

/**
 * @author peter
 */
public class GspLiveTemplatesTest extends LightCodeInsightFixtureTestCase{

  public void testHtmlTemplatesWorkInGsp() throws Throwable {
    myFixture.configureByText("a.gsp", "t<caret>");
    GroovyLiveTemplatesTest.expandTemplate(myFixture.getEditor());
    myFixture.checkResult("<<caret>></>");
  }

}