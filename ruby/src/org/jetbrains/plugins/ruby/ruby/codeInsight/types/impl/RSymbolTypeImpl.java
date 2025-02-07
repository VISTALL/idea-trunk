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

package org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.*;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 13, 2008
 */
public class RSymbolTypeImpl extends RTypeBase implements RSymbolType {
    private DuckType myDuckType;
    private Symbol mySymbol;

    public RSymbolTypeImpl(@Nullable final FileSymbol fileSymbol,
                           @NotNull final Symbol symbol,
                           final Context context,
                           final boolean inReference) {
        // TODO[oleg]: OPTIMIZE! Don`t use ducktype
        mySymbol = symbol;
        myDuckType = RTypeUtil.createDuckTypeBySymbol(fileSymbol, symbol, context, inReference);
    }

    public Symbol getSymbol() {
        return mySymbol;
    }

    @NotNull
    public Collection<Message> getMessages() {
        return myDuckType.getMessages();
    }

    public Collection<Message> getMessagesForName(@Nullable final String name) {
        return myDuckType.getMessagesForName(name);
    }

    public boolean isTyped() {
        // handle nil value correctly
        return !CoreTypes.NilClass.equals(getName());
    }

    @Nullable
    public String getName() {
        return mySymbol.getName();
    }

    public String toString() {
        return "Symbol type: " + mySymbol.getName();
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final RSymbolTypeImpl that = (RSymbolTypeImpl)o;

      if (!mySymbol.equals(that.mySymbol)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = mySymbol.hashCode();
      return result;
    }
}