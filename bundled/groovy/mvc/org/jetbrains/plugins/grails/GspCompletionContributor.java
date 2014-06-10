/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jetbrains.plugins.grails;

import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.lang.html.HTMLLanguage;
import static com.intellij.patterns.XmlPatterns.*;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.fileType.GspFileType;
import org.jetbrains.plugins.grails.lang.gsp.GspLanguage;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;

/**
 * @author Maxim.Medvedev
 * Date: Apr 10, 2009 2:32:04 PM
 */
public class GspCompletionContributor extends CompletionContributor {

  public GspCompletionContributor() {
    extend(CompletionType.BASIC,
           psiElement(XmlTokenType.XML_NAME).withText(not(string().contains(":"))).withParent(
             xmlTag().inFile(
               psiFile().withOriginalFile(
                 psiFile().withLanguage(HTMLLanguage.INSTANCE).withVirtualFile(
                   virtualFile().ofType(GspFileType.GSP_FILE_TYPE))))),
           new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        final PsiFile psi = parameters.getPosition().getContainingFile().getViewProvider().getPsi(GspLanguage.INSTANCE);
        if (psi instanceof GspFile) {
          final GspFile gspFile = (GspFile)psi;
          for (LookupElement element : TagNameReference.getTagNameVariants(gspFile.getRootTag())) {
            result.addElement(TailTypeDecorator.withTail(element, TailType.SPACE));
          }
        }
      }
    });

    extend(CompletionType.BASIC, psiElement().afterLeaf("<%@", "@{").inside(GspDirective.class), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        for (String directiveName : CollectionFactory.ar("page", "taglib")) {
          result.addElement(TailTypeDecorator.withTail(LookupElementBuilder.create(directiveName), TailType.SPACE));
        }
      }
    });
  }

}