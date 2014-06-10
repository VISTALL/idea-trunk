/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring;

import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.model.xml.aop.AopConfig;
import com.intellij.spring.model.xml.aop.AspectjAutoproxy;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.ListOrSet;
import com.intellij.spring.model.xml.context.*;
import com.intellij.spring.model.xml.jee.JndiLookup;
import com.intellij.spring.model.xml.jee.LocalSlsb;
import com.intellij.spring.model.xml.jee.RemoteSlsb;
import com.intellij.spring.model.xml.jms.JcaListenerContainer;
import com.intellij.spring.model.xml.jms.Listener;
import com.intellij.spring.model.xml.jms.ListenerContainer;
import com.intellij.spring.model.xml.lang.BeanShellScript;
import com.intellij.spring.model.xml.lang.GroovyScript;
import com.intellij.spring.model.xml.lang.JRubyScript;
import com.intellij.spring.model.xml.util.*;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author peter
 */
public class SpringDefaultDomExtender {
  private SpringDefaultDomExtender() {
  }

  public static class BeansExtender extends DomExtender<Beans> {
    public void registerExtensions(@NotNull final Beans element, @NotNull final DomExtensionsRegistrar registrar) {
      registerDefaultBeanExtensions(registrar);

      registrar.registerCollectionChildrenExtension(new XmlName("config", SpringConstants.AOP_NAMESPACE_KEY), AopConfig.class);
      registrar.registerFixedNumberChildExtension(new XmlName(SpringConstants.ASPECTJ_AUTOPROXY, SpringConstants.AOP_NAMESPACE_KEY), AspectjAutoproxy.class);
    }
  }

  public static class ListOrSetExtender extends DomExtender<ListOrSet> {
    public void registerExtensions(@NotNull final ListOrSet element, @NotNull final DomExtensionsRegistrar registrar) {
      registerDefaultBeanExtensions(registrar);
    }
  }

  private static void registerDefaultBeanExtensions(final DomExtensionsRegistrar registrar) {
    registrar.registerCollectionChildrenExtension(new XmlName("map", SpringConstants.UTIL_NAMESPACE_KEY), UtilMap.class);
    registrar.registerCollectionChildrenExtension(new XmlName("list", SpringConstants.UTIL_NAMESPACE_KEY), UtilList.class);
    registrar.registerCollectionChildrenExtension(new XmlName("set", SpringConstants.UTIL_NAMESPACE_KEY), UtilSet.class);
    registrar.registerCollectionChildrenExtension(new XmlName("properties", SpringConstants.UTIL_NAMESPACE_KEY), UtilProperties.class);
    registrar.registerCollectionChildrenExtension(new XmlName("constant", SpringConstants.UTIL_NAMESPACE_KEY), SpringConstant.class);
    registrar.registerCollectionChildrenExtension(new XmlName("property-path", SpringConstants.UTIL_NAMESPACE_KEY), PropertyPath.class);

    registrar.registerCollectionChildrenExtension(new XmlName("property-placeholder", SpringConstants.CONTEXT_NAMESPACE_KEY), PropertyPlaceholder.class);
    registrar.registerCollectionChildrenExtension(new XmlName("load-time-weaver", SpringConstants.CONTEXT_NAMESPACE_KEY), LoadTimeWeaver.class);
    registrar.registerCollectionChildrenExtension(new XmlName("component-scan", SpringConstants.CONTEXT_NAMESPACE_KEY), ComponentScan.class);
    registrar.registerCollectionChildrenExtension(new XmlName("filter", SpringConstants.CONTEXT_NAMESPACE_KEY), Filter.class);
    registrar.registerCollectionChildrenExtension(new XmlName("annotation-config", SpringConstants.CONTEXT_NAMESPACE_KEY), AnnotationConfig.class);

    registrar.registerCollectionChildrenExtension(new XmlName("jndi-lookup", SpringConstants.JEE_NAMESPACE_KEY), JndiLookup.class);
    registrar.registerCollectionChildrenExtension(new XmlName("local-slsb", SpringConstants.JEE_NAMESPACE_KEY), LocalSlsb.class);
    registrar.registerCollectionChildrenExtension(new XmlName("remote-slsb", SpringConstants.JEE_NAMESPACE_KEY), RemoteSlsb.class);

    registrar.registerCollectionChildrenExtension(new XmlName("groovy", SpringConstants.LANG_NAMESPACE_KEY), GroovyScript.class);
    registrar.registerCollectionChildrenExtension(new XmlName("jruby", SpringConstants.LANG_NAMESPACE_KEY), JRubyScript.class);
    registrar.registerCollectionChildrenExtension(new XmlName("bsh", SpringConstants.LANG_NAMESPACE_KEY), BeanShellScript.class);

    // jms schema
    registrar.registerCollectionChildrenExtension(new XmlName("listener", SpringConstants.JMS_NAMESPACE_KEY), Listener.class);
    registrar.registerCollectionChildrenExtension(new XmlName("listener", SpringConstants.JMS_NAMESPACE_KEY), ListenerContainer.class);
    registrar.registerCollectionChildrenExtension(new XmlName("listener", SpringConstants.JMS_NAMESPACE_KEY), JcaListenerContainer.class);
    
  }

}
