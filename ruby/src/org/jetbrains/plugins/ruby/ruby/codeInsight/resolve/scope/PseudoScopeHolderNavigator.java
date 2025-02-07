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

package org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RClassObject;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Jan 14, 2008
 */
public class PseudoScopeHolderNavigator {
    /**
     * @param element context
     * @return Returns the root scope for the given context
     */
    @Nullable
    public static PseudoScopeHolder getScopeHolder(@NotNull PsiElement element) {
        if (element.getParent() instanceof RClassObject){
            //noinspection ConstantConditions
            element = PsiTreeUtil.getParentOfType(element, RContainer.class).getParent();
        }
        return element instanceof PseudoScopeHolder ?
                (PseudoScopeHolder) element : PsiTreeUtil.getParentOfType(element, PseudoScopeHolder.class);
    }
}
