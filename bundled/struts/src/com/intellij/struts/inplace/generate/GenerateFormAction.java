/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.struts.inplace.generate;

import com.intellij.struts.StrutsBundle;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.dom.*;
import com.intellij.util.xml.DomElement;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class GenerateFormAction extends GenerateMappingAction<FormBean> {

  public GenerateFormAction() {

    super(new GenerateMappingProvider<FormBean>(StrutsBundle.message("generate.form"), FormBean.class, "struts-form-bean", FormBeans.class, StrutsConfig.class) {

      public FormBean generate(@Nullable final DomElement parent, final Editor editor) {
        final FormBeans mappings;
        if (parent instanceof StrutsConfig) {
          mappings = ((StrutsConfig)parent).getFormBeans();
          mappings.ensureTagExists();
        }
        else if (parent instanceof FormBeans) {
          mappings = (FormBeans)parent;
        }
        else {
          return null;
        }
        return mappings.addFormBean();
      }

    },  StrutsIcons.FORMBEAN_ICON);
  }

}