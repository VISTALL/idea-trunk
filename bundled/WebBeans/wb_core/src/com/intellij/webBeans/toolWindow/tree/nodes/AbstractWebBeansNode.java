package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.containers.SortedList;
import com.intellij.psi.PsiMember;

import java.util.Collection;
import java.util.Comparator;

public abstract class AbstractWebBeansNode extends SimpleNode {
  public AbstractWebBeansNode(SimpleNode parent) {
    super(parent);
  }

  protected static Collection<PsiMemberSimpleNode> createSortedList() {
    return new SortedList<PsiMemberSimpleNode>(new Comparator<PsiMemberSimpleNode>() {
      public int compare(PsiMemberSimpleNode psiMemberSimpleNode, PsiMemberSimpleNode psiMemberSimpleNode1) {
        PsiMember member = psiMemberSimpleNode.getMember();
        PsiMember member2 = psiMemberSimpleNode1.getMember();
        if (member != null && member.isValid() && member2 != null && member2.isValid()) {
          String name = member.getName();
          String name1 = member2.getName();

          if (name != null && name1 != null) {
            return name.compareTo(name1);
          }
        }

        return 0;
      }
    });
  }
}
