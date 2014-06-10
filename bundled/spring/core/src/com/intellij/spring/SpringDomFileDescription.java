/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.spring;

import com.intellij.javaee.model.xml.impl.RootBaseImpl;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.constants.SpringConstants;
import com.intellij.spring.impl.model.CustomBeanWrapperImpl;
import com.intellij.spring.impl.model.aop.*;
import com.intellij.spring.impl.model.beans.*;
import com.intellij.spring.impl.model.context.*;
import com.intellij.spring.impl.model.jee.JndiLookupImpl;
import com.intellij.spring.impl.model.jee.LocalSlsbImpl;
import com.intellij.spring.impl.model.jee.RemoteSlsbImpl;
import com.intellij.spring.impl.model.lang.LangBeanImpl;
import com.intellij.spring.impl.model.tx.TxAdviceImpl;
import com.intellij.spring.impl.model.tx.TxAnnotationDrivenImpl;
import com.intellij.spring.impl.model.util.*;
import com.intellij.spring.model.xml.CustomBeanWrapper;
import com.intellij.spring.model.xml.aop.*;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.spring.model.xml.context.*;
import com.intellij.spring.model.xml.jee.JndiLookup;
import com.intellij.spring.model.xml.jee.LocalSlsb;
import com.intellij.spring.model.xml.jee.RemoteSlsb;
import com.intellij.spring.model.xml.lang.LangBean;
import com.intellij.spring.model.xml.tx.Advice;
import com.intellij.spring.model.xml.tx.AnnotationDriven;
import com.intellij.spring.model.xml.util.*;
import com.intellij.util.NotNullFunction;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author peter
*/
public class SpringDomFileDescription extends DomFileDescription<Beans> {
  
  private static final List<String> SPRING_NAMESPACES = Arrays.asList(SpringConstants.BEANS_DTD_1, SpringConstants.BEANS_DTD_2, SpringConstants.BEANS_XSD);

  public SpringDomFileDescription() {
    super(Beans.class, "beans");
  }

  protected void initializeFileDescription() {

    registerImplementation(Beans.class, (Class)RootBaseImpl.class);
    registerImplementation(SpringBean.class, SpringBeanImpl.class);
    registerImplementation(SpringProperty.class, SpringPropertyImpl.class);
    registerImplementation(ConstructorArg.class, ConstructorArgImpl.class);
    registerImplementation(ReplacedMethod.class, ReplacedMethodImpl.class);
    registerImplementation(SpringValue.class, SpringValueImpl.class);
    registerImplementation(SpringEntry.class, SpringEntryImpl.class);
    registerImplementation(SpringKey.class, SpringKeyImpl.class);
    registerImplementation(ListOrSet.class, ListOrSetImpl.class);
    registerImplementation(UtilProperties.class, UtilPropertiesImpl.class);
    registerImplementation(UtilList.class, UtilListImpl.class);
    registerImplementation(UtilMap.class, UtilMapImpl.class);
    registerImplementation(UtilSet.class, UtilSetImpl.class);
    registerImplementation(SpringConstant.class, UtilConstantImpl.class);
    registerImplementation(PropertyPath.class, UtilPropertyPathImpl.class);

    registerImplementation(PropertyPlaceholder.class, PropertyPlaceholderImpl.class);
    registerImplementation(ComponentScan.class, ComponentScanImpl.class);
    registerImplementation(LoadTimeWeaver.class, LoadTimeWeaverImpl.class);
    registerImplementation(Filter.class, FilterImpl.class);
    registerImplementation(AnnotationConfig.class, AnnotationConfigImpl.class);

    registerImplementation(JndiLookup.class, JndiLookupImpl.class);
    registerImplementation(RemoteSlsb.class, RemoteSlsbImpl.class);
    registerImplementation(LocalSlsb.class, LocalSlsbImpl.class);

    registerImplementation(SpringPointcut.class, SpringPointcutImpl.class);
    registerImplementation(SpringAspect.class, SpringAspectImpl.class);
    registerImplementation(BasicAdvice.class, BasicAdviceImpl.class);
    registerImplementation(DeclareParents.class, DeclareParentsImpl.class);
    registerImplementation(AfterReturningAdvice.class, AfterReturningAdviceImpl.class);
    registerImplementation(Advisor.class, AdvisorImpl.class);
    registerImplementation(AspectjAutoproxy.class, AspectjAutoproxyImpl.class);

    registerImplementation(Advice.class, TxAdviceImpl.class);
    registerImplementation(AnnotationDriven.class, TxAnnotationDrivenImpl.class);

    registerImplementation(PNamespaceValue.class, PNamespaceValueImpl.class);
    registerImplementation(PNamespaceRefValue.class, PNamespaceRefValueImpl.class);
    registerImplementation(CustomBeanWrapper.class, CustomBeanWrapperImpl.class);

    registerImplementation(MetadataValue.class, MetadataValueImpl.class);
    registerImplementation(TypedBeanPointerAttribute.class, TypedBeanPointerAttributeImpl.class);
    
    registerImplementation(LangBean.class, LangBeanImpl.class);

    registerImplementation(SpringDomQualifier.class, SpringDomQualifierImpl.class);
    registerImplementation(SpringDomAttribute.class, SpringDomAttributeImpl.class);

    registerNamespacePolicy(SpringConstants.BEANS_NAMESPACE_KEY, new NotNullFunction<XmlTag, List<String>>() {
      @NotNull
      public List<String> fun(final XmlTag tag) {
        return SPRING_NAMESPACES;
      }
    });

    registerNamespacePolicy(SpringConstants.AOP_NAMESPACE_KEY, SpringConstants.AOP_NAMESPACE);
    registerNamespacePolicy(SpringConstants.JEE_NAMESPACE_KEY, SpringConstants.JEE_NAMESPACE);
    registerNamespacePolicy(SpringConstants.LANG_NAMESPACE_KEY, SpringConstants.LANG_NAMESPACE);
    registerNamespacePolicy(SpringConstants.TOOL_NAMESPACE_KEY, SpringConstants.TOOL_NAMESPACE);
    registerNamespacePolicy(SpringConstants.TX_NAMESPACE_KEY, SpringConstants.TX_NAMESPACE);
    registerNamespacePolicy(SpringConstants.UTIL_NAMESPACE_KEY, SpringConstants.UTIL_NAMESPACE);
    registerNamespacePolicy(SpringConstants.JMS_NAMESPACE_KEY, SpringConstants.JMS_NAMESPACE);
    registerNamespacePolicy(SpringConstants.CONTEXT_NAMESPACE_KEY, SpringConstants.CONTEXT_NAMESPACE);
    registerNamespacePolicy(SpringConstants.P_NAMESPACE_KEY, SpringConstants.P_NAMESPACE);
  }

  public static SpringDomFileDescription getInstance() {
    return ContainerUtil.findInstance(Extensions.getExtensions(EP_NAME), SpringDomFileDescription.class);
  }
}
