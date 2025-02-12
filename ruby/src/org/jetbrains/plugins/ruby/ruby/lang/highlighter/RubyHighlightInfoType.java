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

package org.jetbrains.plugins.ruby.ruby.lang.highlighter;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 27.10.2006
 */
public class RubyHighlightInfoType implements HighlightInfoType {
    public static final RubyHighlightInfoType RUBY_REQUIRE_OR_LOAD_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.REQUIRE_OR_LOAD_CALL_ID, RubyHighlighter.REQUIRE_OR_LOAD_CALL);
    public static final RubyHighlightInfoType RUBY_REQUIRE_OR_LOAD_CALL_ARG_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.REQUIRE_OR_LOAD_CALL_ARG_ID, RubyHighlighter.REQUIRE_OR_LOAD_CALL_ARG);

    public static final RubyHighlightInfoType RUBY_INCLUDE_OR_EXTEND_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.INCLUDE_OR_EXTEND_CALL_ID, RubyHighlighter.INCLUDE_OR_EXTEND_CALL);

    public static final RubyHighlightInfoType RUBY_PRIVATE_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.PRIVATE_CALL_ID, RubyHighlighter.PRIVATE_CALL);
    public static final RubyHighlightInfoType RUBY_PROTECTED_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.PROTECTED_CALL_ID, RubyHighlighter.PROTECTED_CALL);
    public static final RubyHighlightInfoType RUBY_PUBLIC_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.PUBLIC_CALL_ID, RubyHighlighter.PUBLIC_CALL);

    public static final RubyHighlightInfoType RUBY_ATTR_READER_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.ATTR_READER_CALL_ID, RubyHighlighter.ATTR_READER);
    public static final RubyHighlightInfoType RUBY_ATTR_WRITER_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.ATTR_WRITER_CALL_ID, RubyHighlighter.ATTR_WRITER);
    public static final RubyHighlightInfoType RUBY_ATTR_ACCESSOR_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.ATTR_ACCESSOR_CALL_ID, RubyHighlighter.ATTR_ACCESSOR);
    public static final RubyHighlightInfoType RUBY_RAILS_ATTR_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.RAILS_ATTR_CALL_ID, RubyHighlighter.RAILS_ATTR);

    public static final RubyHighlightInfoType RUBY_CONSTANT_DEF_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.CONSTANT_DEF_ID, RubyHighlighter.CONSTANT_DEF);
    public static final RubyHighlightInfoType RUBY_SYMBOL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.SYMBOL_ID, RubyHighlighter.SYMBOL);
    public static final RubyHighlightInfoType RUBY_LOCAL_VARIABLE_HIGHTLIGHT = new RubyHighlightInfoType(RubyHighlighter.LOCAL_VARIABLE_ID, RubyHighlighter.LOCAL_VARIABLE);

    public static final RubyHighlightInfoType JRUBY_INCLUDE_JAVA_HIGHTLIGHT = new RubyHighlightInfoType(RubyHighlighter.INCLUDE_JAVA_CALL_ID, RubyHighlighter.INCLUDE_JAVA_CALL);

    public static final RubyHighlightInfoType RUBY_REQUIRE_GEM_CALL_HIGHLIGHT = new RubyHighlightInfoType(RubyHighlighter.REQUIRE_GEM_CALL_ID, RubyHighlighter.REQUIRE_GEM_CALL);

    private String myText;
    private TextAttributesKey myTextAttributesKey;
    private static final int SEVERITY = -1;

    private RubyHighlightInfoType(final String text, final TextAttributesKey textAttributesKey){
        myText = text;
        myTextAttributesKey = textAttributesKey;
    }

    public HighlightSeverity getSeverity(PsiElement element) {
        return new HighlightSeverity(myText, SEVERITY);
    }

    public TextAttributesKey getAttributesKey() {
        return myTextAttributesKey;
    }
}
