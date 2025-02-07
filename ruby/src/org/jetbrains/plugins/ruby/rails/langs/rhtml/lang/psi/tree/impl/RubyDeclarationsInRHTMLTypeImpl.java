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

package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.tree.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.peer.PeerFactory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.lexer.RHTMLRubyLexer;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.parser.rubyInjections.RHTMLRubyParser;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 07.04.2007
 */
public class RubyDeclarationsInRHTMLTypeImpl extends IFileElementType {//extends TemplateWithOuterFragmentsTypeImpl {
    public RubyDeclarationsInRHTMLTypeImpl(final String debugName) {
        super(debugName, RubyLanguage.RUBY);
    }

    public ASTNode parseContents(final ASTNode chameleon) {
        final PeerFactory factory = PeerFactory.getInstance();

        final Lexer lexer = new RHTMLRubyLexer();

        final Project project = chameleon.getTreeParent().getPsi().getProject();
        final PsiBuilder builder = factory.createBuilder(chameleon, lexer, getLanguage(), ((LeafElement)chameleon).getInternedText(), project);

        return new RHTMLRubyParser().parse(chameleon.getElementType(), builder);
    }
}
