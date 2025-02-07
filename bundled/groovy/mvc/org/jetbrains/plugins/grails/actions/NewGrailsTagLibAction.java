/*
 * Copyright 2000-2008 JetBrains s.r.o.
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.GrailsBundle;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.util.GrailsUtils;

import java.util.List;

public class NewGrailsTagLibAction extends NewGrailsXXXAction {

  public NewGrailsTagLibAction() {
    super(GrailsBundle.message("action.Grails.TagLib.text"),
            GrailsBundle.message("action.Grails.TagLib.description"),
            GrailsIcons.TAG_LIB);
  }

  @NonNls
  protected String getCommand() {
    return "create-tag-lib";
  }

  @Override
  protected VirtualFile getTargetDirectory(@NotNull Module module) {
    return GrailsUtils.findTagLibDirectory(module);
  }

  protected void doAction(Module module, Project project, String name) {
    name = StringUtil.trimEnd(name, GrailsUtils.TAGLIB_SUFFIX);
    super.doAction(module, project, name);
  }


  protected void fillGeneratedNamesList(String name, List<String> names) {
    name = StringUtil.trimEnd(name, GrailsUtils.TAGLIB_SUFFIX);
    names.add("grails-app/taglib/" + canonicalize(name) + "TagLib.groovy");
    names.add(GrailsUtils.GRAILS_INTEGRATION_TESTS + canonicalize(name) + "TagLibTests.groovy");
  }
}
