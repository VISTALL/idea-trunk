package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;
import org.jetbrains.plugins.groovy.mvc.projectView.AbstractMvcPsiNodeDescriptor;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;

import java.util.Collection;

/**
 * @author Dmitry Krasilschikov
 */
public class FieldNode extends AbstractMvcPsiNodeDescriptor {
  public FieldNode(@NotNull final Module module,
                   @NotNull final GrField field,
                   @Nullable final String locationMark,
                   @Nullable final ViewSettings viewSettings) {
    super(module, viewSettings, new NodeId(field, locationMark));
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "GrField: " + ((GrField)psiElement).getName();
  }

  protected Collection<AbstractTreeNode> getChildrenImpl() {
    return null;
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.FIELD;
  }
}
