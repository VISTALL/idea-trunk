package com.intellij.webBeans.toolWindow.tree.nodes;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;

public class AnnotatedMembersNode<T extends PsiMember> extends PsiMemberSimpleNode<T> {
  private Module myModule;
  protected PsiMember myMember;

  public AnnotatedMembersNode(@NotNull Module module, @NotNull T member, SimpleNode parent) {
    super(parent, member);

    myModule = module;
    myMember = member;

    setPlainText(member.getName());
    setUniformIcon(member.getIcon(Iconable.ICON_FLAG_READ_STATUS));
  }

  @Override
  protected void doUpdate() {
    PsiClass psiClass = myMember.getContainingClass();

    clearColoredText();
    setNodeText(myMember.getName(), psiClass != null ? psiClass.getQualifiedName() : null, !myMember.isValid());

    if (psiClass != null && !myMember.equals(psiClass)) {
      String qualifiedName = psiClass.getName();
      if (qualifiedName != null) {
        addColoredFragment(" (" + qualifiedName + ")", SimpleTextAttributes.GRAY_ITALIC_ATTRIBUTES);
      }
    }
  }

  public SimpleNode[] getChildren() {
    return new SimpleNode[0];
  }

  @Override
  public Object[] getEqualityObjects() {
    return new Object[]{myMember};
  }

  //public static class ClassNode extends AnnotatedMembersNode<PsiClass> {
  //  public ClassNode(@NotNull Module module, @NotNull PsiClass member, SimpleNode parent) {
  //    super(module, member, parent);
  //  }
  //}
  //
  //public static class MethodNode extends AnnotatedMembersNode<PsiMethod> {
  //  public MethodNode(@NotNull Module module, @NotNull PsiMethod member, SimpleNode parent) {
  //    super(module, member, parent);
  //  }
  //}
}