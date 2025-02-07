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

package org.jetbrains.plugins.ruby.ruby.lang.formatter.models.spacing;

import com.intellij.psi.tree.TokenSet;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 07.08.2006
 */
public interface SpacingTokens extends RubyTokenTypes {
    /**
     * Always no spacing before these tokens
     */
    final TokenSet NO_SPACING_BEFORE = TokenSet.create(
            tSEMICOLON,
            tCOMMA,
            tCOLON2,
            tDOT
    );

    /**
     * Always at least single space after these tokens
     */
    final TokenSet SPACING_AFTER = TokenSet.create(
            tCOMMA
    );
}
