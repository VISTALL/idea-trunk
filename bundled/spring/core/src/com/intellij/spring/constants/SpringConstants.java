/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring.constants;

import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.Set;

public interface SpringConstants {
  @NonNls String AOP_NAMESPACE = "http://www.springframework.org/schema/aop";
  @NonNls String JEE_NAMESPACE = "http://www.springframework.org/schema/jee";
  @NonNls String LANG_NAMESPACE = "http://www.springframework.org/schema/lang";
  @NonNls String TOOL_NAMESPACE = "http://www.springframework.org/schema/tool";
  @NonNls String TX_NAMESPACE = "http://www.springframework.org/schema/tx";
  @NonNls String UTIL_NAMESPACE = "http://www.springframework.org/schema/util";
  @NonNls String JMS_NAMESPACE = "http://www.springframework.org/schema/jms";
  @NonNls String CONTEXT_NAMESPACE = "http://www.springframework.org/schema/context";
  @NonNls String P_NAMESPACE = "http://www.springframework.org/schema/p";
  @NonNls String BEANS_XSD = "http://www.springframework.org/schema/beans";

  @NonNls String BEANS_DTD_1 = "http://www.springframework.org/dtd/spring-beans.dtd";
  @NonNls String BEANS_DTD_2 = "http://www.springframework.org/dtd/spring-beans-2.0.dtd";
  @NonNls String BEANS_NAMESPACE_KEY = "Spring beans namespace key";

  @NonNls String BEANS_SCHEMALOCATION_FALLBACK = "http://www.springframework.org/schema/beans/spring-beans.xsd";
  @NonNls String TOOL_SCHEMALOCATION_FALLBACK = "http://www.springframework.org/schema/tool/spring-tool.xsd";

  @NonNls String AOP_NAMESPACE_KEY = "Spring AOP namespace key";
  @NonNls String JEE_NAMESPACE_KEY = "Spring JEE namespace key";
  @NonNls String LANG_NAMESPACE_KEY = "Spring Lang namespace key";
  @NonNls String TOOL_NAMESPACE_KEY = "Spring tool namespace key";
  @NonNls String TX_NAMESPACE_KEY = "Spring TX namespace key";
  @NonNls String UTIL_NAMESPACE_KEY = "Spring Util namespace key";
  @NonNls String JMS_NAMESPACE_KEY = "Spring Jms namespace key";
  @NonNls String CONTEXT_NAMESPACE_KEY = "Spring Context namespace key";
  @NonNls String P_NAMESPACE_KEY = "Spring p-namespace";

  @NonNls String SPRING_VERSION_CLASS = "org.springframework.core.SpringVersion";

  
  @NonNls Set<String> INSIDER_NAMESPACES = new THashSet<String>(Arrays.asList(AOP_NAMESPACE, JEE_NAMESPACE, UTIL_NAMESPACE,
                                                                       TX_NAMESPACE, LANG_NAMESPACE, TOOL_NAMESPACE,
                                                                       BEANS_XSD, P_NAMESPACE));
  @NonNls String ASPECTJ_AUTOPROXY = "aspectj-autoproxy";
  @NonNls String ASPECTJ_AUTOPROXY_BEAN_CLASS = "org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator";
}
