package com.intellij.seam.el;

import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.jsf.model.FacesDomModel;
import com.intellij.jsf.model.FacesDomModelManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.impl.source.jsp.el.ELContextProvider;
import com.intellij.psi.impl.source.jsp.el.impl.CustomJsfVariableResolverProvider;
import com.intellij.psi.jsp.JspImplicitVariable;
import com.intellij.seam.jsf.JsfContextBeansUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class SeamELContextProvider implements ELContextProvider {
  private final PsiElement myHost;

  public SeamELContextProvider(final PsiElement host) {
    myHost = host;
  }

  @Nullable
  public Iterator<? extends PsiVariable> getTopLevelElVariables(@Nullable final String nameHint) {
    List<JspImplicitVariable> seamVariables = JsfContextBeansUtils.getJspImplicitVariables(nameHint, myHost.getContainingFile());

    // add custom variables (spring, etc.)
    Module module = ModuleUtil.findModuleForPsiElement(myHost.getContainingFile());
    if (module != null) {
      for (WebFacet webFacet : WebFacet.getInstances(module)) {
        for (FacesDomModel facesDomModel : FacesDomModelManager.getInstance(module.getProject()).getAllModels(webFacet)) {
          String variableResolverClass = facesDomModel.getFacesConfig().getApplication().getVariableResolver().getStringValue();
          if (!StringUtil.isEmptyOrSpaces(variableResolverClass)) {
            final CustomJsfVariableResolverProvider[] providers = Extensions.getExtensions(CustomJsfVariableResolverProvider.EP_NAME);
            for (CustomJsfVariableResolverProvider provider : providers) {
              if (provider.acceptVariableResolver(variableResolverClass)) {
                provider.addVars(seamVariables, module);
              }
            }
          }
        }
      }

    }

    return seamVariables.iterator();
  }

  public boolean acceptsGetMethodForLastReference(final PsiMethod getter) {
    return true;
  }

  public boolean acceptsSetMethodForLastReference(final PsiMethod setter) {
    return false;
  }

  public boolean acceptsNonPropertyMethodForLastReference(final PsiMethod method) {
    if (isObjectClassMethod(method)) return false;

    return true;
  }

  private static boolean isObjectClassMethod(final PsiMethod method) {
    return CommonClassNames.JAVA_LANG_OBJECT.equals(method.getContainingClass().getQualifiedName());
  }
}
