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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RVirtualGlobalVar;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.GlobalVarDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Aug 22, 2007
 */
public class GlobalVarDefinitionImpl implements GlobalVarDefinition {
    private RGlobalVariable myFirstDefinition;

    public GlobalVarDefinitionImpl(@NotNull final RGlobalVariable globalVariable){
        myFirstDefinition  = globalVariable;
    }

    @NotNull
    public RGlobalVariable getFirstDefinition() {
        return myFirstDefinition;
    }

    public void process(@NotNull final RGlobalVariable globalVariable) {
        // do nothing
    }

    @NotNull
    public String getText() {
        return myFirstDefinition.getText();
    }

    public boolean isFor(@NotNull final RVirtualGlobalVar virtualGlobalVar) {
        return getText().equals(virtualGlobalVar.getText());
    }
}
