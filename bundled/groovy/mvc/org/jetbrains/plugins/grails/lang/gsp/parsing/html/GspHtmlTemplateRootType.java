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

package org.jetbrains.plugins.grails.lang.gsp.parsing.html;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.StdLanguages;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerUtil;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.impl.source.tree.*;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.CharTable;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.GspFileViewProvider;
import org.jetbrains.plugins.grails.lang.gsp.lexer.GspTokenTypesEx;
import org.jetbrains.plugins.grails.lang.gsp.parsing.gsp.lexer.GspLexer;
import org.jetbrains.plugins.grails.lang.gsp.parsing.html.lexer.GspTemplateBlackAndWhiteLexer;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.impl.GspHtmlOuterElementImpl;

import javax.swing.*;

/**
 * @author ilyas
 */
public class GspHtmlTemplateRootType extends IFileElementType {

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.lang.gsp.parsing.html.GspHtmlTemplateRootType");

  public GspHtmlTemplateRootType(@NonNls String debugName) {
    super(debugName, StdLanguages.HTML);
  }

  public ASTNode parseContents(ASTNode chameleon) {
    final CharTable table = SharedImplUtil.findCharTableByTree(chameleon);
    final FileElement treeElement = new DummyHolder(((TreeElement) chameleon).getManager(), null, table).getTreeElement();

    final PsiFile file = (PsiFile) TreeUtil.getFileElement((TreeElement) chameleon).getPsi();
    PsiFile originalFile = file.getOriginalFile();

    GspFileViewProvider viewProvider = (GspFileViewProvider) originalFile.getViewProvider();
    final Language language = viewProvider.getTemplateDataLanguage();
    final CharSequence chars = chameleon.getChars();

    // Create template file without any GSP occurrences
    final GspLexer gspLexer = new GspLexer();
    final StringBuffer templateText = createTemplateText(chars, gspLexer);
    final PsiFile templateFile = createFromText(language, templateText, file.getManager());

    final TreeElement parsed = ((PsiFileImpl) templateFile).calcTreeElement();

    final Lexer lexer = new MergingLexerAdapter(
        new GspTemplateBlackAndWhiteLexer(
            LanguageParserDefinitions.INSTANCE.forLanguage(language).createLexer(file.getProject())), TokenSet.EMPTY);
    lexer.start(chars);

    insertOuters(parsed, lexer, table);

    if (parsed != null) treeElement.rawAddChildren(parsed.getFirstChildNode());
    treeElement.clearCaches();
    treeElement.subtreeChanged();
    return treeElement.getFirstChildNode();
  }

  private StringBuffer createTemplateText(CharSequence buf, Lexer lexer) {
    StringBuffer result = new StringBuffer(buf.length());
    lexer.start(buf);

    while (lexer.getTokenType() != null) {
      if (lexer.getTokenType() == GspTokenTypesEx.GSP_TEMPLATE_DATA ||
          lexer.getTokenType() == XmlTokenType.XML_WHITE_SPACE) {
        result.append(buf, lexer.getTokenStart(), lexer.getTokenEnd());
      }
      lexer.advance();
    }

    return result;
  }


  private PsiFile createFromText(final Language language, CharSequence text, PsiManager manager) {
    @NonNls
    final LightVirtualFile virtualFile = new LightVirtualFile("foo", new LanguageFileType(language) {

      @NotNull
      @NonNls
      public String getDefaultExtension() {
        return "";
      }

      @NotNull
      @NonNls
      public String getDescription() {
        return "fake for language" + language.getID();
      }

      @Nullable
      public Icon getIcon() {
        return null;
      }

      @NotNull
      @NonNls
      public String getName() {
        return language.getID();
      }
    }, text, LocalTimeCounter.currentTime());

    FileViewProvider viewProvider = new SingleRootFileViewProvider(manager, virtualFile, false) {
      @NotNull
      public Language getBaseLanguage() {
        return language;
      }
    };

    return viewProvider.getPsi(language);
  }

  private void insertOuters(TreeElement root, Lexer lexer, final CharTable table) {
    GspHtmlTreePatcher patcher = new GspHtmlTreePatcher(root.getManager());

    int treeOffset = 0;
    LeafElement leaf = TreeUtil.findFirstLeaf(root);
    while (lexer.getTokenType() != null) {
      IElementType tt = lexer.getTokenType();
      if (tt == GspTokenTypesEx.GSP_FRAGMENT_IN_HTML) {
        while (leaf != null && treeOffset < lexer.getTokenStart()) {
          treeOffset += leaf.getTextLength();
          if (treeOffset > lexer.getTokenStart()) {
            leaf = patcher.split(leaf, leaf.getTextLength() - (treeOffset - lexer.getTokenStart()), table);
            treeOffset = lexer.getTokenStart();
          }
          leaf = (LeafElement)TreeUtil.nextLeaf(leaf);
        }

        if (leaf == null) break;

        final GspHtmlOuterElementImpl element = createOuterElement(lexer, table);
        patcher.insert(leaf.getTreeParent(), leaf, element);
        leaf.getTreeParent().subtreeChanged();
        leaf = element;
      }
      lexer.advance();
    }

    if (lexer.getTokenType() != null) {
      assert lexer.getTokenType() == GspTokenTypesEx.GSP_FRAGMENT_IN_HTML;
      GspHtmlOuterElementImpl outerElement = createOuterElement(lexer, table);
      ((CompositeElement) root).rawAddChildren(outerElement);
      ((CompositeElement) root).subtreeChanged();
    }
  }


  protected GspHtmlOuterElementImpl createOuterElement(final Lexer lexer,
                                                       final CharTable table) {
    return new GspHtmlOuterElementImpl(GspTokenTypesEx.GSP_FRAGMENT_IN_HTML, LexerUtil.internToken(lexer, table));
  }

}


class GspHtmlTreePatcher {
  public PsiManager myManager;

  public GspHtmlTreePatcher(@NotNull PsiManager manager) {
    myManager = manager;
  }

  public void insert(CompositeElement parent, TreeElement anchorBefore, OuterLanguageElement toInsert) {
    if (anchorBefore != null) {
      TreeElement prev = anchorBefore.getTreePrev();
      if (parent.getPsi() instanceof XmlTag &&
          anchorBefore.getElementType() == XmlTokenType.XML_START_TAG_START) {
        //XmlElementFactory.createXmlTextFromText(toInsert.getManager(), "");
        parent.rawInsertBeforeMe((TreeElement) toInsert);
      } else if (XmlElementType.XML_TEXT != anchorBefore.getElementType() &&
          prev != null &&
          prev.getChildren(null).length > 0 &&
          XmlElementType.XML_TEXT == prev.getElementType()) {
        prev.getLastChildNode().rawInsertAfterMe((TreeElement) toInsert);
      } else {
        anchorBefore.rawInsertBeforeMe((TreeElement) toInsert);
      }
    }
  }

  public LeafElement split(LeafElement leaf, int offset, CharTable table) {
    final CharSequence chars = leaf.getChars();
    final LeafElement leftPart = Factory.createSingleLeafElement(leaf.getElementType(), chars, 0, offset, table, myManager);
    final LeafElement rightPart = Factory.createSingleLeafElement(leaf.getElementType(), chars, offset, chars.length(), table, myManager);
    leaf.rawInsertAfterMe(leftPart);
    leftPart.rawInsertAfterMe(rightPart);
    leaf.rawRemove();
    return leftPart;
  }
}
