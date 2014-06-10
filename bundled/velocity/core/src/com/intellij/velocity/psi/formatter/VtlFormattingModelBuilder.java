package com.intellij.velocity.psi.formatter;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageFormatting;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.DocumentBasedFormattingModel;
import com.intellij.psi.formatter.FormatterUtil;
import com.intellij.psi.formatter.FormatterUtilHelper;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.velocity.psi.VtlElementTypes;
import static com.intellij.velocity.psi.formatter.BlockUtil.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * @author Alexey Chmutov
 *         Date: Jun 26, 2009
 *         Time: 4:07:09 PM
 */
public class VtlFormattingModelBuilder implements DelegatingFormattingModelBuilder {

  static {
    FormatterUtil.addHelper(new FormatterUtilHelper() {
      public boolean containsWhitespacesOnly(ASTNode node) {
        return (node.getElementType() == VtlElementTypes.TEMPLATE_TEXT) && node.getText().trim().length() == 0;
      }

      public boolean addWhitespace(ASTNode treePrev, LeafElement whiteSpaceElement) {
        return false;
      }
    });
  }

  @NotNull
  public FormattingModel createModel(PsiElement element, CodeStyleSettings settings) {
    final PsiFile file = element.getContainingFile();
    Block rootBlock = getRootBlock(element, file.getViewProvider(), settings);
    return new DocumentBasedFormattingModel(rootBlock, element.getProject(), settings, file.getFileType(), file);
  }

  private Block getRootBlock(PsiElement element, FileViewProvider viewProvider, CodeStyleSettings settings) {
    ASTNode node = element.getNode();
    TextRange range = element.getTextRange();
    final AbstractBlock dummyRootBlock = new AbstractBlock(node, Wrap.createWrap(WrapType.NONE, false), Alignment.createAlignment()) {
      protected List<Block> buildChildren() {
        return Collections.emptyList();
      }

      public Spacing getSpacing(final Block child1, final Block child2) {
        return Spacing.getReadOnlySpacing();
      }

      public boolean isLeaf() {
        return true;
      }
    };
    if (node == null || range == null) {
      return dummyRootBlock;
    }
    final Language dataLanguage = ((TemplateLanguageFileViewProvider)viewProvider).getTemplateDataLanguage();
    final FormattingModelBuilder builder = LanguageFormatting.INSTANCE.forLanguage(dataLanguage);
    if (builder instanceof DelegatingFormattingModelBuilder && ((DelegatingFormattingModelBuilder)builder).dontFormatMyModel()) {
      return dummyRootBlock;
    }
    if (builder == null) {
      return new VtlBlock(node, Collections.<Block>emptyList());
    }
    final FormattingModel model = builder.createModel(viewProvider.getPsi(dataLanguage), settings);
    List<Block> childWrappers = buildWrappers(model.getRootBlock().getSubBlocks());
    if (childWrappers.size() == 1) {
      childWrappers = ((DataLanguageBlockWrapper)childWrappers.get(0)).getOriginal().getSubBlocks();
    }
    return new VtlBlock(node, filterBlocksByRange(buildWrappers(childWrappers), range));
  }

  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }

  public boolean dontFormatMyModel() {
    return true;
  }
}
