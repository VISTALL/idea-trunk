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

/*
 * User: anna
 * Date: 24-Feb-2009
 */
package com.intellij.j2meplugin.i18n;

import com.intellij.ide.fileTemplates.DefaultCreateFromTemplateHandler;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.j2meplugin.module.J2MEModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;

public class CreateMobileResourcePackHandler extends DefaultCreateFromTemplateHandler {
  public boolean handlesTemplate(FileTemplate template) {
    return StringUtil.startsWithIgnoreCase(template.getName(), "Mobile");
  }

  public boolean canCreate(PsiDirectory[] dirs) {
    for (PsiDirectory dir : dirs) {
      final Module module = ModuleUtil.findModuleForPsiElement(dir);
      if (module != null && module.getModuleType() == J2MEModuleType.getInstance()) {
        return true;
      }
    }
    return false;
  }
}