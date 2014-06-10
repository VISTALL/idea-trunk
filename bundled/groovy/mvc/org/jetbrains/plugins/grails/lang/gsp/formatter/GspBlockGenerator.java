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

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageFormatting;
import com.intellij.lang.javascript.JSElementTypes;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.JavascriptParserDefinition;
import com.intellij.lang.javascript.formatter.JSFormattingModel;
import com.intellij.lang.javascript.formatter.Util;
import com.intellij.lang.javascript.formatter.blocks.JSBlock;
import com.intellij.lang.javascript.formatter.blocks.SubBlockVisitor;
import com.intellij.lang.javascript.psi.JSAssignmentExpression;
import com.intellij.lang.javascript.psi.JSBinaryExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLoopStatement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlFormattingPolicy;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.addins.GrailsIntegrationUtil;
import org.jetbrains.plugins.grails.addins.js.CssIntegrationUtil;
import org.jetbrains.plugins.grails.addins.js.JavaScriptIntegrationUtil;
import org.jetbrains.plugins.grails.lang.gsp.formatter.processors.GspIndentProcessor;
import org.jetbrains.plugins.grails.lang.gsp.psi.GspPsiUtil;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.gtag.GspXmlRootTag;

import java.util.List;

/**
 * @author ilyas
 */
public class GspBlockGenerator {
  private GspBlockGenerator() {
  }

  /**
   * Creates child block for block with given child by it's textRange
   *
   * @param child     node of parent block
   * @param textRange textRange of child block
   */
  public static void createGspBlockByTextRange(List<Block> result,
                                               ASTNode child,
                                               Wrap wrap,
                                               Alignment alignment,
                                               XmlFormattingPolicy policy,
                                               TextRange textRange) {

    ASTNode parent = child.getTreeParent();
    Indent indent = parent != null && parent.getPsi() instanceof XmlTag &&
            !(parent.getPsi() instanceof GspXmlRootTag) ?
            Indent.getNormalIndent() : Indent.getNoneIndent();

    if (!child.getTextRange().contains(textRange)) return;

    String text = child.getPsi().getContainingFile().getText();
    int start = textRange.getStartOffset();
    int end = textRange.getEndOffset();
    String s = end < text.length() - 1 ?
            text.substring(start, end) :
            text.substring(start);
    if (s.trim().length() == 0) return;

    while (!text.substring(start, start + 1).equals(text.substring(start, start + 1).trim())) {
      start++;
    }
    while (!text.substring(end - 1, end).equals(text.substring(end - 1, end).trim())) {
      end--;
    }

    assert start < end;

    result.add(new GspBlock(child, wrap, alignment, policy, indent, new TextRange(start, end)));
  }

  /**
   * Creates block by child ASTNode
   *
   * @param child given childNode
   */
  public static void createGspBlockByChildNode(List<Block> result,
                                               ASTNode parentNode,
                                               ASTNode child,
                                               Wrap wrap,
                                               Alignment alignment,
                                               XmlFormattingPolicy policy) {
    if (!canBeCorrectBlock(child)) {
      return;
    }
    result.add(new GspBlock(child, wrap, alignment, policy, GspIndentProcessor.getGspChildIndent(parentNode, child, policy), child.getTextRange()));
  }

  /**
   * Creates block by child ASTNode
   *
   * @param child given childNode
   */
  public static void createHtmlBlockByChildNode(List<Block> result,
                                                ASTNode parentNode,
                                                ASTNode child,
                                                Wrap wrap,
                                                Alignment alignment,
                                                XmlFormattingPolicy policy,
                                                XmlTag[] nestedGspTags) {
    if (!canBeCorrectBlock(child)) {
      return;
    }

    PsiElement childPsi = child.getPsi();
    if (GrailsIntegrationUtil.isJsSupportEnabled() &&
            (JavaScriptIntegrationUtil.isJavaScriptInjection(childPsi, childPsi.getParent()) ||
                    JavaScriptIntegrationUtil.isJSEmbeddedContent(childPsi))) {
      createForeignLanguageBlock(JavaScriptSupportLoader.JAVASCRIPT.getLanguage(),
              child, result, policy, policy.getSettings());
    } else if (GrailsIntegrationUtil.isCssSupportEnabled() && CssIntegrationUtil.isCssStylesheet(child)) {
      createForeignLanguageBlock(child.getPsi().getLanguage(),
              child, result, policy, policy.getSettings());
    } else {
      result.add(new GspHtmlBlock(child,
              wrap,
              alignment,
              policy,
              GspIndentProcessor.getGspChildIndent(parentNode, child, policy),
              nestedGspTags));
    }
  }


