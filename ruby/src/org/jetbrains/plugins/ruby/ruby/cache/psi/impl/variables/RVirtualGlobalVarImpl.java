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

package org.jetbrains.plugins.ruby.ruby.cache.psi.impl.variables;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RubyVirtualElementVisitor;
import org.jetbrains.plugins.ruby.ruby.cache.psi.holders.RVirtualGlobalVarHolder;
import org.jetbrains.plugins.ruby.ruby.cache.psi.impl.RVirtualElementBase;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RVirtualGlobalVar;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Aug 22, 2007
 */
public class RVirtualGlobalVarImpl extends RVirtualElementBase implements RVirtualGlobalVar, Serializable {
    private String myText;
    private RVirtualGlobalVarHolder myHolder;

    public RVirtualGlobalVarImpl(@NotNull final String text, @NotNull final RVirtualGlobalVarHolder holder) {
        myText = text;
        myHolder = holder;
    }

    @NotNull
    public String getText() {
        return myText;
    }

    @NotNull
    public RVirtualGlobalVarHolder getHolder() {
        return myHolder;
    }

    public void accept(@NotNull RubyVirtualElementVisitor visitor) {
        visitor.visitElement(this);
    }

    public String toString() {
        return myText;
    }
}
