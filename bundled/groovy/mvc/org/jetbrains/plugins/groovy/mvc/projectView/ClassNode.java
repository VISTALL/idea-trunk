package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Krasilschikov
 */
public class ClassNode extends AbstractMvcPsiNodeDescriptor {
  public ClassNode(@NotNull final Module module,
                   @NotNull final GrTypeDefinition rClass,
                   @Nullable final String locationMark,
                   @Nullable final ViewSettings viewSettings) {
    super(module, viewSettings, new NodeId(rClass, locationMark));
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "GrTypeDefinition: " + ((GrTypeDefinition)psiElement).getName();
  }

  @Override
  protected GrTypeDefinition extractPsiFromValue() {
    return (GrTypeDefinition)super.extractPsiFromValue();
  }

  @Nullable
  protected Collection<AbstractTreeNode> getChildrenImpl() {
    final List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
    final Module module = getModule();

    final GrTypeDefinition grTypeDefinition = extractPsiFromValue();
    assert grTypeDefinition != null;

    buildChildren(module, grTypeDefinition, children);

    return children.isEmpty() ? null : children;
  }

  protected void buildChildren(final Module module, final GrTypeDefinition grClass, final List<AbstractTreeNode> children) {
    final GrField[] fields = grClass.getFields();
    final String parentLocationRootMark = getValue().getLocationRootMark();
    for (final GrField field : fields) {
      if (field.hasModifierProperty(PsiModifier.STATIC)) continue;
      //if (!(field instanceof GrClosableBlock)) continue;

      final FieldNode node = createNodeForField(module, field, parentLocationRootMark);
      if (node != null) children.add(node);
    }

    final GrMethod[] methods = grClass.getGroovyMethods();
    for (final GrMethod method : methods) {
      if (method.hasModifierProperty(PsiModifier.STATIC)) continue;

      final MethodNode node = createNodeForMethod(module, method, parentLocationRootMark);
      if (node != null) children.add(node);
    }
  }

  @Nullable
  protected FieldNode createNodeForField(final Module module, final GrField field, final String parentLocationRootMark) {
    return null;
  }

  @Nullable
  protected MethodNode createNodeForMethod(final Module module, final GrMethod method, final String parentLocationRootMark) {
    return null;
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.CLASS;
  }
}