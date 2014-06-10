/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.reference.jsp;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupValueFactory;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom tags attribute "action".
 *
 * @author Yann C&eacute;bron
 */
public class ActionReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull final PsiElement psiElement,
                                               @NotNull final ProcessingContext context) {
    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(ModuleUtil.findModuleForPsiElement(psiElement));
    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final XmlAttributeValue xmlAttributeValue = (XmlAttributeValue) psiElement;
    final String path = xmlAttributeValue.getValue();

    // resolve to <action>
    final String actionName = TaglibUtil.trimActionPath(path);
    final String namespace = getNamespace(xmlAttributeValue);
    final List<Action> actions = strutsModel.findActionsByName(actionName, namespace);
    final Action action = actions.isEmpty() ? null : actions.get(0);

    final int bangIndex = StringUtil.indexOf(path, TaglibUtil.BANG_SYMBOL);
    if (bangIndex == -1) {
      return new PsiReference[]{new ActionReference(xmlAttributeValue, action, namespace, strutsModel)};
    }

    return new PsiReference[]{new ActionReference(xmlAttributeValue, action, namespace, strutsModel),
                              new ActionMethodReference(xmlAttributeValue, action, bangIndex)};
  }

  @Nullable
  private static String getNamespace(@NotNull final XmlAttributeValue xmlAttributeValue) {
    final XmlTag tag = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag.class);
    if (tag == null) {
      return null;
    }

    final XmlAttribute namespaceAttribute = tag.getAttribute("namespace");
    return namespaceAttribute != null ? namespaceAttribute.getValue() : null;
  }


  private static class ActionMethodReference extends PsiReferenceBase<XmlAttributeValue> implements EmptyResolveMessageProvider {

    private final Action action;


    private ActionMethodReference(final XmlAttributeValue xmlAttributeValue,
                                  @Nullable final Action action,
                                  final int bangIndex) {
      super(xmlAttributeValue);
      this.action = action;
      setRangeInElement(TextRange.from(getRangeInElement().getStartOffset() + bangIndex + 1,
                                       getRangeInElement().getLength() - 1 - bangIndex));
    }

    public PsiElement resolve() {
      if (action == null) {
        return null;
      }

      final String methodName = getValue();
      return ContainerUtil.find(action.getActionMethods(), new Condition<PsiMethod>() {
        public boolean value(final PsiMethod psiMethod) {
          return Comparing.equal(psiMethod.getName(), methodName);
        }
      });
    }

    @NotNull
    public Object[] getVariants() {
      if (action == null) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
      }

      return action.getActionMethods().toArray(new PsiMethod[action.getActionMethods().size()]);
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action method ''" + getValue() + "''";
    }
  }


  private static class ActionReference extends PsiReferenceBase<XmlAttributeValue> implements EmptyResolveMessageProvider {

    private final Action action;
    private final String namespace;
    private final StrutsModel strutsModel;

    private ActionReference(final XmlAttributeValue xmlAttributeValue,
                            @Nullable final Action action,
                            @Nullable @NonNls final String namespace,
                            final StrutsModel strutsModel) {
      super(xmlAttributeValue);
      this.action = action;
      this.namespace = namespace;
      this.strutsModel = strutsModel;
    }

    public PsiElement resolve() {
      if (TaglibUtil.isDynamicExpression(myElement.getValue())) {
        return myElement;
      }

      if (action == null) {
        return null;
      }

      return action.getXmlTag();
    }

    @NotNull
    public Object[] getVariants() {
      final List<Action> actionList = strutsModel.getActionsForNamespace(namespace);

      final List<Object> variants = new ArrayList<Object>(actionList.size());
      for (final Action action : actionList) {
        final String actionPath = action.getName().getStringValue();
        if (actionPath != null) {
          final Object variant =
              LookupValueFactory.createLookupValueWithHint(actionPath, StrutsIcons.ACTION,
                                                           action.getNamespace());
          variants.add(variant);
        }
      }
      return ArrayUtil.toObjectArray(variants);
    }

    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action ''" + getValue() + "''";
    }

  }

}