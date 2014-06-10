package org.jetbrains.plugins.grails.lang.gsp.formatter;

import com.intellij.formatting.FormattingModel;
import com.intellij.formatting.FormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.PsiBasedFormattingModel;
import com.intellij.psi.formatter.xml.XmlPolicy;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.impl.source.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.lang.gsp.psi.html.api.GspLikeFile;

/**
 * @author ilyas
 */
public class GspFormattingModelBuilder implements FormattingModelBuilder {
  @NotNull
  public FormattingModel createModel(final PsiElement element, final CodeStyleSettings settings) {
    ASTNode root = TreeUtil.getFileElement((TreeElement) SourceTreeToPsiMap.psiElementToTree(element));
    PsiFile containingFile = element.getContainingFile();
    if (containingFile instanceof GspLikeFile) {
      containingFile = ((GspLikeFile) containingFile).getGspLanguageRoot();
      root = containingFile.getNode();
    }
    final FormattingDocumentModelImpl documentModel = FormattingDocumentModelImpl.createOn(containingFile);
    return new PsiBasedFormattingModel(containingFile,
            new GspBlock(root, null, null, new XmlPolicy(settings, documentModel), null, null),
            documentModel);
  }

  @Nullable
  public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
    return null;
  }
}
