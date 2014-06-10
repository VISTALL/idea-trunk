package org.jetbrains.plugins.grails.lang.gsp.util;/*
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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.impl.GspHtmlFileImpl;
import org.jetbrains.plugins.grails.lang.gsp.GspFileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlToken;
import com.intellij.psi.xml.XmlTag;
import com.intellij.lang.ASTNode;

/**
 * @author ilyas
 */
public class GspUtil {
  @Nullable
  public static GspGrailsTag getContainingGrailsTag(PsiElement e) {
    GspGrailsTag tag = null;
    if (e == null) return null;
    if (e.getTextOffset() >= 0) {
      FileViewProvider provider = e.getContainingFile().getViewProvider();
      PsiFile file = provider.getPsi(provider.getBaseLanguage());
      if (file instanceof GspFile) {
        int offset = e.getTextRange().getStartOffset();
        ASTNode node = file.getNode().findLeafElementAt(offset);
        if (node != null) {
          PsiElement start = node.getPsi();
          while (start != null && !(start instanceof GspGrailsTag)) {
            start = start.getParent();
          }
          if (start != null) tag = ((GspGrailsTag) start);
        }
      }
    }
    return tag;
  }

  @Nullable
  public static XmlTag getContainigHtmlTag(PsiElement e) {
    XmlTag tag = null;
    if (e == null) return null;
    if (e.getTextOffset() >= 0) {
      FileViewProvider provider = e.getContainingFile().getViewProvider();
      if (provider instanceof GspFileViewProvider) {
      PsiFile file = provider.getPsi(((GspFileViewProvider) provider).getTemplateDataLanguage());
        if (file instanceof GspHtmlFileImpl) {
          int offset = e.getTextRange().getStartOffset();
          ASTNode node = file.getNode().findLeafElementAt(offset);
          if (node != null) {
            PsiElement start = node.getPsi();
            while (start != null && !(start instanceof XmlTag)) {
              start = start.getParent();
            }
            if (start != null) tag = ((XmlTag) start);
          }
        }
      }
    }
    if (tag != null && tag.getTextRange().contains(e.getTextRange())) return tag;
    return null;
  }
}
