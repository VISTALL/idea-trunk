package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.resources.WebBeansBundle;
import com.intellij.webBeans.utils.WebBeansCommonUtils;

import java.util.Collection;

public class BindingsNode extends AbstractWebBeansTypeNode {

  public BindingsNode(Module module, WebBeansModuleNode webBeansModuleNode) {
    super(webBeansModuleNode, module, WebBeansNodeTypes.BINDING, WebBeansBundle.message("actions.show.binding.types"));

    setUniformIcon(WebBeansIcons.BINDING_TYPES);
  }

  public SimpleNode[] getChildren() {
    Collection<PsiClass> bindingTypesClasses = WebBeansCommonUtils.getBindingTypesClasses(getModule());

    Collection<PsiMemberSimpleNode> nodes = createSortedList();

    for (PsiClass psiClass : bindingTypesClasses) {
      nodes.add(new BindingAnnotationTypeNode(getModule(), psiClass, this));
    }

    return nodes.toArray(new PsiMemberSimpleNode[nodes.size()]);
  }
}
