package com.intellij.spring.factories.resolvers;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.jee.SpringEjb;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SpringEjbTypeResolver extends AbstractJeeSchemaTypeResolver {
  @NonNls private static final String[] myFactoryClasses = new String[]{"org.springframework.ejb.access.LocalStatelessSessionProxyFactoryBean",
    "org.springframework.ejb.access.SimpleRemoteStatelessSessionProxyFactoryBean"};
  @NonNls private static final String[] myProperties = new String[]{"businessInterface"};

  @NotNull
  protected Set<String> getJeeObjectType(final CommonSpringBean context) {
    if (context instanceof SpringEjb) {
      final SpringEjb springEjb = (SpringEjb)context;
      if (StringUtil.isNotEmpty(springEjb.getBusinessInterface().getStringValue())) {
        return Collections.singleton(springEjb.getBusinessInterface().getStringValue());
      }
    }
    return Collections.emptySet();
  }

  protected List<String> getProperties() {
    return Arrays.asList(myProperties);
  }

  protected List<String> getFactoryClasses() {
    return Arrays.asList(myFactoryClasses);
  }
}
