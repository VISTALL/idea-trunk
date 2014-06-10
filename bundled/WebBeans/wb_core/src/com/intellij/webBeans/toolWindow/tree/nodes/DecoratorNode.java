package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.resources.WebBeansBundle;

import java.util.Set;

public class DecoratorNode extends AbstractWebBeansTypeNode {

  public DecoratorNode(Module module, WebBeansModuleNode webBeansModuleNode) {
    super(webBeansModuleNode, module, WebBeansNodeTypes.DECORATOR, WebBeansBundle.message("actions.show.binding.types"));

    //setPlainText(WebBeansBundle.message("actions.show.binding.types"));
    //setUniformIcon(WebBeansIcons.BINDING_TYPES);
  }

  public SimpleNode[] getChildren() {
    Set<SimpleNode> nodes = new HashSet<SimpleNode>();
    //Collection<PsiClass> bindingTypesClasses = WebBeansCommonUtils.getBindingTypesClasses(getModule());
    //for (PsiClass psiClass : bindingTypesClasses) {
    //  nodes.add(new AnnotationTypeNode(getModule(), psiClass));
    //}
    return nodes.toArray(new SimpleNode[nodes.size()]);
  }
}
