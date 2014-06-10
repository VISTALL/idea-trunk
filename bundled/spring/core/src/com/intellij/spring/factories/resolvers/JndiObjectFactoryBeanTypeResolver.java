package com.intellij.spring.factories.resolvers;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.jee.JndiLookup;
import com.intellij.spring.model.xml.jee.SpringJeeElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JndiObjectFactoryBeanTypeResolver extends AbstractJeeSchemaTypeResolver {
  @NonNls private static final String FACTORY_CLASS = "org.springframework.jndi.JndiObjectFactoryBean";

  @NonNls private static final String[] myProperties = new String[] {"proxyInterface","expectedType"};
  
  @NotNull
  protected Set<String> getJeeObjectType(final CommonSpringBean context) {
    Set<String> result = new HashSet<String>();
    if (context instanceof SpringJeeElement) {
      final JndiLookup jndiLookup = (JndiLookup)context;
      if (StringUtil.isNotEmpty(jndiLookup.getProxyInterface().getStringValue())) {
        result.add(jndiLookup.getProxyInterface().getStringValue());
      } else if (StringUtil.isNotEmpty(jndiLookup.getExpectedType().getStringValue())) {
        result.add(jndiLookup.getExpectedType().getStringValue());
      }
    }
    return result;
  }

  protected List<String> getProperties() {
    return Arrays.asList(myProperties);
  }

  protected List<String> getFactoryClasses() {
    return Collections.singletonList(FACTORY_CLASS);  
  }
}
