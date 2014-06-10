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

package org.jetbrains.plugins.grails.lang.gsp.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.StdLanguages;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.addins.js.JavaScriptIntegrationUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspOuterGroovyElement;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspDeclarationTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspScriptletTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author ilyas
 */
public class GspFormattingHelper {
  private final XmlFormattingPolicy myXmlFormattingPolicy;
  private final ASTNode myNode;
  private TextRange myTextRange;
  private Collection<PsiElement> myChildrenToSkip = new ArrayList<PsiElement>();
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.grails.lang.gsp.formatter.GspFormattingHelper");

  public GspFormattingHelper(XmlFormattingPolicy policy, ASTNode node, TextRange textRange) {
    myNode = node;
    myXmlFormattingPolicy = policy;
    myTextRange = textRange;
  }

  public ASTNode processNonGspChild(final ASTNode child,
                                    final Indent indent,
                                    final List<Block> result,
                                    final Wrap wrap,
                                    final Alignment alignment) {

//     We ignore fragmented injections
//    if (myChildrenToSkip.contains(child.getPsi())) {
//      return child.getTreeNext();
//    }
    final Pair<PsiElement, Language> root = GspGroovyBlock.findPsiRootAt(child);
    int htmlTagOffset;
    if (root != null && child.getPsi() instanceof GspOuterGroovyElement) {
      createGspGroovyNode(result, child, Indent.getNormalIndent());
      myTextRange = new TextRange(child.getTextRange().getEndOffset(), myTextRange.getEndOffset());
      return child.getTreeNext();
    } else if (GspPsiUtil.isJSInjection(child.getPsi()) && myChildrenToSkip.size() == 0) {

      // todo implement for fragmented injections
      myChildrenToSkip = processJavascriptTagBody(child.getPsi());
      // create block for JavaScript injection
      if (myChildrenToSkip.size() < 2) {
        GspBlockGenerator.createForeignLanguageBlock(JavaScriptSupportLoader.JAVASCRIPT.getLanguage(),
                child, result, myXmlFormattingPolicy, myXmlFormattingPolicy.getSettings());
      } else {
        GspBlockGenerator.createGspBlockByChildNode(result, myNode, child, wrap, alignment, myXmlFormattingPolicy);
      }
      myTextRange = new TextRange(child.getTextRange().getEndOffset(), myTextRange.getEndOffset());

    } else if ((htmlTagOffset = calculatePossibleHtmlTagBegin(child)) >= 0) {
      XmlTag tag = getHtmlTagByOffset(htmlTagOffset, child);
      if (tag != null &&
              containsTag(tag) &&
              doesNotIntersectSubTagsWith(tag) &&
              isGoodTag(tag)) {
        if (htmlTagOffset > myTextRange.getStartOffset()) {
          TextRange trashRange = new TextRange(myTextRange.getStartOffset(), htmlTagOffset);
          GspBlockGenerator.createGspBlockByTextRange(result, child, wrap, alignment, myXmlFormattingPolicy, trashRange);
        }
        XmlTag[] nestedGspTags = getSubTags();
        GspBlockGenerator.createHtmlBlockByChildNode(result, myNode, tag.getNode(), wrap,
                alignment, myXmlFormattingPolicy, nestedGspTags);
        int tagEndOffset = tag.getTextRange().getEndOffset();
        if (tagEndOffset == myTextRange.getEndOffset()) {
          myTextRange = new TextRange(tagEndOffset, tagEndOffset);
          return child.getTreeNext();
        } else {
          // Outer element is a outers element
          ASTNode newChild = myNode.findLeafElementAt(tagEndOffset - myNode.getStartOffset());
          myTextRange = new TextRange(tagEndOffset, myTextRange.getEndOffset());
          while (newChild != null && newChild.getTreeParent() != myNode) {
            newChild = newChild.getTreeParent();
          }
          return newChild;
        }
      }
    }
    GspBlockGenerator.createGspBlockByTextRange(result, child, wrap, alignment, myXmlFormattingPolicy,
            myTextRange.intersection(child.getTextRange()));
    myTextRange = new TextRange(child.getTextRange().getEndOffset(), myTextRange.getEndOffset());
    return child.getTreeNext();
  }

  private ArrayList<PsiElement> processJavascriptTagBody(PsiElement child) {
    ArrayList<PsiElement> childrenToSkip = new ArrayList<PsiElement>();
    PsiElement parent = child.getParent();
    assert parent instanceof GspGrailsTag;

    while (child != null) {
      if (JavaScriptIntegrationUtil.isJavaScriptInjection(child, parent)) {
        childrenToSkip.add(child);
      }
      child = child.getNextSibling();
    }

    return childrenToSkip;
  }

  private boolean isGoodTag(XmlTag tag) {
    for (PsiElement element : tag.getChildren()) {
      if (element instanceof PsiErrorElement) {
        return false;
      }
    }
    return true;
  }

  private XmlTag getHtmlTagByOffset(int htmlTagOffset, ASTNode child) {
    final FileViewProvider viewProvider = child.getPsi().getContainingFile().getViewProvider();
    final PsiFile file = viewProvider.getPsi(StdLanguages.HTML);
    ASTNode found = file.getNode().findLeafElementAt(htmlTagOffset);
    if (found != null) {
      final ASTNode foundTag = findTagParentWithTheSameOffset(found);
      if (foundTag == null) return null;
      final PsiElement foundPsiElement = foundTag.getPsi();
      if (foundPsiElement instanceof XmlTag) {
        return (XmlTag) foundTag.getPsi();
      }
    }
    return null;
  }

