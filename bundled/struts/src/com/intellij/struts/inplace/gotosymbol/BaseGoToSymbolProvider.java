package com.intellij.struts.inplace.gotosymbol;

import com.intellij.openapi.module.Module;
import com.intellij.javaee.web.facet.WebFacet;
import com.intellij.util.xml.model.gotosymbol.GoToSymbolProvider;

public abstract class BaseGoToSymbolProvider extends GoToSymbolProvider {
    protected boolean acceptModule(final Module module) {
    return !WebFacet.getInstances(module).isEmpty();
  }
}
