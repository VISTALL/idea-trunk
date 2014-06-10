package com.intellij.spring.model.actions.generate;

import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

public class SpringBeanGenerateProvider extends BasicSpringDomGenerateProvider<SpringBean> {

  public SpringBeanGenerateProvider(final String description, @NonNls String template) {
    super(description, SpringBean.class, template);
  }

  @Nullable
  protected DomElement getElementToNavigate(final SpringBean springBean) {
    return null;
  }
}
