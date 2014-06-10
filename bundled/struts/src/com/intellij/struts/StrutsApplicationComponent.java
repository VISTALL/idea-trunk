/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
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

package com.intellij.struts;

import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizable;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RegExpValidator;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.refactoring.rename.RenameInputValidatorRegistry;
import com.intellij.struts.config.StrutsConfiguration;
import com.intellij.struts.core.JDOMClassExternalizer;
import com.intellij.struts.dom.SetProperty;
import com.intellij.struts.dom.converters.StrutsPathReferenceConverter;
import com.intellij.struts.dom.converters.StrutsPathReferenceConverterImpl;
import com.intellij.struts.dom.validator.*;
import com.intellij.struts.facet.StrutsFacetType;
import com.intellij.struts.highlighting.StrutsInspection;
import com.intellij.struts.highlighting.TilesInspection;
import com.intellij.struts.highlighting.ValidatorInspection;
import com.intellij.struts.inplace.inspections.ValidatorFormInspection;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.ConverterManager;
import com.intellij.util.xml.ElementPresentationManager;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents Struts support at application level.
 *
 * @author Dmitry Avdeev
 */
public class StrutsApplicationComponent
  implements ApplicationComponent, JDOMExternalizable, InspectionToolProvider {

  private static final Logger LOG = Logger.getInstance(StrutsApplicationComponent.class.getName());

  public StrutsApplicationComponent(ConverterManager converterManager) {
    converterManager.registerConverterImplementation(StrutsPathReferenceConverter.class, new StrutsPathReferenceConverterImpl());
  }

  @NotNull
  public String getComponentName() {
    return StrutsConstants.PLUGIN_NAME;
  }

  public void initComponent() {

    registerPresentations();
    registerLiveTemplates();

    // validate <action> "path"-attribute on Refactor->Rename
    final RegExpValidator validator = new RegExpValidator("/[\\d\\w\\_\\.\\-/]+");
    RenameInputValidatorRegistry.getInstance().registerInputValidator(
      XmlPatterns.xmlTag().withNamespace(StrutsConstants.STRUTS_DTDS).withLocalName("action"), new RenameInputValidator() {
      public boolean isInputValid(final String newName, final PsiElement element, final ProcessingContext context) {
        return validator.value(newName);
      }
    });

    FacetTypeRegistry.getInstance().registerFacetType(StrutsFacetType.INSTANCE);
  }

  private void registerLiveTemplates() {
    final TemplateSettings settings = TemplateSettings.getInstance();
    try {
      final Document document = JDOMUtil.loadDocument(getClass().getResourceAsStream("/liveTemplates/struts.xml"));
      settings.readHiddenTemplateFile(document);
    }
    catch (Exception e) {
      LOG.error("Can't load Struts live templates", e);
    }
  }

  public void disposeComponent() {
  }

  /**
   * Writes struts configuration
   *
   * TODO switch to PersistentStateComponent
   * @param element JDOM Element.
   * @throws WriteExternalException
   */
  public void writeExternal(Element element) throws WriteExternalException {
    JDOMClassExternalizer.writeExternal(StrutsConfiguration.getInstance(), element);
  }

  public void readExternal(Element element) throws InvalidDataException {
    JDOMClassExternalizer.readExternal(StrutsConfiguration.getInstance(), element);
  }

  private static void registerPresentations() {
    ElementPresentationManager
      .registerIcon(FormValidation.class, StrutsIcons.getIcon("validator/ValidatorConfig.png"));

    ElementPresentationManager.registerIcon(Global.class, StrutsIcons.getIcon("validator/Global.png"));
    ElementPresentationManager.registerIcon(Validator.class, StrutsIcons.VALIDATOR_ICON);
    ElementPresentationManager.registerIcon(Constant.class, StrutsIcons.getIcon("validator/Constant.png"));

    ElementPresentationManager.registerIcon(Formset.class, StrutsIcons.getIcon("validator/Formset.png"));
    ElementPresentationManager.registerIcon(Form.class, StrutsIcons.getIcon("validator/Form.png"));
    ElementPresentationManager.registerIcon(Field.class, StrutsIcons.getIcon("FormProperty.png"));

    ElementPresentationManager.registerIcon(Msg.class, StrutsIcons.getIcon("validator/Msg.png"));
    ElementPresentationManager.registerIcon(Arg.class, StrutsIcons.getIcon("validator/Arg.png"));
    ElementPresentationManager.registerIcon(Var.class, StrutsIcons.getIcon("validator/Var.png"));

    // <set-property>, <formset>
    ElementPresentationManager.registerNameProvider(new Function<Object, String>() {
      @Nullable
      public String fun(final Object s) {
        if (s instanceof SetProperty) {
          final SetProperty setProperty = ((SetProperty)s);
          final String property = setProperty.getProperty().getStringValue();
          if (property != null) {
            return property;
          }
          return setProperty.getKey().getStringValue();
        } else if (s instanceof Formset) {
          final Formset formset = ((Formset) s);
          String lang = formset.getLanguage().getStringValue();
          String country = formset.getCountry().getStringValue();
          String variant = formset.getVariant().getStringValue();
          String name = lang == null ? null : lang;
          if (country != null) {
            name = name == null ? country : name + "_" + country;
          }
          if (variant != null) {
            name = name == null ? variant : name + "_" + variant;
          }
          return name;
        } else if (s instanceof Arg) {
          final String name = ((Arg) s).getName().getStringValue();
          if (name == null) {
            return null;
          }
          final String position = ((Arg) s).getPosition().getStringValue();
          return position == null ? name : name + position;
        }
        return null;
      }
    });
  }

  public Class[] getInspectionClasses() {
    return new Class[]{
      StrutsInspection.class,
      TilesInspection.class,
      ValidatorInspection.class,
      ValidatorFormInspection.class
    };
  }

}
