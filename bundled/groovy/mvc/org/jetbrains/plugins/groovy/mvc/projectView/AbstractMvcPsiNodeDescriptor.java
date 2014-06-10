package org.jetbrains.plugins.groovy.mvc.projectView;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.AbstractPsiBasedNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;

/**
 * @author Dmitry Krasilschikov
 */
public abstract class AbstractMvcPsiNodeDescriptor extends AbstractPsiBasedNode<NodeId> implements MvcNodeDescriptor {
  private final Module myModule;

  @NonNls
  protected abstract String getTestPresentationImpl(@NotNull final NodeId nodeId,
                                                    @NotNull final PsiElement psiElement);

  protected AbstractMvcPsiNodeDescriptor(@NotNull final Module module,
                                           @Nullable final ViewSettings viewSettings,
                                           @NotNull final NodeId nodeId) {
    super(module.getProject(), nodeId, viewSettings);
    myModule = module;
  }

  @Override
  public final boolean contains(@NotNull final VirtualFile file) {
    return isValid() && containsImpl(file);
  }

  protected boolean containsImpl(@NotNull final VirtualFile file) {
    return super.contains(file);
  }

  @Nullable
  protected PsiElement extractPsiFromValue() {
    final NodeId nodeId = getValue();
    return nodeId != null ? nodeId.getPsiElement() : null;
  }

  @Override
  public final String getTestPresentation() {
    if (!isValid()) {
      return "null";
    }

    return getTestPresentationImpl(getValue(), extractPsiFromValue());
  }

  @NotNull
  public Module getModule() {
    return myModule;
  }

  @Nullable
  @Override
  public VirtualFile getVirtualFile() {
    if (!isValid()) {
      return null;
    }
    final PsiElement psiElement = extractPsiFromValue();
    assert psiElement != null;

    if (psiElement instanceof PsiFileSystemItem) {
      return ((PsiFileSystemItem)psiElement).getVirtualFile();
    }
    return psiElement.getContainingFile().getVirtualFile();
  }

  protected void updateImpl(final PresentationData data) {
    final PsiElement psiElement = extractPsiFromValue();
    if (psiElement instanceof NavigationItem) {
      final ItemPresentation presentation = ((NavigationItem)psiElement).getPresentation();
      assert presentation != null;

      data.setPresentableText(presentation.getPresentableText());
    }
  }

  @Override
  public final int getWeight() {
    return getSortInformation().getWeight();
  }

  @Override
  public final int getTypeSortWeight(final boolean sortByType) {
    return getSortInformation().getTypeSortWeight(sortByType);
  }

  protected boolean hasProblemFileBeneath() {
    return WolfTheProblemSolver.getInstance(getProject()).hasProblemFilesBeneath(new Condition<VirtualFile>() {
      public boolean value(final VirtualFile virtualFile) {
        // Rails Project View doesn't support flattern packages, also contains() method is
        // used for update and there is no sense in checking it's childs on contains this
        // (i.e. someChildContainsFile(virtualFile))
        return contains(virtualFile);
      }
    });
  }

  @NotNull
  public SortInfo getSortInformation() {
    return SortInfo.DEFAULT_NODE;
  }

  /**
   * This method is internal and should be invoked only by TreeBuilder and NodeDescriptor.update().
   * Nodes shouldn't use this for checking actuality of current node
   * instead use isValid(). Otherwise it will be lead to problems in updating childrens in TreeBuilder
   * @return True if node is valid
   */
  @Override
  public boolean validate() {
    return super.validate();
  }

  public boolean isValid() {
    final PsiElement psiElement = extractPsiFromValue();
    return psiElement != null && psiElement.isValid();
  }
}
