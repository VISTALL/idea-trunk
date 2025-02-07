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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.methods;

import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgumentList;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Feb 4, 2008
 */
public class RArgumentListNavigator {
    @Nullable
    public static RArgumentList getByArgument(@NotNull final RArgument argument){
        final RArgumentList argList = PsiTreeUtil.getParentOfType(argument, RArgumentList.class);
        if (argList == null){
            return null;
        }
        final int num = argList.getArgNumber(argument);
        return num!= -1 ? argList : null;
    }
}
