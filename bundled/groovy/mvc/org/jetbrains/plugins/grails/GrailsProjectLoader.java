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

package org.jetbrains.plugins.grails;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.css.CssSupportLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.addins.GrailsIntegrationUtil;
import org.jetbrains.plugins.grails.fileType.GspFileType;

/**
 * @author ilyas
 */
public class GrailsProjectLoader extends AbstractProjectComponent {

  public GrailsProjectLoader(final Project project) {
    super(project);
    // JavaScript support
    if (GrailsIntegrationUtil.isCssSupportEnabled()) {
      final CssSupportLoader loader = CssSupportLoader.getCssSupportLoader(project);
      if (loader != null) {
        loader.registerCssEnabledFileType(GspFileType.GSP_FILE_TYPE);
      }
    }

  }

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Grails project loader";
  }
}
