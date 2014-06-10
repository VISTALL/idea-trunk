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

package org.jetbrains.plugins.groovy.grails.parser;

import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import org.jetbrains.plugins.groovy.util.TestUtils;

import java.util.List;
import java.io.IOException;

/**
 * @author peter
 */
public abstract class GspParsingTestCase extends LightCodeInsightFixtureTestCase {

  protected void doTest(Language lang) throws IOException {
    final List<String> data = TestUtils.readInput(getTestDataPath() + getTestName(true).replace('$', '/') + ".test");

    final PsiFile file = PsiFileFactory.getInstance(getProject()).createFileFromText("temp.gsp", data.get(0));
    final PsiFile psi = file.getViewProvider().getPsi(lang);
    assertEquals(data.get(1).trim(), DebugUtil.psiToString(psi, false).trim());
  }

}
