/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

/*
 * User: anna
 * Date: 11-Nov-2008
 */
package org.jetbrains.idea.eclipse.importWizard;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.JDOMUtil;
import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.eclipse.EclipseXml;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EclipseProjectFinder implements EclipseXml {
  public static void findModuleRoots(final List<String> paths, final String rootPath) {
    final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
    if (progressIndicator != null) {
      if (progressIndicator.isCanceled()) return;
      progressIndicator.setText2(rootPath);
    }

    final String project = findProjectName(rootPath);
    if (project != null) {
      paths.add(rootPath);
    }
    else {
      final File root = new File(rootPath);
      if (root.isDirectory()) {
        final File[] files = root.listFiles();
        if (files != null) {
          for (File file : files) {
            findModuleRoots(paths, file.getPath());
          }
        }
      }
    }
  }

  @Nullable
  public static String findProjectName(String rootPath) {
    String name = null;
    final File file = new File(rootPath, DOT_PROJECT_EXT);
    if (file.isFile()) {
      try {
        name = JDOMUtil.loadDocument(file).getRootElement().getChildText(NAME_TAG);
      }
      catch (JDOMException e) {
        return null;
      }
      catch (IOException e) {
        return null;
      }
    }
    return name;
  }
}