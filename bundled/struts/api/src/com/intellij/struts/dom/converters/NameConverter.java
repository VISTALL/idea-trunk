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

package com.intellij.struts.dom.converters;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.codeStyle.SuggestedNameInfo;
import com.intellij.psi.codeStyle.VariableKind;
import com.intellij.struts.StrutsManager;
import com.intellij.struts.StrutsModel;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.ActionMappings;
import com.intellij.struts.dom.FormBean;
import com.intellij.struts.dom.FormBeans;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 */
public abstract class NameConverter<T extends DomElement> implements CustomReferenceConverter<String> {

  private final String myClassPostfix;

  public NameConverter(@NonNls String classPostfix) {
    myClassPostfix = classPostfix;
  }

  protected abstract GenericDomValue<PsiClass> getClassElement(T parent);

  @Nullable
  protected abstract List<T> getSiblings(T parent);

  protected void preprocess(List<String> variants) {}

  @NotNull
  public Collection<? extends String> getVariants(final ConvertContext context) {
    //noinspection unchecked
    final T parent = (T)context.getInvocationElement().getParent();
    final GenericDomValue<PsiClass> classElement = getClassElement(parent);
    final PsiClass psiClass = classElement.getValue();
    if (psiClass != null) {
      final ArrayList<String> variants = new ArrayList<String>();
      final String className = psiClass.getName();
      if (className != null && className.endsWith(myClassPostfix)) {
        String s = className.substring(0, className.length() - myClassPostfix.length());
        s = StringUtil.decapitalize(s);
        variants.add(s);
      } else {
        final Project project = psiClass.getProject();
        final PsiClassType classType = JavaPsiFacade.getInstance(project).getElementFactory().createType(psiClass);

        JavaCodeStyleManager codeStyleManager = JavaCodeStyleManager.getInstance(project);
        final SuggestedNameInfo info = codeStyleManager.suggestVariableName(VariableKind.LOCAL_VARIABLE, null, null, classType);
        variants.addAll(Arrays.asList(info.names));
      }
      preprocess(variants);
      final List<T> list = getSiblings(parent);
      if (list != null) {
        for (int i = 0; i < variants.size(); i++) {
          String name = variants.get(i);
          int iter = 0;
          while (DomUtil.findByName(list, name) != null) {
            name = variants.get(i) + (++iter);
          }
          variants.set(i, name);
        }
      }
      return variants;
    }
    return Collections.emptyList();
  }

  @NotNull
  public PsiReference[] createReferences(final GenericDomValue<String> genericDomValue, PsiElement element, final ConvertContext context) {
    final PsiReferenceBase<PsiElement> ref = new PsiReferenceBase<PsiElement>(element) {

      public PsiElement resolve() {
        return genericDomValue.getParent().getXmlTag();
      }

      public boolean isSoft() {
        return true;
      }

      //do nothing. the element will be renamed via PsiMetaData (com.intellij.refactoring.rename.RenameUtil.doRenameGenericNamedElement())
      public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
        return getElement();
      }

      public Object[] getVariants() {
        return NameConverter.this.getVariants(context).toArray();
      }
    };
    return new PsiReference[] {ref};
  }

  @SuppressWarnings({"WeakerAccess"})
  public static class ForAction extends NameConverter<Action> {
    @SuppressWarnings({"UnusedDeclaration"})
    public ForAction() {
      super("Action");
    }

    protected void preprocess(final List<String> variants) {
      for (int i = 0; i < variants.size(); i++) {
        variants.set(i, "/" + variants.get(i));
      }
    }

    protected GenericDomValue<PsiClass> getClassElement(final Action parent) {
      return parent.getType();
    }

    @Nullable
    protected List<Action> getSiblings(final Action parent) {
      final StrutsModel model = StrutsManager.getInstance().getStrutsModel(parent.getXmlTag());
      if (model == null) {
        final DomElement element = parent.getParent();
        assert element != null;
        return ((ActionMappings)element).getActions();
      }
      else {
        return model.getActions();
      }
    }
  }

  public static class ForForm extends NameConverter<FormBean> {
    public ForForm() {
      super("Form");
    }

    protected GenericDomValue<PsiClass> getClassElement(final FormBean parent) {
      return parent.getType();
    }

    @Nullable
    protected List<FormBean> getSiblings(final FormBean parent) {
      final StrutsModel model = StrutsManager.getInstance().getStrutsModel(parent.getXmlTag());
      if (model == null) {
        final DomElement element = parent.getParent();
        assert element != null;
        return ((FormBeans)element).getFormBeans();
      }
      else {
        return model.getFormBeans();
      }
    }
  }
}
