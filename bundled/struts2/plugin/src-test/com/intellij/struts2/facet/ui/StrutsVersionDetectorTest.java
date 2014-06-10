/*
 * Copyright 2008 The authors
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
 *
 */

package com.intellij.struts2.facet.ui;

import com.intellij.struts2.BasicHighlightingTestCase;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Tests for {@link StrutsVersionDetector}.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsVersionDetectorTest extends BasicHighlightingTestCase<JavaModuleFixtureBuilder> {

  @NotNull
  protected String getTestDataLocation() {
    return "strutsXmlResult"; // fake
  }

  public void testDetectStrutsVersion() throws Exception {
    final String version = StrutsVersionDetector.detectStrutsVersion(myModule);
    assertEquals("2.1.6", version);
  }

}