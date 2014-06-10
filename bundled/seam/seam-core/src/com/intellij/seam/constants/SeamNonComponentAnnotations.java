package com.intellij.seam.constants;

import org.jetbrains.annotations.NonNls;

/**
 * User: Sergey.Vasiliev
 */
public interface SeamNonComponentAnnotations {
  // Interceptors
  @NonNls String AROUND = "org.jboss.seam.annotations.Around";
  @NonNls String WITHIN = "org.jboss.seam.annotations.Within";

  // Meta-annotations for databinding
  @NonNls String DATA_BINDER_CLASS_ANNOTATION = "org.jboss.seam.annotations.DataBinderClass";
  @NonNls String DATA_SELECTOR_CLASS_ANNOTATION = "org.jboss.seam.annotations.DataSelectorClass";

  //Annotations for exceptions
  @NonNls String EXCEPTIONS_REDIRECT_ANNOTATION = "org.jboss.seam.annotations.exception.Redirect";
  @NonNls String EXCEPTIONS_HTTP_ERROR_ANNOTATION = "org.jboss.seam.annotations.exception.HttpError";

  // Annotations for Seam interceptors
  @NonNls String INTERCEPTOR_ANNOTATION = "org.jboss.seam.annotations.intercept.Interceptor";

  // Annotations for packaging
  @NonNls String NAMESPACE_ANNOTATION = "org.jboss.seam.annotations.Namespace";

  // Annotations for integrating with the servlet container
  @NonNls String FILTER_ANNOTATION = "org.jboss.seam.annotations.web.Filter";

  //Annotations for asynchronicity
   @NonNls String ASYNCHRONICITY_ASINCHRONOUS_ANNOTATION = "org.jboss.seam.annotations.async.Asynchronous";
   @NonNls String ASYNCHRONICITY_DURATION_ANNOTATION = "org.jboss.seam.annotations.async.Duration";
   @NonNls String ASYNCHRONICITY_EXPIRATION_ANNOTATION = "org.jboss.seam.annotations.async.Expiration";
   @NonNls String ASYNCHRONICITY_INTERVAL_DURATION_ANNOTATION = "org.jboss.seam.annotations.async.IntervalDuration";
  
}
