package com.intellij.spring.model.actions.generate;

import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.MemberChooserObject;
import com.intellij.codeInsight.generation.MemberChooserObjectBase;
import com.intellij.codeInsight.generation.PsiElementMemberChooserObject;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBeanPointer;
import org.jetbrains.annotations.NotNull;

public class SpringBeanClassMember extends MemberChooserObjectBase implements ClassMember {
  private final SpringBeanPointer mySpringBean;

  public SpringBeanClassMember(final SpringBeanPointer springBean) {
    super(SpringUtils.getPresentationBeanName(springBean), springBean.getBeanIcon());
    mySpringBean = springBean;
  }

  public MemberChooserObject getParentNodeDelegate() {
    return new SpringFileMemberChooserObjectBase(getSpringBean().getContainingFile());
  }

  public SpringBeanPointer getSpringBean() {
    return mySpringBean;
  }

  private static class SpringFileMemberChooserObjectBase extends PsiElementMemberChooserObject {

    public SpringFileMemberChooserObjectBase(@NotNull final PsiFile psiFile) {
      super(psiFile, psiFile.getName(), psiFile instanceof XmlFile ?  SpringIcons.CONFIG_FILE: SpringIcons.JAVA_CONFIG_FILE);
    }

  }
}
