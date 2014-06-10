/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package com.intellij.uml.actions.create;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.uml.utils.UmlBundle;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 */
public class ClassNameValidator implements InputValidator{
  private final Project project;

  public ClassNameValidator(@NotNull Project project) {
    this.project = project;
  }

  public boolean checkInput(final String inputString) {
    return true;
  }

  public boolean canClose(final String fqn) {
    if (! isFQNLike(fqn)) {
      Messages.showErrorDialog(project, UmlBundle.message("name.is.invalid", fqn), UmlBundle.message("error"));
      return false;
    }

    if (JavaPsiFacade.getInstance(project).findClass(fqn, GlobalSearchScope.projectScope(project)) != null) {
      Messages.showErrorDialog(project, UmlBundle.message("class.exists", fqn), UmlBundle.message("error"));
      return false;
    }

    final String className = getClassName(fqn);
    final String packageName = getPackageFQN(fqn);

    final PsiPackage aPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
    if (aPackage == null) {
      Messages.showErrorDialog(project, UmlBundle.message("package.doesnt.exist", fqn), UmlBundle.message("error"));
      return false;
    }

    return true;
  }

  private static boolean isClassName(final String name) {
    if(name == null || name.length() == 0) return false;
    if (! Character.isJavaIdentifierStart(name.charAt(0))) return false;
    for (int i = 1; i < name.length(); i++) {
      if (! Character.isJavaIdentifierPart(name.charAt(i))) return false;
    }
    return true;
  }

  private static String getClassName(final @NotNull String value) {
    String[] tokens = value.split("\\.");
    return tokens[tokens.length - 1];
  }

  private static String getPackageFQN(final @NotNull String fqn) {
    final int dot = fqn.lastIndexOf('.');
    return (dot > 0) ? fqn.substring(0, dot) : "";
  }

  private static boolean isFQNLike(final String value) {
    if (value == null || value.length() == 0
        || value.charAt(0) == '.' || value.charAt(value.length() - 1) == '.') return false;

    for (String clazz : value.split("\\.")) {
      if (! isClassName(clazz)) return false;
    }

    return true;
  }
}
