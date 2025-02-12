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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.Collection;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: May 4, 2007
 */
public interface Scope {
    @NotNull
    public PseudoScopeHolder getHolder();

    @NotNull
    public Collection<ScopeVariable> getVariables();

    void processIdentifier(@NotNull final RIdentifier identifier);

    @Nullable
    public ScopeVariable getVariableByName(@NotNull final String name);

    @NotNull
    public Set<String> getScopeNames();

    @NotNull
    public Collection<Scope> getSubScopes(); 
}
