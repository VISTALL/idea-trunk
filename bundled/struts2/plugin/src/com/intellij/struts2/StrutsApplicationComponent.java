/*
 * Copyright 2009 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2;

import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.struts2.dom.params.Param;
import com.intellij.struts2.dom.struts.*;
import com.intellij.struts2.dom.struts.action.*;
import com.intellij.struts2.dom.struts.constant.*;
import com.intellij.struts2.dom.struts.impl.*;
import com.intellij.struts2.dom.struts.impl.path.StrutsPathReferenceConverterImpl;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.struts2.dom.validator.Field;
import com.intellij.struts2.dom.validator.FieldValidator;
import com.intellij.struts2.dom.validator.Message;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.struts2.dom.validator.config.ValidatorConfigResolveConverter;
import com.intellij.struts2.dom.validator.impl.ValidatorConfigResolveConverterImpl;
import com.intellij.struts2.facet.StrutsFacetType;
import com.intellij.util.Icons;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Application-level support.
 * <p/>
 * <ul>
 * <li>StrutsFacet</li>
 * <li>DOM Icons/Presentation/Converters</li>
 * </ul>
 *
 * @author Yann C&eacute;bron
 */
public class StrutsApplicationComponent implements ApplicationComponent {

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Struts2ApplicationComponent";
  }

  public StrutsApplicationComponent(final ConverterManager converterManager) {
    converterManager.registerConverterImplementation(ConstantNameConverter.class,
                                                     new ConstantNameConverterImpl());
    converterManager.registerConverterImplementation(ConstantValueConverter.class,
                                                     new ConstantValueConverterImpl());
    converterManager.registerConverterImplementation(ExtendableClassConverter.class,
                                                     new ExtendableClassConverterImpl());
    converterManager.registerConverterImplementation(StrutsPackageExtendsResolveConverter.class,
                                                     new StrutsPackageExtendsResolveConverterImpl());
    converterManager.registerConverterImplementation(IncludeFileResolvingConverter.class,
                                                     new IncludeFileResolvingConverterImpl());
    converterManager.registerConverterImplementation(ResultTypeResolvingConverter.class,
                                                     new ResultTypeResolvingConverterImpl());
    converterManager.registerConverterImplementation(InterceptorRefResolveConverter.class,
                                                     new InterceptorRefResolveConverterImpl());
    converterManager.registerConverterImplementation(DefaultInterceptorRefResolveConverter.class,
                                                     new DefaultInterceptorRefResolveConverterImpl());
    converterManager.registerConverterImplementation(StrutsPathReferenceConverter.class,
                                                     new StrutsPathReferenceConverterImpl());
    converterManager.registerConverterImplementation(UnknownHandlerRefConverter.class,
                                                     new UnknownHandlerRefConverterImpl());

    converterManager.registerConverterImplementation(ValidatorConfigResolveConverter.class,
                                                     new ValidatorConfigResolveConverterImpl());
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(StrutsFacetType.INSTANCE);

    registerStrutsDomPresentation();
    registerValidationDomPresentation();

    registerDocumentationProviders();
  }

  public void disposeComponent() {
  }

  /**
   * Provides display name for subclass(es) of given DomElement-type.
   *
   * @param <T> DomElement-type to provide names for.
   */
  private abstract static class TypedNameProvider<T extends DomElement> {

    private final Class<T> clazz;

    private TypedNameProvider(final Class<T> clazz) {
      this.clazz = clazz;
    }

    private Class<T> getClazz() {
      return clazz;
    }

    @Nullable
    protected abstract String getDisplayName(T t);

  }

  /**
   * Provides registry and mapping for multiple {@link TypedNameProvider}s.
   */
  private static class TypedNameProviderRegistry implements NullableFunction<Object, String> {

    private final Map<Class, TypedNameProvider> typedNameProviderSet = new HashMap<Class, TypedNameProvider>();

    private void addTypedNameProvider(final TypedNameProvider nameProvider) {
      typedNameProviderSet.put(nameProvider.getClazz(), nameProvider);
    }

    public String fun(final Object o) {
      for (final Map.Entry<Class, TypedNameProvider> entry : typedNameProviderSet.entrySet()) {
        if (entry.getKey().isAssignableFrom(o.getClass())) {
          //noinspection unchecked
          return entry.getValue().getDisplayName((DomElement) o);
        }
      }

      return null;
    }

  }

  private static void registerStrutsDomPresentation() {
    final TypedNameProviderRegistry nameProviderRegistry = new TypedNameProviderRegistry();

    // <struts>
    ElementPresentationManager.registerIcon(StrutsRoot.class, StrutsIcons.STRUTS_CONFIG_FILE_ICON);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<StrutsRoot>(StrutsRoot.class) {
      protected String getDisplayName(final StrutsRoot strutsRoot) {
        return DomUtil.getFile(strutsRoot).getName();
      }
    });


    // <exception-mapping>
    ElementPresentationManager.registerIcon(ExceptionMapping.class, StrutsIcons.EXCEPTION_MAPPING);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<ExceptionMapping>(ExceptionMapping.class) {
      protected String getDisplayName(final ExceptionMapping exceptionMapping) {
        final PsiClass exceptionClass = exceptionMapping.getExceptionClass().getValue();
        if (exceptionClass != null) {
          return exceptionClass.getName();
        }
        return exceptionMapping.getName().getStringValue();
      }
    });

    // global <exception-mapping>
    ElementPresentationManager.registerIcon(GlobalExceptionMapping.class, StrutsIcons.GLOBAL_EXCEPTION_MAPPING);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<GlobalExceptionMapping>(GlobalExceptionMapping.class) {
      protected String getDisplayName(final GlobalExceptionMapping globalExceptionMapping) {
        final PsiClass exceptionClass = globalExceptionMapping.getExceptionClass().getValue();
        if (exceptionClass != null) {
          return exceptionClass.getName();
        }
        return globalExceptionMapping.getName().getStringValue();
      }
    });

    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<InterceptorRef>(InterceptorRef.class) {
      protected String getDisplayName(final InterceptorRef interceptorRef) {
        return interceptorRef.getName().getStringValue();
      }
    });

    ElementPresentationManager.registerIcon(DefaultInterceptorRef.class, StrutsIcons.DEFAULT_INTERCEPTOR_REF);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<DefaultInterceptorRef>(DefaultInterceptorRef.class) {
      protected String getDisplayName(final DefaultInterceptorRef defaultInterceptorRef) {
        return defaultInterceptorRef.getName().getStringValue();
      }
    });

    // <include>
    ElementPresentationManager.registerIcon(Include.class, StrutsIcons.INCLUDE);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Include>(Include.class) {
      protected String getDisplayName(final Include include) {
        return include.getFile().getStringValue();
      }
    });

    // <result>
    ElementPresentationManager.registerIcon(Result.class, StrutsIcons.RESULT);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Result>(Result.class) {
      protected String getDisplayName(final Result result) {
        final String resultName = result.getName().getStringValue();
        return resultName != null ? resultName : Result.DEFAULT_NAME;
      }
    });

    // <global-result>
    ElementPresentationManager.registerIcon(GlobalResult.class, StrutsIcons.GLOBAL_RESULT);
    TypeNameManager.registerTypeName(GlobalResult.class, "global result");
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<GlobalResult>(GlobalResult.class) {
      protected String getDisplayName(final GlobalResult globalResult) {
        final String globalResultName = globalResult.getName().getStringValue();
        return globalResultName != null ? globalResultName : Result.DEFAULT_NAME;
      }
    });

    // <default-action-ref>
    ElementPresentationManager.registerIcon(DefaultActionRef.class, StrutsIcons.DEFAULT_ACTION_REF);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<DefaultActionRef>(DefaultActionRef.class) {
      protected String getDisplayName(final DefaultActionRef defaultActionRef) {
        return defaultActionRef.getName().getStringValue();
      }
    });

    // <default-class-ref>
    ElementPresentationManager.registerIcon(DefaultClassRef.class, StrutsIcons.DEFAULT_CLASS_REF);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<DefaultClassRef>(DefaultClassRef.class) {
      protected String getDisplayName(final DefaultClassRef defaultClassRef) {
        return defaultClassRef.getDefaultClass().getStringValue();
      }
    });

    // register central name provider
    ElementPresentationManager.registerNameProvider(nameProviderRegistry);

    ElementPresentationManager.registerIcon(Action.class, StrutsIcons.ACTION);
    ElementPresentationManager.registerIcon(Bean.class, StrutsIcons.BEAN);
    ElementPresentationManager.registerIcon(Constant.class, Icons.PARAMETER_ICON);
    ElementPresentationManager.registerIcon(Interceptor.class, StrutsIcons.INTERCEPTOR);
    ElementPresentationManager.registerIcon(InterceptorStack.class, StrutsIcons.INTERCEPTOR_STACK);
    ElementPresentationManager.registerIcon(Param.class, StrutsIcons.PARAM);
    ElementPresentationManager.registerIcon(ResultType.class, StrutsIcons.RESULT_TYPE);
    ElementPresentationManager.registerIcon(StrutsPackage.class, StrutsIcons.PACKAGE);
  }

  private static void registerValidationDomPresentation() {
    final TypedNameProviderRegistry nameProviderRegistry = new TypedNameProviderRegistry();

    ElementPresentationManager.registerIcon(ValidatorConfig.class, StrutsIcons.VALIDATOR);

    ElementPresentationManager.registerIcon(Validators.class, StrutsIcons.VALIDATION_CONFIG_FILE_ICON);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Validators>(Validators.class) {
      protected String getDisplayName(final Validators validators) {
        return DomUtil.getFile(validators).getName();
      }
    });

    // <field>
    ElementPresentationManager.registerIcon(Field.class, Icons.FIELD_ICON);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Field>(Field.class) {
      protected String getDisplayName(final Field field) {
        return field.getName().getStringValue();
      }
    });

    // <field-validator>
    ElementPresentationManager.registerIcon(FieldValidator.class, StrutsIcons.VALIDATOR);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<FieldValidator>(FieldValidator.class) {
      protected String getDisplayName(final FieldValidator fieldValidator) {
        final ValidatorConfig validatorConfig = fieldValidator.getType().getValue();
        return validatorConfig != null ? validatorConfig.getName().getStringValue() : null;
      }
    });

    // <message>
    ElementPresentationManager.registerIcon(Message.class, StrutsIcons.MESSAGE);
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Message>(Message.class) {
      protected String getDisplayName(final Message message) {
        final String key = message.getKey().getStringValue();
        return StringUtil.isNotEmpty(key) ? key : message.getValue();
      }
    });

    // register central name provider
    ElementPresentationManager.registerNameProvider(nameProviderRegistry);
  }

  private static void registerDocumentationProviders() {
    ElementPresentationManager.registerDocumentationProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof Action) {
          final Action action = (Action) o;
          final StrutsPackage strutsPackage = action.getStrutsPackage();

          final DocumentationBuilder builder = new DocumentationBuilder();
          builder.addLine("Action", action.getName().getStringValue())
              .addLine("Class", action.getActionClass().getStringValue())
              .addLine("Method", action.getMethod().getStringValue())
              .addLine("Package", strutsPackage.getName().getStringValue())
              .addLine("Namespace", strutsPackage.getNamespace().getStringValue());

          return builder.getText();
        }

        if (o instanceof Result) {
          final Result result = (Result) o;
          final PathReference pathReference = result.getValue();
          final String displayPath = pathReference != null ? pathReference.getPath() : "???";
          final ResultType resultType = result.getEffectiveResultType();
          final String resultTypeValue = resultType != null ? resultType.getName().getStringValue() : "???";

          final DocumentationBuilder builder = new DocumentationBuilder();
          builder.addLine("Path", displayPath)
              .addLine("Type", resultTypeValue);
          return builder.getText();
        }

        return null;
      }
    });
  }

  /**
   * Builds HTML-table based descriptions for use in documentation, tooltips.
   *
   * @author Yann C&eacute;bron
   */
  private static class DocumentationBuilder {

    @NonNls
    private final StringBuilder builder = new StringBuilder("<html><table>");

    /**
     * Adds a labeled content line.
     *
     * @param label   Content description.
     * @param content Content text, {@code null} or empty text will be replaced with '-'.
     * @return this instance.
     */
    private DocumentationBuilder addLine(@NotNull @NonNls final String label, @Nullable @NonNls final String content) {
      builder.append("<tr><td><strong>").append(label).append(":</strong></td>")
          .append("<td>").append(StringUtil.isNotEmpty(content) ? content : "-").append("</td></tr>");
      return this;
    }

    private String getText() {
      builder.append("</table></html>");
      return builder.toString();
    }

  }
}