  public static void createForeignLanguageBlock(final Language childLanguage,
                                                final ASTNode child,
                                                final List<Block> result, final XmlFormattingPolicy policy,
                                                final CodeStyleSettings settings) {
    final PsiElement childPsi = child.getPsi();
    FormattingModelBuilder builder;
    if (GspPsiUtil.isJSInjection(childPsi)) {
      generateBlockForJSInjection(childPsi, result, settings);
    } else {
      builder = LanguageFormatting.INSTANCE.forContext(childLanguage, childPsi);
      if (builder != null) {
        final FormattingModel childModel = builder.createModel(childPsi, settings);
        final Indent childIndent = GrailsIntegrationUtil.isCssSupportEnabled() && CssIntegrationUtil.isCssLanguage(childLanguage) 
                                   ? Indent.getNoneIndent()
                                   : Indent.getNormalIndent();
        result.add(new ForeignLanguageBlock(child,
                policy,
                childModel.getRootBlock(), childIndent));
      }
    }
  }

  private static void generateBlockForJSInjection(PsiElement outer, List<Block> result, CodeStyleSettings settings) {
    PsiFile file = outer.getContainingFile();
    final int offset = outer.getTextRange().getStartOffset();
    PsiElement element = InjectedLanguageUtil.findElementAtNoCommit(file, offset);
    FormattingModelBuilder builder = getJSFormattingModelBuilder(offset, file, outer);
    if (builder != null) {
      final FormattingModel childModel = builder.createModel(element, settings);
      Block rootJsBlock = childModel.getRootBlock();
      result.add(rootJsBlock);
    }
  }


