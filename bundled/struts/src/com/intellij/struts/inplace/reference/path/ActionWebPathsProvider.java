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

package com.intellij.struts.inplace.reference.path;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.daemon.QuickFixProvider;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.javaee.web.CustomServletReferenceAdapter;
import com.intellij.javaee.web.ServletMappingInfo;
import com.intellij.javaee.web.ServletMappingType;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.struts.StrutsBundle;
import com.intellij.struts.StrutsIcons;
import com.intellij.struts.StrutsManager;
import com.intellij.struts.StrutsModel;
import com.intellij.struts.dom.Action;
import com.intellij.struts.dom.ActionMappings;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.highlighting.ResolvingElementQuickFix;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dmitry Avdeev
 */
public class ActionWebPathsProvider extends CustomServletReferenceAdapter {

  private final boolean myPrefixAllowed;

  public ActionWebPathsProvider() {
    myPrefixAllowed = true;
  }

  public ActionWebPathsProvider(final boolean prefixAllowed) {
    myPrefixAllowed = prefixAllowed;
  }

  @Nullable
  public PathReference createWebPath(final String path, final @NotNull PsiElement element, final ServletMappingInfo info) {
    StrutsModel model = StrutsManager.getInstance().getStrutsModel(element);
    if (model != null) {
      final Action action = model.resolveActionURL(path);
      if (action != null) {
        return new PathReference(path,  new PathReference.ConstFunction(StrutsIcons.ACTION_ICON)) {
          public PsiElement resolve() {
            return action.getXmlTag();
          }
        };
      }
    }
    return null;
  }

  protected PsiReference[] createReferences(final @NotNull PsiElement element,
                                            final int offset,
                                            final String text,
                                            final @Nullable ServletMappingInfo info,
                                            final boolean soft) {
    final StrutsModel model = StrutsManager.getInstance().getStrutsModel(element);
    if (model != null) {
      return new PsiReference[]{new ActionReference(element, offset, text, info == null || info.equals(model.getServletMappingInfo()) ? info : null, soft)};
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private class ActionReference extends PsiReferenceBase<PsiElement>
    implements EmptyResolveMessageProvider, QuickFixProvider<ActionReference>, LocalQuickFixProvider {


    public ActionReference(@NotNull final PsiElement element, int offset, String text, ServletMappingInfo info, final boolean soft) {

      super(element, new TextRange(offset, offset + text.length()), soft);
      if (info != null && (info.getType() != ServletMappingType.PATH || myPrefixAllowed)) {
        final TextRange range = info.getNameRange(text);
        if (range != null) {
          setRangeInElement(range.shiftRight(offset));
        }
      }
    }

    @Nullable
    public PsiElement resolve() {
      final StrutsModel model = StrutsManager.getInstance().getStrutsModel(myElement);
      if (model == null) {
        return null;
      }
      String url = getValue();
      Action action = model.findAction(url);
      if (action != null) {
        return action.getXmlTag();
      }
      else {
        return null;
      }
    }

    public Object[] getVariants() {
      final StrutsModel model = StrutsManager.getInstance().getStrutsModel(myElement);
      if (model == null) {
        return ArrayUtil.EMPTY_OBJECT_ARRAY;
      }
      final ServletMappingInfo info = model.getServletMappingInfo();
      List<Action> actions = model.getActions();
      return ElementPresentationManager.getInstance().createVariants(actions, new Function<Action, String>() {
        @Nullable
        public String fun(final Action action) {
          final String actionPath = action.getPath().getValue();
          if (actionPath == null) {
            return null;
          }
          else {
            return myPrefixAllowed ? info.addMapping(actionPath) : actionPath;
          }
        }
      }, Iconable.ICON_FLAG_VISIBILITY);
    }

    public String getUnresolvedMessagePattern() {
      return StrutsBundle.message("cannot.resolve.action", getValue());
    }

    public void registerQuickfix(final HighlightInfo info, final ActionReference reference) {
      final ResolvingElementQuickFix fix = createFix();
      QuickFixAction.registerQuickFixAction(info, fix);
    }

    @Nullable
    private ResolvingElementQuickFix createFix() {      
      final StrutsModel model = StrutsManager.getInstance().getStrutsModel(myElement);
      if (model == null) {
        return null;
      }
      final String text = getValue();
      final ActionMappings scope = model.getMergedModel().getActionMappings();
      return ResolvingElementQuickFix.createFix(text, Action.class, scope);
    }

    public LocalQuickFix[] getQuickFixes() {
      final ResolvingElementQuickFix quickFix = createFix();
      return quickFix == null ? LocalQuickFix.EMPTY_ARRAY : new LocalQuickFix[] {quickFix};
    }
  }
}
