package com.intellij.webBeans.providers;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.jam.NamedWebBean;
import com.intellij.webBeans.jam.WebBeansJamModel;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class WebBeansGotoSymbolProvider extends GoToSymbolProvider {

  protected void addNames(@NotNull final Module module, final Set<String> result) {
    final List<NamedWebBean> webBeans = WebBeansJamModel.getModel(module).getNamedWebBeans();
    result.addAll(ContainerUtil.mapNotNull(webBeans, new Function<NamedWebBean, String>() {
      public String fun(NamedWebBean namedWebBean) {
        return namedWebBean.getName();
      }
    }));
  }

  protected void addItems(@NotNull final Module module, final String name, final List<NavigationItem> result) {
    for (NamedWebBean<?> namedWebBean : WebBeansJamModel.getModel(module).getNamedWebBeans()) {
      PsiNamedElement element = namedWebBean.getIdentifyingPsiElement();
      final String elementName = element.getName();
        if (elementName != null && elementName.trim().startsWith(name)) {
          final NavigationItem navigationItem = createNavigationItem(element, elementName, WebBeansIcons.WEB_BEAN);
          if (!result.contains(navigationItem)) {
            result.add(navigationItem);
          }
      }
    }
  }

  protected boolean acceptModule(final Module module) {
    return WebBeansCommonUtils.isModuleContainsWebBeansFacet(module);
  }
}

