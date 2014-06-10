package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.resources.WebBeansBundle;
import com.intellij.webBeans.utils.WebBeansCommonUtils;

import java.util.Collection;

public class ScopeNode extends AbstractWebBeansTypeNode {
  public ScopeNode(Module module, WebBeansModuleNode webBeansModuleNode) {
    super(webBeansModuleNode, module, WebBeansNodeTypes.SCOPE, WebBeansBundle.message("actions.show.scope"));

    setUniformIcon(WebBeansIcons.SCOPE_TYPES);
  }

  public SimpleNode[] getChildren() {
    Collection<PsiClass> deploymentTypesClasses = WebBeansCommonUtils.getScopeTypesClasses(getModule());

    Collection<PsiMemberSimpleNode> nodes = createSortedList();
    for (PsiClass psiClass : deploymentTypesClasses) {
      nodes.add(new AnnotationTypeNode(getModule(), psiClass, this));
    }
    return nodes.toArray(new SimpleNode[nodes.size()]);
  }
}
