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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.blocks;

import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RRescueBlock;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.iterators.RBlockVariableNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockVariables;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Dec 26, 2007
 */
public class RRescueBlockNavigator {
    public static RRescueBlock getByParameter(@NotNull final RIdentifier identifier){
        final RBlockVariables blockVariables = RBlockVariableNavigator.getByIdentifier(identifier);
        if (blockVariables == null){
            return null;
        }
        final RRescueBlock rescueBlock = PsiTreeUtil.getParentOfType(blockVariables, RRescueBlock.class);
        if (rescueBlock == null){
            return null;
        }
        return rescueBlock.getBlockVariables() == blockVariables ? rescueBlock : null;
    }
}
