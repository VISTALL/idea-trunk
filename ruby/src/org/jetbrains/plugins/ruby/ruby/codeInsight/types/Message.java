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

package org.jetbrains.plugins.ruby.ruby.codeInsight.types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: May 23, 2007
 */
public interface Message {
    @NotNull
    public String getName();

    public int getArgumentsNumber();

    /**
     * @param patternMessage Patern message
     * @return true, if this can be matched by patterMessage
     */
    public boolean matchesMessage(@NotNull final Message patternMessage);

    @Nullable
    public Symbol getSymbol();

    public boolean isImportant();
}
