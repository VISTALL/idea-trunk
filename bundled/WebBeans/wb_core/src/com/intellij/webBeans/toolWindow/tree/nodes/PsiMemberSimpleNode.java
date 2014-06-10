package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.psi.PsiMember;
import com.intellij.ui.treeStructure.SimpleNode;

public abstract class PsiMemberSimpleNode<T extends PsiMember> extends AbstractWebBeansNode {
  private T myMember;

  protected PsiMemberSimpleNode(SimpleNode parent, T member) {
    super(parent);
    myMember = member;
  }

  public T getMember() {
    return myMember;
  }
}
