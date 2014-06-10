package com.intellij.webBeans.jam.meta;

import com.intellij.jam.JamPsiAnnotationParameterMetaData;
import com.intellij.jam.JamService;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.meta.MetaDataContributor;
import com.intellij.psi.meta.MetaDataRegistrar;
import com.intellij.webBeans.jam.NamedWebBean;
import org.jetbrains.annotations.NotNull;

public class WebBeansMetaDataContributor implements MetaDataContributor {

  public void contributeMetaData(final MetaDataRegistrar registrar) {
    //registrar.registerMetaData(new AnnotationMetaDataFilter(WebBeansAnnoConstants.NAMED_ANNOTATION), NamedWebBeanMeta.class);
  }

  public static class NamedWebBeanMeta extends JamPsiAnnotationParameterMetaData<NamedWebBean> {

    @NotNull
    protected NamedWebBean getModelElement(final PsiModifierListOwner owner, final PsiAnnotation annotation) {
      return JamService.getJamService(owner.getProject()).getJamElement(NamedWebBean.class, owner);
    }
  }
}

