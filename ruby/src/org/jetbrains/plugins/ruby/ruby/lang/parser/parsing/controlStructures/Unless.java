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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures;


import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.COMPSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 10.06.2006
 */
public class Unless implements RubyTokenTypes {
/*
        | kUNLESS expr_value then
          compstmt
          opt_else
          kEND
*/
    @NotNull
    public static IElementType parse(final RBuilder builder){
        RMarker statementMarker = builder.mark();

        builder.match(kUNLESS);

        Condition.parse(builder);

        THEN.parse(builder);

        COMPSTMT.parse(builder, kELSE, kEND);

        builder.passEOLs();
        OPT_ELSE.parse(builder);

        builder.matchIgnoreEOL(kEND);
        statementMarker.done(RubyElementTypes.UNLESS_STATEMENT);
        return RubyElementTypes.UNLESS_STATEMENT;

    }
}
