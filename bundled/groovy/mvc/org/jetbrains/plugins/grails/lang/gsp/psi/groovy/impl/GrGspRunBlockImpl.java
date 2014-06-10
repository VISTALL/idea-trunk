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

package org.jetbrains.plugins.grails.lang.gsp.psi.groovy.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.grails.lang.gsp.lexer.core.GspTokenTypes;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspDeclarationHolder;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspExprInjection;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GrGspRunBlock;
import org.jetbrains.plugins.grails.lang.gsp.psi.groovy.api.GspGroovyFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspFile;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.GspScriptletTag;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspGrailsTag;
import org.jetbrains.plugins.groovy.lang.lexer.GroovyTokenTypes;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrStatement;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.blocks.GrOpenBlockImpl;

/**
 * @author ilyas
 */
public class GrGspRunBlockImpl extends GrOpenBlockImpl implements GrGspRunBlock {
  private final String GSPBLOCK_SYNTHETIC_NAME = "GspRunBlock";

  public GrGspRunBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return GSPBLOCK_SYNTHETIC_NAME;
  }

  public GrGspDeclarationHolder[] getDeclarationHolders() {
    return findChildrenByClass(GrGspDeclarationHolder.class);
  }

  public GrStatement addStatementBefore(@NotNull GrStatement element, @NotNull GrStatement anchor) throws IncorrectOperationException {
    if (!this.equals(anchor.getParent())) {
      throw new IncorrectOperationException();
    }

    ASTNode elemNode = element.copy().getNode();
    if (anchor instanceof GrGspExprInjection) {
      GspFile gspFile = ((GspGroovyFile) getContainingFile()).getGspLanguageRoot();
      PsiElement injectionStart = anchor.getPrevSibling();
      assert injectionStart != null;
      PsiElement elem = gspFile.findElementAt(injectionStart.getNode().getStartOffset());
      assert elem != null;
      // groovy expression injection holder
      PsiElement parent = elem.getParent();

      ASTNode treePrev = anchor.getNode().getTreePrev();
      treePrev = findAppropriateScriptletEndElement(treePrev, parent);

      ASTNode blockNode = getNode();
      boolean createNewTag = treePrev == null;
      if (!createNewTag) {
        ASTNode openTag = treePrev.getTreePrev();
        while (openTag != null &&
            openTag.getPsi() instanceof PsiWhiteSpace) {
          openTag = openTag.getTreePrev();
        }
        if (openTag != null &&
            GroovyTokenTypes.mNLS != openTag.getElementType()) {
          blockNode.addLeaf(GroovyTokenTypes.mNLS, "\n", treePrev);
        } else {
          blockNode.addLeaf(GroovyTokenTypes.mWS, " ", treePrev);
        }
        blockNode.addChild(elemNode, treePrev);
        blockNode.addLeaf(GroovyTokenTypes.mNLS, "\n", treePrev);
      } else {
        treePrev = blockNode.getFirstChildNode();
        blockNode.addChild(elemNode, treePrev);
        blockNode.addLeaf(GspTokenTypes.JSCRIPT_BEGIN, "<%", elemNode);
        blockNode.addLeaf(GroovyTokenTypes.mNLS, "\n", elemNode);
        blockNode.addLeaf(GroovyTokenTypes.mNLS, "\n", treePrev);
        blockNode.addLeaf(GspTokenTypes.JSCRIPT_END, "%>\n", treePrev);
      }
      ASTNode fileNode = gspFile.getNode();
      assert fileNode != null;
      return (GrStatement) elemNode.getPsi();
    } else {
      return super.addStatementBefore(element, anchor);
    }
  }

  private ASTNode findAppropriateScriptletEndElement(ASTNode treePrev, PsiElement exprParent) {
    while (treePrev != null &&
        !(GspTokenTypes.GSCRIPT_END == treePrev.getElementType() ||
            GspTokenTypes.JSCRIPT_END == treePrev.getElementType())) {
      treePrev = treePrev.getTreePrev();
    }
    GspFile gspFile = ((GspGroovyFile) getContainingFile()).getGspLanguageRoot();
    if (treePrev == null) return treePrev;
    PsiElement elem = gspFile.findElementAt(treePrev.getStartOffset());
    if (elem != null &&
        elem.getParent() instanceof GspScriptletTag) {
      PsiElement prevParent = elem.getParent();
      if (prevParent.getParent() instanceof GspGrailsTag &&
          exprParent != null &&
          exprParent.getParent() != prevParent.getParent()) {
        treePrev = treePrev.getTreePrev();
        return findAppropriateScriptletEndElement(treePrev, exprParent);
      }
    }
    return treePrev;
  }
}
