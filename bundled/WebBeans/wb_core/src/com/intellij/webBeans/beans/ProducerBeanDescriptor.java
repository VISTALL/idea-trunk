package com.intellij.webBeans.beans;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMember;
import com.intellij.webBeans.manager.WebBeansManager;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;

import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public abstract class ProducerBeanDescriptor<T extends PsiMember> extends AbstractWebBeanDescriptor<T> {

  @Override
  public PsiClass getDefaultDeploymentType() {
    PsiClass psiClass = getAnnotatedItem().getContainingClass();
    if (psiClass != null) {
      Set<WebBeanDescriptor> webBeanDescriptors = WebBeansManager.getService(getModule()).resolveWebBeanByType(psiClass);
      if (webBeanDescriptors.size() == 1) {
        return webBeanDescriptors.iterator().next().getDeploymentType(); // todo ???
      }
    }

    return getAnnotationClass(WebBeansAnnoConstants.PRODUCTION_ANNOTATION);
  }
}
