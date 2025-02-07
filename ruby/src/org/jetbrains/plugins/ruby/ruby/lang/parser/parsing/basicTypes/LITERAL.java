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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes;


import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 27.06.2006
 */
public class LITERAL implements RubyTokenTypes {

/*
literal		: numeric
		| symbol
		| dsym

		;
*/

    @NotNull
    public static IElementType parse(final RBuilder builder) {
        if (!builder.compare(BNF.tLITERAL_FIRST_TOKEN)){
            return RubyElementTypes.EMPTY_INPUT;
        }
// numerical literal parsing
        if (builder.compare(tINTEGER)) {
            builder.parseSingleToken(tINTEGER, RubyElementTypes.INTEGER);
            return RubyElementTypes.INTEGER;
        }

        if (builder.compare(tFLOAT)) {
            builder.parseSingleToken(tFLOAT, RubyElementTypes.FLOAT);
            return RubyElementTypes.FLOAT;
        }

// :expr = value of variable, constant or operator
        if (builder.compare(tSYMBEG)) {
            return SYMBOL.parse(builder);
        }

        return RubyElementTypes.EMPTY_INPUT;
    }

}
