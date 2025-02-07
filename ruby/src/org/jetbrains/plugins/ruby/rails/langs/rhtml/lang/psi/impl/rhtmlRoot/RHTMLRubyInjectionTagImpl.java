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

package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.impl.rhtmlRoot;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.xml.XmlTagImpl;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.RHTMLTokenType;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.RHTMLElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 21.05.2007
 */
public class RHTMLRubyInjectionTagImpl extends XmlTagImpl implements RHTMLRubyInjectionTag {
    public RHTMLRubyInjectionTagImpl() {
        super(RHTMLElementType.RHTML_XML_TAG);
    }

    @NotNull
    public String getTagText() {
        final PsiElement[] elements = getElements();
        final StringBuilder buff = new StringBuilder();

        for (final PsiElement element : elements) {
            final ASTNode treeElement = element.getNode();
            if (treeElement != null
                && treeElement.getElementType() == RHTMLTokenType.RUBY_CODE_CHARACTERS) {
                buff.append(treeElement.getText());
            }
        }
        return buff.toString();
    }

    //TODO Make it protected in parent
    private PsiElement[] getElements() {
        final List<PsiElement> elements = new ArrayList<PsiElement>();
        processElements(new PsiElementProcessor() {
          public boolean execute(PsiElement psiElement) {
            elements.add(psiElement);
            return true;
          }
        }, this);
        return elements.toArray(new PsiElement[elements.size()]);
      }

    @NotNull
    public String getName() {
        return getTagText().trim();
    }


    @NonNls
    public String toString() {
        return "Ruby injection:" + getName();
    }

    public XmlTag findParentTag() {
        return null;
    }
}
