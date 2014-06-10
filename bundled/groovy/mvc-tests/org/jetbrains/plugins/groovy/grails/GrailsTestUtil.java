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

import org.jetbrains.plugins.groovy.util.TestUtils;
import com.intellij.psi.PsiFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.application.PathManager;
import com.intellij.util.IncorrectOperationException;

/**
 * @author peter
 */
@SuppressWarnings({"AbstractClassNeverImplemented"})
public abstract class GrailsTestUtil {
  public static String getMockGrailsLibraryHome() {
    return FileUtil.toSystemIndependentName(PathManager.getHomePath()) + "/svnPlugins/groovy/mvc-testdata/mockGrailsLib";
  }

  public static PsiFile createPseudoPhysicalGspFile(final Project project, final String text) throws IncorrectOperationException {
    return TestUtils.createPseudoPhysicalFile(project, TestUtils.GSP_TEMP_FILE, text);
  }
}