  private static FormattingModelBuilder getJSFormattingModelBuilder(final int offset, final PsiFile file, final PsiElement outer) {
    return new FormattingModelBuilder() {
      @NotNull
      public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
        JSBlock jsBlock = new MyJSRootBlock(outer, settings, offset, file);
        return new JSFormattingModel(file, settings, jsBlock);
      }

      @Nullable
      public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return null;
      }
    };
  }

  public static boolean canBeCorrectBlock(final ASTNode node) {
    return node != null && (node.getText().trim().length() > 0);
  }

  private static class MyJSSubBlockVisitor extends SubBlockVisitor {
    private final CodeStyleSettings mySettings;
    private final int myOffset;

    public MyJSSubBlockVisitor(CodeStyleSettings settings, int offset) {
      super(settings);
      mySettings = settings;
      myOffset = offset;
    }

    @Nullable
    private static Alignment alignmentProjection(final Alignment defaultAlignment, final ASTNode parent, final ASTNode child) {
      if (parent.getElementType() == JSElementTypes.FOR_STATEMENT &&
              (JSElementTypes.EXPRESSIONS.contains(child.getElementType()) ||
                      child.getElementType() == JSElementTypes.VAR_STATEMENT)) {
        return defaultAlignment;
      } else if (parent.getElementType() == JSElementTypes.PARAMETER_LIST &&
              child.getElementType() == JSElementTypes.FORMAL_PARAMETER) {
        return defaultAlignment;
      } else if (parent.getPsi() instanceof JSBinaryExpression &&
              JSElementTypes.EXPRESSIONS.contains(child.getElementType())) {
        return defaultAlignment;
      } else if (parent.getElementType() == JSElementTypes.CONDITIONAL_EXPRESSION &&
              JSElementTypes.EXPRESSIONS.contains(child.getElementType())) {
        return defaultAlignment;
      }

      return null;
    }

    @Nullable
    private static Alignment getDefaultAlignment(final ASTNode node) {
      if (node.getElementType() == JSElementTypes.FOR_STATEMENT ||
              node.getElementType() == JSElementTypes.PARAMETER_LIST ||
              node.getElementType() == JSElementTypes.BINARY_EXPRESSION ||
              node.getElementType() == JSElementTypes.ASSIGNMENT_EXPRESSION ||
              node.getElementType() == JSElementTypes.CONDITIONAL_EXPRESSION) {
        return Alignment.createAlignment();
      }

      return null;
    }


    public void visitElement(final ASTNode node) {
      Alignment alignment = getDefaultAlignment(node);

      ASTNode child = node.getFirstChildNode();
      while (child != null) {
        if (child.getElementType() != JSTokenTypes.WHITE_SPACE &&
                child.getTextRange().getLength() > 0) {
          Wrap wrap = getWrap(node, child);
          Alignment childAlignment = alignmentProjection(alignment, node, child);
          Indent childIndent = getIndent(node, child);
          JSBlock jsBlock = new MyJSBlock(child, childAlignment, childIndent, wrap, mySettings, myOffset);
          getBlocks().add(jsBlock);
        }
        child = child.getTreeNext();
      }
    }

    @Nullable
    private Wrap getWrap(final ASTNode node, final ASTNode child) {
      WrapType wrapType = null;
      if (node.getElementType() == JSElementTypes.ASSIGNMENT_EXPRESSION) {
        final JSAssignmentExpression assignment = (JSAssignmentExpression) node.getPsi();
        if (child.getElementType() == assignment.getOperationSign() && mySettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE ||
                child.getPsi() == assignment.getROperand() && !mySettings.PLACE_ASSIGNMENT_SIGN_ON_NEXT_LINE) {
          wrapType = Util.getWrapType(mySettings.ASSIGNMENT_WRAP);
        }
      } else if (node.getElementType() == JSElementTypes.BINARY_EXPRESSION) {
        final JSBinaryExpression binary = (JSBinaryExpression) node.getPsi();
        if (child.getElementType() == binary.getOperationSign() && mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE ||
                child.getPsi() == binary.getROperand() && !mySettings.BINARY_OPERATION_SIGN_ON_NEXT_LINE) {
          wrapType = Util.getWrapType(mySettings.BINARY_OPERATION_WRAP);
        }
      } else if (node.getElementType() == JSElementTypes.PARENTHESIZED_EXPRESSION) {
        if (child == node.findChildByType(JSTokenTypes.LPAR) && mySettings.PARENTHESES_EXPRESSION_LPAREN_WRAP) {
          wrapType = Wrap.NORMAL;
        } else if (child == node.findChildByType(JSTokenTypes.RPAR) && mySettings.PARENTHESES_EXPRESSION_RPAREN_WRAP) {
          wrapType = Wrap.ALWAYS;
        }
      } else if (node.getElementType() == JSElementTypes.ARRAY_LITERAL_EXPRESSION) {
        if (child == node.findChildByType(JSTokenTypes.LBRACE) && mySettings.ARRAY_INITIALIZER_LBRACE_ON_NEXT_LINE) {
          wrapType = Wrap.NORMAL;
        } else
        if (child == node.findChildByType(JSTokenTypes.RPAR) && mySettings.ARRAY_INITIALIZER_RBRACE_ON_NEXT_LINE) {
          wrapType = Wrap.ALWAYS;
        }
      } else if (node.getElementType() == JSElementTypes.CONDITIONAL_EXPRESSION) {
        final IElementType elementType = child.getElementType();
        if ((mySettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE && (elementType == JSTokenTypes.QUEST || elementType == JSTokenTypes.COLON)) ||
                (!mySettings.TERNARY_OPERATION_SIGNS_ON_NEXT_LINE && child.getPsi() instanceof JSExpression)) {
          wrapType = Util.getWrapType(mySettings.TERNARY_OPERATION_WRAP);
        }
      } else if (node.getElementType() == JSElementTypes.CALL_EXPRESSION) {
        if (child == node.findChildByType(JSTokenTypes.LPAR) && mySettings.CALL_PARAMETERS_LPAREN_ON_NEXT_LINE) {
          wrapType = Wrap.NORMAL;
        } else if (child == node.findChildByType(JSTokenTypes.RPAR) && mySettings.CALL_PARAMETERS_RPAREN_ON_NEXT_LINE) {
          wrapType = Wrap.ALWAYS;
        }
      } else if (node.getElementType() == JSElementTypes.PARAMETER_LIST) {
        if (child.getElementType() == JSElementTypes.FORMAL_PARAMETER) {
          wrapType = Util.getWrapType(mySettings.METHOD_PARAMETERS_WRAP);
        }
      } else if (node.getElementType() == JSElementTypes.FOR_STATEMENT ||
              node.getElementType() == JSElementTypes.FOR_IN_STATEMENT) {
        if (JSElementTypes.EXPRESSIONS.contains(child.getElementType())) {
          wrapType = Util.getWrapType(mySettings.FOR_STATEMENT_WRAP);
        }
      }

      return wrapType == null ? null : Wrap.createWrap(wrapType, false);
    }

    private Indent getIndent(final ASTNode node, final ASTNode child) {
      final IElementType nodeElementType = node.getElementType();

      if (nodeElementType == JavascriptParserDefinition.FILE ||
              nodeElementType == JSElementTypes.EMBEDDED_CONTENT) {
        return Indent.getNoneIndent();
      }

      final IElementType childElementType = child.getElementType();
      if (childElementType == JSElementTypes.BLOCK_STATEMENT) {
        if (nodeElementType == JSElementTypes.FUNCTION_DECLARATION &&
                (mySettings.METHOD_BRACE_STYLE == CodeStyleSettings.NEXT_LINE_SHIFTED ||
                        mySettings.METHOD_BRACE_STYLE == CodeStyleSettings.NEXT_LINE_SHIFTED2)) {
          return Indent.getNormalIndent();
        }
        if (mySettings.BRACE_STYLE == CodeStyleSettings.NEXT_LINE_SHIFTED ||
                mySettings.BRACE_STYLE == CodeStyleSettings.NEXT_LINE_SHIFTED2) {
          return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
      }

      if (childElementType == JSElementTypes.CATCH_BLOCK) {
        return Indent.getNoneIndent();
      }

      if (childElementType == JSElementTypes.CASE_CLAUSE) {
        return mySettings.INDENT_CASE_FROM_SWITCH ? Indent.getNormalIndent() : Indent.getNoneIndent();
      }

      if (nodeElementType == JSElementTypes.CASE_CLAUSE) {
        if (JSElementTypes.STATEMENTS.contains(childElementType)) {
          return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
      }

      if (nodeElementType == JSElementTypes.SWITCH_STATEMENT && childElementType == JSTokenTypes.RBRACE) {
        return Indent.getNoneIndent();
      }

      if (nodeElementType == JSElementTypes.IF_STATEMENT) {
        if (childElementType == JSTokenTypes.ELSE_KEYWORD) {
          return Indent.getNoneIndent();
        }
        if (JSElementTypes.SOURCE_ELEMENTS.contains(childElementType)) {
          return Indent.getNormalIndent();
        }
      }

      if (nodeElementType == JSElementTypes.WITH_STATEMENT &&
              JSElementTypes.SOURCE_ELEMENTS.contains(childElementType)) {
        return Indent.getNormalIndent();
      }

      if (nodeElementType == JSElementTypes.DOWHILE_STATEMENT && childElementType == JSTokenTypes.WHILE_KEYWORD) {
        return Indent.getNoneIndent();
      }

      if (nodeElementType == JSElementTypes.TRY_STATEMENT && childElementType == JSTokenTypes.FINALLY_KEYWORD) {
        return Indent.getNoneIndent();
      }

      if (nodeElementType == JSElementTypes.BLOCK_STATEMENT ||
              nodeElementType == JSElementTypes.CLASS ||
              nodeElementType == JSElementTypes.PACKAGE_STATEMENT
              ) {
        final ASTNode parent = node.getTreeParent();
        if (parent != null && parent.getElementType() == JSElementTypes.FUNCTION_DECLARATION &&
                mySettings.METHOD_BRACE_STYLE == CodeStyleSettings.NEXT_LINE_SHIFTED) {
          return Indent.getNoneIndent();
        }
        if (mySettings.BRACE_STYLE == CodeStyleSettings.NEXT_LINE_SHIFTED) {
          return Indent.getNoneIndent();
        }
        if (JSElementTypes.SOURCE_ELEMENTS.contains(childElementType) ||
                JSTokenTypes.COMMENTS.contains(childElementType)) {
          return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
      } else if (node.getPsi() instanceof JSLoopStatement) {
        if (child.getPsi() == ((JSLoopStatement) node.getPsi()).getBody()) {
          if (childElementType == JSElementTypes.BLOCK_STATEMENT) {
            return Indent.getNoneIndent();
          } else {
            return Indent.getNormalIndent();
          }
        }
      }

      if (JSTokenTypes.COMMENTS.contains(childElementType)) {
        return Indent.getNormalIndent();
      }
      if (childElementType == JSElementTypes.OBJECT_LITERAL_EXPRESSION) {
        return nodeElementType == JSElementTypes.ARRAY_LITERAL_EXPRESSION ? Indent.getNormalIndent() : Indent.getNoneIndent();
      }

      if (nodeElementType == JSElementTypes.ARRAY_LITERAL_EXPRESSION) {
        if (childElementType == JSTokenTypes.LBRACKET ||
                childElementType == JSTokenTypes.RBRACKET) {
          return Indent.getNoneIndent();
        }
        return Indent.getNormalIndent();
      }

      if (nodeElementType == JSElementTypes.OBJECT_LITERAL_EXPRESSION) {
        if (childElementType == JSTokenTypes.LBRACE ||
                childElementType == JSTokenTypes.RBRACE) {
          return Indent.getNoneIndent();
        }
        return Indent.getNormalIndent();
      }
      return Indent.getNoneIndent();
    }


  }

  private static class MyJSBlock extends JSBlock {
    private List<Block> mySubBlocks;
    private final int myOffset;
    private final CodeStyleSettings mySettings;

    public MyJSBlock(ASTNode child, Alignment childAlignment, Indent childIndent, Wrap wrap, CodeStyleSettings settings, int offset) {
      super(child, childAlignment, childIndent, wrap, settings);
      myOffset = offset;
      mySettings = settings;
    }

    @NotNull
    public TextRange getTextRange() {
      // Shifted text range
      return super.getTextRange().shiftRight(myOffset);
    }

    @NotNull
    public List<Block> getSubBlocks() {
      if (mySubBlocks == null) {
        SubBlockVisitor visitor = new MyJSSubBlockVisitor(mySettings, myOffset);
        visitor.visit(getNode());
        mySubBlocks = visitor.getBlocks();
      }
      return mySubBlocks;
    }
  }

  private static class MyJSRootBlock extends JSBlock {

    private List<Block> mySubBlocks;
    private final CodeStyleSettings mySettings;
    private final int myOffset;
    private final PsiFile myFile;

    private MyJSRootBlock(PsiElement outer, CodeStyleSettings settings, int offset, PsiFile file) {
      super(outer.getNode(), null, Indent.getNormalIndent(), null, settings);
      mySettings = settings;
      myOffset = offset;
      myFile = file;
      mySubBlocks = null;
    }

    @NotNull
    public List<Block> getSubBlocks() {
      if (mySubBlocks == null) {
        SubBlockVisitor visitor = new MyJSSubBlockVisitor(mySettings, myOffset);
        PsiFile jsFile = InjectedLanguageUtil.findInjectedPsiNoCommit(myFile, myOffset);
        if (jsFile != null && jsFile.getNode() != null) {
          visitor.visit(jsFile.getNode());
        }
        mySubBlocks = visitor.getBlocks();
      }
      return mySubBlocks;
    }

    @NotNull
    public TextRange getTextRange() {
      return super.getTextRange();
    }
  }
}
