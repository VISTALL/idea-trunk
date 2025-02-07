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

package org.jetbrains.plugins.ruby.ruby.lang.surround.surrounders;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RCondition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RWhileStatement;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 5, 2007
 */
public class RubyWhileSurrounder extends RubySurrounderBase{

    protected TextRange getTextRange(@NotNull final RPsiElement element) {
        assert element instanceof RWhileStatement;
        final RCondition condition = ((RWhileStatement) element).getCondition();
        return condition!=null ? condition.getTextRange() : null;
    }

    protected String getText(PsiElement[] elements) {
        return "while condition do\n" + gatherText(elements) + "\nend";
    }

    public String getTemplateDescription() {
        return "while ... do ... end";
    }
}
