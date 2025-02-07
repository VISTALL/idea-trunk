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

package org.jetbrains.plugins.ruby.ruby.lang.namesValidator;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Dec 3, 2007
 */
public class RubyNamesValidator implements NamesValidator {
    public boolean isKeyword(String name, Project project) {
        final IElementType keywords[]= BNF.kRESWORDS.getTypes();
        for (IElementType type : keywords) {
            if (type.toString().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isIdentifier(String name, Project project) {
        return TextUtil.isCID(name) || TextUtil.isFID(name) || TextUtil.isAID(name);
    }
}
