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

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.util.GrailsUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NewGrailsControllerAction extends NewGrailsXXXAction {

  public NewGrailsControllerAction() {
    super(GrailsBundle.message("action.Grails.Controller.text"),
        GrailsBundle.message("action.Grails.Controller.description"),
        GrailsIcons.CONTROLLER);
  }

  protected String getCommand() {
    return "create-controller";
  }

  @Override
  protected VirtualFile getTargetDirectory(@NotNull Module module) {
    return GrailsUtils.findControllersDirectory(module);
  }

  protected void doAction(Module module, Project project, String name) {
    name = StringUtil.trimEnd(name, GrailsUtils.CONTROLLER_SUFFIX);
    super.doAction(module, project, name);
  }

  protected void fillGeneratedNamesList(String name, List<String> names) {
    name = StringUtil.trimEnd(name, GrailsUtils.CONTROLLER_SUFFIX);
    names.add("grails-app/controllers/" + canonicalize(name) + "Controller.groovy");
    names.add(GrailsUtils.GRAILS_INTEGRATION_TESTS + canonicalize(name) + "ControllerTests.groovy");
  }

  @Override
  protected boolean isValidIdentifier(final String inputString, final Project project) {
    return JavaPsiFacade.getInstance(project).getNameHelper().isQualifiedName(inputString);
  }
}