package com.intellij.spring.usages;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.TypeSafeDataProvider;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageView;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.UsageGroupingRule;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SpringBeansGroupingRule implements UsageGroupingRule {
  private static final Logger LOG = Logger.getInstance("#com.intellij.spring.usages.SpringBeansGroupingRule");

  public UsageGroup groupUsage(Usage usage) {
    if (usage instanceof PsiElementUsage) {
      PsiElement psiElement = ((PsiElementUsage)usage).getElement();

      final PsiFile psiFile = psiElement.getContainingFile();
      final Project project = psiElement.getProject();
      if (psiFile instanceof XmlFile && SpringManager.getInstance(project).isSpringBeans((XmlFile)psiFile)) {
        final DomElement domElement = DomUtil.getDomElement(psiElement);
        if (domElement != null) {
          final DomSpringBean springBean = domElement.getParentOfType(DomSpringBean.class, false);
          if (springBean != null) {
            return new SpringBeansUsageGroup(springBean);
          }
        }
      }
    }
    return null;
  }

  private static class SpringBeansUsageGroup implements UsageGroup, TypeSafeDataProvider {
    private final String myName;
    private final DomSpringBean myBean;

    public SpringBeansUsageGroup(@NotNull DomSpringBean bean) {
      myBean = bean;
      final String beanName = bean.getPresentation().getElementName();
      myName = beanName == null ? SpringBundle.message("spring.bean.with.unknown.name") : beanName;

      update();
    }

    public void update() {
    }

    public boolean equals(final Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      final SpringBeansUsageGroup that = (SpringBeansUsageGroup)o;

      if (!myBean.equals(that.myBean)) return false;
      if (!myName.equals(that.myName)) return false;

      return true;
    }

    public int hashCode() {
      int result;
      result = myName.hashCode();
      result = 31 * result + myBean.hashCode();
      return result;
    }

    public Icon getIcon(boolean isOpen) {
      return SpringIcons.SPRING_BEAN_ICON;
    }

    @NotNull
    public String getText(UsageView view) {
      return myName;
    }

    @NotNull
    public DomSpringBean getBean() {
      return myBean;
    }

    public FileStatus getFileStatus() {
      return isValid() ? DomUtil.getFile(getBean()).getFileStatus() : null;
    }

    public boolean isValid() {
      return getBean().isValid();
    }

    public void navigate(boolean focus) throws UnsupportedOperationException {
      if (canNavigate()) {
        SpringUtils.navigate(myBean);
      }
    }

    public boolean canNavigate() {
      return isValid();
    }

    public boolean canNavigateToSource() {
      return canNavigate();
    }

    public int compareTo(UsageGroup usageGroup) {
      if (!(usageGroup instanceof SpringBeansUsageGroup)) {
        LOG.error("MethodUsageGroup expected but " + usageGroup.getClass() + " found");
      }

      return myName.compareTo(((SpringBeansUsageGroup)usageGroup).myName);
    }

    public void calcData(final DataKey key, final DataSink sink) {
      if (!isValid()) return;
      if (LangDataKeys.PSI_ELEMENT == key) {
        final XmlElement element = getPsiElement();
        if (element != null && element.isValid()) {
          sink.put(LangDataKeys.PSI_ELEMENT, element);
        }
      }
      if (UsageView.USAGE_INFO_KEY == key) {
        PsiElement element = getPsiElement();
        if (element != null && element.isValid()) {
          sink.put(UsageView.USAGE_INFO_KEY, new UsageInfo(element));
        }
      }
    }

    @Nullable
    private XmlElement getPsiElement() {
      return getBean().getXmlElement();
    }
  }
}