  protected static ASTNode findTagParentWithTheSameOffset(final ASTNode correspondingNode) {
    int offset = correspondingNode.getTextRange().getStartOffset();
    ASTNode result = correspondingNode;
    while (result.getTreeParent() != null
            && result.getTreeParent().getTextRange().getStartOffset() == offset) {
      if (result.getTreeParent().getPsi() instanceof XmlTag) return result.getTreeParent();
      result = result.getTreeParent();
    }
    return result;
  }


  private int calculatePossibleHtmlTagBegin(ASTNode child) {
    final FileViewProvider viewProvider = child.getPsi().getContainingFile().getViewProvider();
    final PsiFile file = viewProvider.getPsi(StdLanguages.HTML);
    assert file != null;
    TextRange range = child.getTextRange();
    int curOffset = Math.max(range.getStartOffset(), myTextRange.getStartOffset());
    int endOffset = Math.min(range.getEndOffset(), myTextRange.getEndOffset());

    ASTNode astNode = file.getNode();
    assert astNode != null;
    ASTNode leaf = astNode.findLeafElementAt(curOffset);
    while (leaf != null && curOffset < endOffset && leaf.getElementType() != XmlTokenType.XML_START_TAG_START) {
      curOffset++;
      leaf = astNode.findLeafElementAt(curOffset);
    }

    if (leaf == null) return -1;
    if (leaf.getElementType() == XmlTokenType.XML_START_TAG_START) return curOffset;
    return -1;
  }


  protected ASTNode processChild(List<Block> result,
                                 final ASTNode child,
                                 final Wrap wrap,
                                 final Alignment alignment,
                                 final Indent indent) {
    final PsiElement childPsi = child.getPsi();
    if (!myTextRange.intersectsStrict(child.getTextRange())) return child.getTreeNext();
    if (childPsi instanceof OuterLanguageElement) {
      return processNonGspChild(child, indent, result, wrap, alignment);
    } else {
      GspBlockGenerator.createGspBlockByChildNode(result, myNode, child, wrap, alignment, myXmlFormattingPolicy);
      myTextRange = new TextRange(child.getTextRange().getEndOffset(), myTextRange.getEndOffset());
      return child.getTreeNext();
    }
  }

  protected void createGspGroovyNode(final List<Block> localResult, final ASTNode child, final Indent indent) {
    localResult.add(new GspGroovyBlock(child, myXmlFormattingPolicy, GspGroovyBlock.findPsiRootAt(child), indent));
  }

  public static ASTNode findChildAfter(@NotNull final ASTNode child, final int endOffset) {
    TreeElement fileNode = TreeUtil.getFileElement((TreeElement) child);
    final LeafElement leaf = fileNode.findLeafElementAt(endOffset);
    if (leaf != null && leaf.getStartOffset() == endOffset && endOffset > 0) {
      return fileNode.findLeafElementAt(endOffset - 1);
    }
    return leaf;
  }


  public boolean containsTag(final PsiElement tag) {
    return tag.getTextRange().getStartOffset() >= myTextRange.getStartOffset()
            && tag.getTextRange().getEndOffset() <= myTextRange.getEndOffset();
  }

  public boolean doesNotIntersectSubTagsWith(final PsiElement tag) {
    final TextRange tagRange = tag.getTextRange();
    final XmlTag[] subTags = getSubTags();
    for (XmlTag subTag : subTags) {
      final TextRange subTagRange = subTag.getTextRange();
      if (subTagRange.getEndOffset() < tagRange.getStartOffset()) continue;
      if (subTagRange.getStartOffset() > tagRange.getEndOffset()) return true;

      if (GspHtmlBlock.areOvercrossing(subTagRange, tagRange))
        return false;
    }
    return true;
  }


  public static XmlTag[] collectSubTags(final XmlElement node) {
    final List<XmlTag> result = new ArrayList<XmlTag>();
    node.processElements(new PsiElementProcessor() {
      public boolean execute(final PsiElement element) {
        if (element instanceof XmlTag) {
          result.add((XmlTag) element);
        }
        return true;
      }
    }, node);
    return result.toArray(new XmlTag[result.size()]);
  }

  public XmlTag[] getSubTags() {

    if (myNode instanceof XmlTag) {
      return ((XmlTag) myNode.getPsi()).getSubTags();
    } else if (myNode.getPsi() instanceof XmlElement) {
      return collectSubTags((XmlElement) myNode.getPsi());
    } else {
      return new XmlTag[0];
    }
  }

  public boolean canBeAnotherTreeTagStart(final ASTNode child) {
    boolean can = GspPsiUtil.getGspFile(myNode.getPsi()) != null
            && (isXmlTag(myNode) || myNode.getElementType() == XmlElementType.XML_DOCUMENT || myNode.getPsi() instanceof PsiFile)
            && child.getPsi() instanceof OuterLanguageElement;
    return can;
  }

  protected static boolean isXmlTag(final ASTNode child) {
    return isXmlTag(child.getPsi());
  }

  protected static boolean isXmlTag(final PsiElement psi) {
    return psi instanceof XmlTag
            && !(psi instanceof GspScriptletTag)
            && !(psi instanceof GspDirective)
            && !(psi instanceof GspDeclarationTag);
  }


}
