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

package org.jetbrains.plugins.grails.addins.js;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.psi.css.CssStylesheet;

/**
 * @author peter
 */
public class CssIntegrationUtil {

  public static boolean isCssStylesheet(ASTNode child) {
    return child.getPsi() instanceof CssStylesheet;
  }

  public static boolean isCssLanguage(Language childLanguage) {
    return childLanguage instanceof CSSLanguage;
  }
}