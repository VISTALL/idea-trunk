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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.arg;


import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.*;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 08.06.2006
 */
class RangeExpression {

    /**
     * Range expression parsing
     * @return result of parsing
     * @param builder current builder
     */
    @NotNull
    public static IElementType parse(final RBuilder builder){
        return parseWithLeadBool(builder, builder.mark(), BooleanExpression.parse(builder));
    }

    /**
     * Range expression parsing with lead Bool
     * @return result of parsing
     * @param builder current builder
     * @param marker  Marker before Bool expr
     * @param result result of Bool parsing
     */
    @NotNull
    public static IElementType parseWithLeadBool(final RBuilder builder, final RMarker marker, final IElementType result){
        ParsingMethod parsingMethod = new ParsingMethodWithAssignmentLookup(){
            @NotNull
            public IElementType parseInner(final RBuilder builder){
                return BooleanExpression.parse(builder);
            }
        };

        return BinaryExprParsing.parseWithLeadOperand(builder,
                marker, result,
                parsingMethod,
                ErrorMsg.EXPRESSION_EXPECTED_MESSAGE,
                BNF.tRANGE_TOKENS,
                RubyElementTypes.RANGE_EXPRESSION);

    }

    /**
     * Range expression parsing with lead PRIMARY
     * @return result of parsing
     * @param builder current builder
     * @param marker  Marker before PRIMARY
     * @param result result of PRIMARY parsed 
    */
    public static IElementType parseWithLeadPRIMARY(final RBuilder builder, final RMarker marker, final IElementType result) {
        return parseWithLeadBool(builder, marker.precede(), BooleanExpression.parseWithLeadPRIMARY(builder, marker, result));
    }
}
