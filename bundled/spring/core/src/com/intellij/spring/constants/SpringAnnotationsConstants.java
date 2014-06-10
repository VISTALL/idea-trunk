package com.intellij.spring.constants;

import org.jetbrains.annotations.NonNls;

/**
 * User: Sergey.Vasiliev
 */
public interface SpringAnnotationsConstants {
  // java config
  @NonNls String JAVA_CONFIG_CONFIGURATION_ANNOTATION = "org.springframework.config.java.annotation.Configuration";
  @NonNls String JAVA_CONFIG_BEAN_ANNOTATION = "org.springframework.config.java.annotation.Bean";
  @NonNls String JAVA_CONFIG_EXTERNAL_BEAN_ANNOTATION = "org.springframework.config.java.annotation.ExternalBean";
  @NonNls String JAVA_CONFIG_SCOPED_PROXY_ANNOTATION = "org.springframework.config.java.annotation.aop.ScopedProxy";

  // java config in spring 3.0
  @NonNls String JAVA_SPRING_CONFIGURATION_ANNOTATION = "org.springframework.context.annotation.Configuration";
  @NonNls String JAVA_SPRING_BEAN_ANNOTATION = "org.springframework.context.annotation.Bean";

  // stereotypes
  @NonNls String COMPONENT_ANNOTATION = "org.springframework.stereotype.Component";
  @NonNls String CONTROLLER_ANNOTATION = "org.springframework.stereotype.Controller";
  @NonNls String REPOSITORY_ANNOTATION = "org.springframework.stereotype.Repository";
  @NonNls String SERVICE_ANNOTATION = "org.springframework.stereotype.Service";

  // spring 2.X
  @NonNls String AUTOWIRED_ANNOTATION = "org.springframework.beans.factory.annotation.Autowired";
  @NonNls String CONFIGURABLE_ANNOTATION = "org.springframework.beans.factory.annotation.Configurable";
  @NonNls String QUALIFIER_ANNOTATION = "org.springframework.beans.factory.annotation.Qualifier";
  @NonNls String REQUIRED_ANNOTATION = "org.springframework.beans.factory.annotation.Required";

  // javax annotations
  @NonNls String RESOURCE_ANNOTATION = "javax.annotation.Resource";
  @NonNls String PRE_DESTROY_ANNOTATION = "javax.annotation.PreDestroy";
  @NonNls String POST_CONSTRUCT_ANNOTATION = "javax.annotation.PostConstruct";

  @NonNls String CUSTOM_AUTOWIRE_CONFIGURER_CLASS = "org.springframework.beans.factory.annotation.CustomAutowireConfigurer";

  // predefined component annotations
  @NonNls String[] SPRING_COMPONENT_ANNOTATIONS =
    {SpringAnnotationsConstants.COMPONENT_ANNOTATION, SpringAnnotationsConstants.CONTROLLER_ANNOTATION,
      SpringAnnotationsConstants.REPOSITORY_ANNOTATION, SpringAnnotationsConstants.SERVICE_ANNOTATION,
      SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION, SpringAnnotationsConstants.JAVA_SPRING_CONFIGURATION_ANNOTATION};

}
