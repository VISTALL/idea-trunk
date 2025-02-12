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

package com.intellij.struts.inplace.gutter;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.StrutsModel;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.Controller;
import com.intellij.struts.dom.FormBean;
import com.intellij.struts.dom.PlugIn;
import com.intellij.struts.facet.StrutsFacet;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Provides annotations and gutter mark icons for Struts-related classes.
 *
 * @author Dmitry Avdeev
 */
public class StrutsClassAnnotator implements Annotator {

  private final ClassAnnotator[] myClassAnnotators =
    new ClassAnnotator[]{
      new ClassAnnotator("org.apache.struts.action.Action", StrutsIcons.ACTION_ICON) {

        @Nullable
        protected DomElement[] getDestinations(final PsiClass clazz) {
          final ArrayList<DomElement> destinations = new ArrayList<DomElement>();
          final StrutsModel model = getCombinedStrutsModel(clazz);
          if (model != null) {
            for (final Action action : model.getActions()) {
              if (action.getType().getValue() == clazz) {
                destinations.add(action);
              }
            }
          }
          return destinations.isEmpty() ? null : destinations.toArray(DomElement.EMPTY_ARRAY);
        }
      },

      new ClassAnnotator("org.apache.struts.action.ActionForm",
                         StrutsIcons.FORMBEAN_ICON) {

        @Nullable
        protected DomElement[] getDestinations(final PsiClass clazz) {
          final ArrayList<DomElement> destinations = new ArrayList<DomElement>();
          final StrutsModel model = getCombinedStrutsModel(clazz);
          if (model != null) {
            for (final FormBean formBean : model.getFormBeans()) {
              if (formBean.getType().getValue() == clazz) {
                destinations.add(formBean);
              }
            }
          }
          return destinations.isEmpty() ? null : destinations.toArray(DomElement.EMPTY_ARRAY);
        }
      },

      new ClassAnnotator("org.apache.struts.action.RequestProcessor",
                         StrutsIcons.CONTROLLER_ICON) {

        @Nullable
        protected DomElement[] getDestinations(final PsiClass clazz) {
          final StrutsModel model = getCombinedStrutsModel(clazz);
          if (model != null) {
            final Controller controller = model.getMergedModel().getController();
            final PsiClass processorClazz = controller.getProcessorClass().getValue();
            if (processorClazz != null && processorClazz == clazz) {
              return new DomElement[]{controller};
            }
          }
          return null;
        }
      },

      new ClassAnnotator("org.apache.struts.action.PlugIn",
                         StrutsIcons.PLUGIN_ICON) {

        @Nullable
        protected DomElement[] getDestinations(final PsiClass clazz) {
          final ArrayList<DomElement> destinations = new ArrayList<DomElement>();
          final StrutsModel model = getCombinedStrutsModel(clazz);
          if (model != null) {
            for (final PlugIn plugIn : model.getMergedModel().getPlugIns()) {
              if (plugIn.getClassName().getValue() == clazz) {
                destinations.add(plugIn);
              }
            }
          }
          return destinations.isEmpty() ? null : destinations.toArray(DomElement.EMPTY_ARRAY);
        }
      }

    };

  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof PsiClass &&
        StrutsFacet.isPresentForContainingWebFacet(psiElement)) {
      for (final ClassAnnotator classAnnotator : myClassAnnotators) {
        if (classAnnotator.annotate((PsiClass) psiElement, holder)) {
          break;
        }
      }
    }
  }

}
