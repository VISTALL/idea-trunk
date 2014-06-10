package com.intellij.spring.model.gotoSymbol;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.facet.SpringFacet;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.util.containers.HashSet;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * User: Sergey.Vasiliev
 */
public class GotoSpringBeanProvider extends GoToSymbolProvider {

  protected void addNames(@NotNull final Module module, final Set<String> result) {
    for (SpringModel springModel : SpringManager.getInstance(module.getProject()).getAllModels(module)) {
      for (SpringBaseBeanPointer pointer : springModel.getAllCommonBeans()) {
        result.addAll(getNames(pointer));
      }
    }
  }

  private void addNonNull(Set<String> result, String aliase) {
    if (!StringUtil.isEmptyOrSpaces(aliase)) {
      result.add(aliase);
    }
  }

  protected void addItems(@NotNull final Module module, final String name, final List<NavigationItem> result) {
    for (SpringModel springModel : SpringManager.getInstance(module.getProject()).getAllModels(module)) {
      for (SpringBaseBeanPointer pointer : springModel.getAllCommonBeans()) {
        final Set<String> beanNames = getNames(pointer);
        if (beanNames.contains(name)) {
          final PsiElement element = pointer.getSpringBean().getIdentifyingPsiElement();

          if (element != null) {
            result.add(createNavigationItem(element, name, pointer.getBeanIcon()));
          }
        }
      }
    }
  }

  private Set<String> getNames(SpringBaseBeanPointer pointer) {
    Set<String> names = new HashSet<String>();
    addNonNull(names, pointer.getName());

    for (String aliase : pointer.getAliases()) {
      addNonNull(names, aliase);
    }
    return names;
  }

  protected boolean acceptModule(final Module module) {
    return SpringFacet.getInstance(module) != null;
  }
}

