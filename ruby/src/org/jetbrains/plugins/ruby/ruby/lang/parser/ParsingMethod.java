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

package org.jetbrains.plugins.ruby.ruby.lang.parser;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 26.07.2006
 */
public interface ParsingMethod {
    /**
     * Parses some expression
     * @param builder Builder used for parsing
     * @return The type of parsed expression
     */

    public IElementType parse(final RBuilder builder);
}
