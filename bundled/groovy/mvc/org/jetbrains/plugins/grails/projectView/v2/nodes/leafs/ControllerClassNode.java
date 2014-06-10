package org.jetbrains.plugins.grails.projectView.v2.nodes.leafs;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.grails.GrailsIcons;
import org.jetbrains.plugins.grails.projectView.GrailsController;
import org.jetbrains.plugins.grails.projectView.GrailsLayout;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock;
import org.jetbrains.plugins.groovy.mvc.projectView.NodeId;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrField;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.plugins.groovy.mvc.projectView.ClassNode;
import org.jetbrains.plugins.groovy.mvc.projectView.FieldNode;

import java.util.List;

/**
 * @author Dmitry Krasilschikov
 */
public class ControllerClassNode extends ClassNode {
  public ControllerClassNode(@NotNull final Module module,
                             @NotNull final GrTypeDefinition controllerClass,
                             @Nullable final ViewSettings viewSettings) {
    super(module, controllerClass, NodeId.CONTROLLER_IN_CONTROLLERS_SUBTREE, viewSettings);
  }

  @Nullable
  @Override
  protected FieldNode createNodeForField(final Module module, final GrField field, final String parentLocationRootMark) {
    final GrTypeDefinition thisClass = extractPsiFromValue();
    assert thisClass != null;

    if (!field.hasModifierProperty(PsiModifier.STATIC) && field.getInitializerGroovy() instanceof GrClosableBlock) {
      return new ActionNode(getModule(), field, getSettings());
    }

    return null;
  }

  @Override
  protected String getTestPresentationImpl(@NotNull final NodeId nodeId, @NotNull final PsiElement psiElement) {
    return "Controller: " + ((GrTypeDefinition)psiElement).getName();
  }

  protected boolean containsImpl(@NotNull final VirtualFile file) {
    final VirtualFile virtualFile = getVirtualFile();
    assert virtualFile != null;


    final GrailsController controller = GrailsController.fromFile(getModule(), virtualFile);
    return isFileBelongsToController(file, controller);
  }

  public static boolean isFileBelongsToController(final VirtualFile file, final GrailsController controller) {
    if (controller == null) {
      return false;
    }
    // controller file
    if (file == controller.getTypeDefinition().getContainingFile().getOriginalFile()) {
      return true;
    }

    // view
    final VirtualFile viewsFolder = controller.getViewsFolder();
    if (viewsFolder != null) {
      if (viewsFolder == file || file.getParent() == viewsFolder) {
        return true;
      }
    }
    // layout
    final List<GrailsLayout> layouts = controller.getLayouts();
    if (layouts != null) {
      for (GrailsLayout layout : layouts) {
        if (file == layout.getLayoutFile()) {
          return true;
        }
      }
    }
    // else
    return false;
  }

  @Override
  protected void updateImpl(final PresentationData data) {
    super.updateImpl(data);
    data.setIcons(GrailsIcons.GRAILS_CONTROLLER_NODE);
  }

  @NotNull
  @Override
  public SortInfo getSortInformation() {
    return SortInfo.CONTROLLER;
  }

  @Override
  public boolean validate() {
    if (!super.validate()) {
      return false;
    }
    final GrTypeDefinition grTypeDefinition = extractPsiFromValue();
    assert grTypeDefinition != null;

    if (GrailsController.fromClass(grTypeDefinition) == null) {
      setValue(null);
    }
    return getValue() != null;
  }
}
