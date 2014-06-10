/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

import org.jetbrains.plugins.groovy.editor.selection.GroovyBasicSelectioner;
import org.jetbrains.plugins.grails.lang.gsp.util.GspUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.editor.Editor;

import java.util.List;
import java.util.ArrayList;

/**
 * @author ilyas
 */
public class GrailsHtmlTagBodySelectioner extends GroovyBasicSelectioner {
  public boolean canSelect(PsiElement e) {
    if (e == null || e.getNode() == null) return false;
    return (e instanceof GspGrailsTag ||
        e.getNode().getElementType() == XmlTokenType.XML_DATA_CHARACTERS) &&
        GspUtil.getContainigHtmlTag(e) != null;
  }

  public List<TextRange> select(PsiElement e, CharSequence editorText, int cursorOffset, Editor editor) {
    List<TextRange> result = new ArrayList<TextRange>();
    XmlTag tag = GspUtil.getContainigHtmlTag(e);
    if (tag != null) {
      result.add(tag.getTextRange());
      PsiElement parent = tag.getParent();
      while (!(parent instanceof PsiFile)) {
        if (parent instanceof XmlTag) {
          result.add(parent.getTextRange());
        }
        parent = parent.getParent();
      }
    }
    return result;
  }
}