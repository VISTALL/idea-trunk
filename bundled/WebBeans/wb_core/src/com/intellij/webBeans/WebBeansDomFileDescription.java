package com.intellij.webBeans;

import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import com.intellij.webBeans.constants.WebBeansCommonConstants;
import com.intellij.webBeans.constants.WebBeansNamespaceConstants;
import com.intellij.webBeans.model.xml.WebBeans;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class WebBeansDomFileDescription extends DomFileDescription<WebBeans> {

  public WebBeansDomFileDescription() {
    super(WebBeans.class, WebBeansCommonConstants.WEB_BEANS_CONFIG_ROOT_TAG_NAME);
  }

  protected void initializeFileDescription() {
    registerNamespacePollicies();

    registerImplementations();
  }

  private void registerImplementations() {
    //registerImplementation(WebBeansDomComponent.class, WebBeansDomComponentImpl.class);
  }

  private void registerNamespacePollicies() {
    registerNamespacePolicy(WebBeansNamespaceConstants.WEB_BEANS_NAMESPACE_KEY, WebBeansNamespaceConstants.WEB_BEANS_NAMESPACE);
  }

  @NotNull
  @Override
  public List<String> getAllowedNamespaces(@NotNull final String namespaceKey, @NotNull final XmlFile file) {
    final List<String> stringList = super.getAllowedNamespaces(namespaceKey, file);
    return stringList.isEmpty() ? Collections.singletonList(namespaceKey) : stringList;
  }

}
