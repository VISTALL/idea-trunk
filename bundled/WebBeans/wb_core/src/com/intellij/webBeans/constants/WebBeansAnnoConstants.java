package com.intellij.webBeans.constants;

import org.jetbrains.annotations.NonNls;

public interface WebBeansAnnoConstants {

  // *** javax.annotation ***
  @NonNls String NON_BINDING_ANNOTATION = "javax.annotation.NonBinding";

  @NonNls String STEREOTYPE_ANNOTATION = "javax.annotation.Stereotype";

  @NonNls String NAMED_ANNOTATION = "javax.annotation.Named";

  // *** javax.context ***
  @NonNls String REQUEST_SCOPED_ANNOTATION = "javax.context.RequestScoped";

  @NonNls String SCOPE_TYPE_ANNOTATION = "javax.context.ScopeType";

  @NonNls String SESSION_SCOPED_ANNOTATION = "javax.context.SessionScoped";

  @NonNls String DEPENDENT_ANNOTATION = "javax.context.Dependent";

  @NonNls String CONVERSATION_SCOPED_ANNOTATION = "javax.context.ConversationScoped";

  @NonNls String APPLICATION_SCOPED_ANNOTATION = "javax.context.ApplicationScoped";

  // *** javax.decorator ***
  @NonNls String DECORATOR_ANNOTATION = "javax.decorator.Decorator";

  @NonNls String DECORATES_ANNOTATION = "javax.decorator.Decorates";

  // *** javax.event ***
  @NonNls String AFTER_TRANSACTION_COMPLETION_ANNOTATION = "javax.event.AfterTransactionCompletion";

  @NonNls String BEFORE_TRANSACTION_COMPLETION_ANNOTATION = "javax.event.BeforeTransactionCompletion";

  @NonNls String FIRES_ANNOTATION = "javax.event.Fires";

  @NonNls String AFTER_TRANSACTION_SUCCESS_ANNOTATION = "javax.event.AfterTransactionSuccess";

  @NonNls String IF_EXISTS_ANNOTATION = "javax.event.IfExists";

  @NonNls String AFTER_TRANSACTION_FAILURE_ANNOTATION = "javax.event.AfterTransactionFailure";

  @NonNls String OBSERVES_ANNOTATION = "javax.event.Observes";

  // *** javax.inject ***
  @NonNls String INITIALIZER_ANNOTATION = "javax.inject.Initializer";

  @NonNls String SPECIALIZES_ANNOTATION = "javax.inject.Specializes";

  @NonNls String PRODUCES_ANNOTATION = "javax.inject.Produces";

  @NonNls String OBTAINS_ANNOTATION = "javax.inject.Obtains";

  @NonNls String NEW_ANNOTATION = "javax.inject.New";

  @NonNls String DEPLOYMENT_TYPE_ANNOTATION = "javax.inject.DeploymentType";

  @NonNls String STANDARD_ANNOTATION = "javax.inject.Standard";

  @NonNls String CURRENT_ANNOTATION = "javax.inject.Current";

  @NonNls String PRODUCTION_ANNOTATION = "javax.inject.Production";

  @NonNls String DISPOSES_ANNOTATION = "javax.inject.Disposes";

  @NonNls String BINDING_TYPE_ANNOTATION = "javax.inject.BindingType";

  @NonNls String REALIZES_ANNOTATION = "javax.inject.Realizes";

  // *** javax.interceptor ***
  @NonNls String INTERCEPTOR_ANNOTATION = "javax.interceptor.Interceptor";

  @NonNls String INTERCEPTOR_BINDING_TYPE_ANNOTATION = "javax.interceptor.InterceptorBindingType";

  // *** javax.webbeans ***
  @NonNls String MODEL_ANNOTATION = "javax.webbeans.Model";
}
