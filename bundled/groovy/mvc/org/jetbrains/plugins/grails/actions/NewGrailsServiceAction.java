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

package org.jetbrains.plugins.grails.actions;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NewGrailsServiceAction extends NewGrailsXXXAction {

  public NewGrailsServiceAction() {
    super(GrailsBundle.message("action.Grails.Service.text"),
        GrailsBundle.message("action.Grails.Service.description"),
        GrailsIcons.SERVICE);
  }

  protected String getCommand() {
    return "create-service";
  }

  @Override
  protected VirtualFile getTargetDirectory(@NotNull Module module) {
    return GrailsUtils.findServicesDirectory(module);
  }

  protected void fillGeneratedNamesList(String name, List<String> names) {
    names.add("grails-app/services/" + canonicalize(name) + "Service.groovy");
    names.add(GrailsUtils.GRAILS_INTEGRATION_TESTS + canonicalize(name) + "ServiceTests.groovy");
  }

  @Override
  protected boolean isValidIdentifier(final String inputString, final Project project) {
    return JavaPsiFacade.getInstance(project).getNameHelper().isQualifiedName(inputString);
  }
  
}