package org.jetbrains.plugins.grails.projectView.v2.nodes.leafs;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.projectView.GrailsAction;
import org.jetbrains.plugins.grails.projectView.GrailsView;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;
import org.jetbrains.plugins.groovy.mvc.projectView.MvcNodeDescriptor;
import org.jetbrains.plugins.groovy.mvc.projectView.FieldNode;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dmitry Krasilchikov
 */
public class ActionNode extends FieldNode {
  public ActionNode(@NotNull final Module module, @NotNull final GrField field, @Nullable final ViewSettings viewSettings) {
    // For automatic invalidation method/action nodes after changing
    // semantic of RMethod (action method <-> usual method)
    // we should use nodeId with different locations
    super(module, field, NodeId.ACTION_IN_CONTROLLERS_SUBTREE, viewSettings);
  }

  @Override
  protected GrField extractPsiFromValue() {
    return (GrField)super.extractPsiFromValue();
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "Action: " + ((GrField)psiElement).getName();
  }

  @Override
  protected Collection<AbstractTreeNode> getChildrenImpl() {
    final List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
    final GrField grActionClosure = extractPsiFromValue();
    assert grActionClosure != null;

    final GrailsAction grailsAction = GrailsAction.fromField(grActionClosure);
    assert grailsAction != null; // can't be null here, tree must be upto date

    final Module module = getModule();

    final List<GrailsView> viewList = grailsAction.getViews();
    for (GrailsView view : viewList) {
      final PsiFile file = PsiManager.getInstance(getProject()).findFile(view.getFile());
      if (file != null) {
        children.add(new ViewNode(module, file, getSettings(), NodeId.CONTROLLERS_SUBTREE));
      }
    }

    if (children.isEmpty()) {
      return null;
    }

    return children;
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    super.updateImpl(data);

    //final GrMethod method = extractPsiFromValue();
    //data.setIcons(RContainerPresentationUtil.getIconWithModifiers(method, RailsIcons.RAILS_ACTION_NODE));
    data.setIcons(GrailsIcons.GRAILS_ACTION_NODE);
  }

  @Override
  protected boolean containsImpl(@NotNull final VirtualFile file) {
    if (!isValid()) {
      return false;
    }
    if (super.containsImpl(file)) {
      return true;
    }
    final GrailsAction grailsAction = GrailsAction.fromField(extractPsiFromValue());
    assert grailsAction != null;
    final List<GrailsView> views = grailsAction.getViews();
    for (GrailsView view : views) {
      if (view.getFile() == file) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  @Override
  public MvcNodeDescriptor.SortInfo getSortInformation() {
    return MvcNodeDescriptor.SortInfo.ACTION;
  }

  @Override
  public boolean validate() {
    if (!super.validate()) {
      return false;
    }
    final GrField field = extractPsiFromValue();
    assert field != null;
    if (GrailsAction.fromField(field) == null) {
      setValue(null);
    }
    return getValue() != null;
  }
}