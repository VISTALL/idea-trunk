package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;
import org.jetbrains.plugins.groovy.mvc.projectView.AbstractMvcPsiNodeDescriptor;
import org.jetbrains.plugins.groovy.mvc.projectView.MvcNodeDescriptor;

import java.util.Collection;

/**
 * @author peter
 */
public class FileNode extends AbstractMvcPsiNodeDescriptor implements MvcNodeDescriptor {
  public FileNode(@NotNull final Module module,
                  @NotNull final PsiFile file,
                  @Nullable final String locationMark,
                  final ViewSettings viewSettings) {
    super(module, viewSettings, new NodeId(file, locationMark));
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId,
                                           @NotNull final PsiElement psiElement) {
    return "File: " + ((PsiFile)psiElement).getName();
  }

  @Override
  protected PsiFile extractPsiFromValue() {
    return (PsiFile)super.extractPsiFromValue();
  }

  protected Collection<AbstractTreeNode> getChildrenImpl() {
    return null;
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.FILE;
  }

  public Comparable getTypeSortKey() {
    String extension = PsiFileNode.extension(extractPsiFromValue());
    return extension == null ? null : new PsiFileNode.ExtensionSortKey(extension);
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    final PsiFile value = extractPsiFromValue();
    assert value != null;
    data.setPresentableText(value.getName());
    data.setIcons(value.getIcon(Iconable.ICON_FLAG_READ_STATUS));
  }
}
