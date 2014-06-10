/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.editor.selection;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.groovy.editor.selection.GroovyBasicSelectioner;

import java.util.List;

/**
 * @author ilyas
 */
public class GrailsTagSelectioner extends GroovyBasicSelectioner {
  public boolean canSelect(PsiElement e) {
    PsiElement parent = e.getParent();
    return parent instanceof GspGrailsTag;
  }

  public List<TextRange> select(PsiElement element, CharSequence editorText, int cursorOffset, Editor editor) {
    List<TextRange> result = super.select(element, editorText, cursorOffset, editor);

    PsiElement parent = element.getParent();
    if (!(parent instanceof GspGrailsTag)) return result;
    ASTNode node = element.getNode();
    assert node != null;

    ASTNode parentNode = parent.getNode();
    assert parentNode != null;

    ASTNode prev = node.getTreePrev();
    ASTNode last = node.getTreeNext();
    while (prev != null &&
        prev.getElementType() != GspTokenTypesEx.XML_START_TAG_START &&
        prev.getElementType() != GspTokenTypesEx.XML_END_TAG_START) {
      prev = prev.getTreePrev();
    }
    while (last != null &&
        last.getElementType() != GspTokenTypesEx.XML_TAG_END) {
      last = last.getTreeNext();
    }
    if (prev != null && last != null) {
      result.add(new TextRange(prev.getTextRange().getStartOffset(), last.getTextRange().getEndOffset()));
    }

    return result;
  }
}
