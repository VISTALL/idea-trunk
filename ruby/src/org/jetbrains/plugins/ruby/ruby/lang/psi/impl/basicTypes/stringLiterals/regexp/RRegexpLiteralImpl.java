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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.basicTypes.stringLiterals.regexp;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.CoreTypes;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RRegexpLiteral;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.basicTypes.stringLiterals.RStringLiteralBase;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 08.06.2006
 */
public class RRegexpLiteralImpl extends RStringLiteralBase implements RRegexpLiteral {
    public RRegexpLiteralImpl(ASTNode astNode) {
        super(astNode);
    }

    @NotNull
    public RType getType(@Nullable final FileSymbol fileSymbol) {
        return RTypeUtil.createTypeBySymbol(fileSymbol, SymbolUtil.getTopLevelClassByName(fileSymbol, CoreTypes.Regexp), Context.INSTANCE, true);
    }
}
