package com.intellij.seam.model.xml.converters;

import com.intellij.seam.model.xml.PageflowDomModelManager;
import com.intellij.seam.model.xml.PageflowModel;
import com.intellij.seam.model.xml.pageflow.Action;
import com.intellij.seam.model.xml.pageflow.PageflowDefinition;
import com.intellij.seam.model.xml.pageflow.Page;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

/**
 * User: Sergey.Vasiliev
 */
public class PageflowDefinitionActionConverter extends ResolvingConverter<Action> {

  @NotNull
  public Collection<Action> getVariants(final ConvertContext context) {
    final DomElement element = context.getInvocationElement();
    final Action currentAction = element.getParentOfType(Action.class, false);
    final List<Action> pageflowActions = getPageflowActions(getPageflowDefinition(context));

    if (currentAction != null) {
      final String actionName = currentAction.getName().getStringValue();
      if (!StringUtil.isEmptyOrSpaces(actionName)) {
        List<Action> noCurrent = new ArrayList<Action>();
        for (Action pageflowAction : pageflowActions) {
          if (!actionName.equals(pageflowAction.getName().getStringValue())) {
            noCurrent.add(pageflowAction);
          }
        }
        return noCurrent;
      }
    }

    return pageflowActions;
  }

  public Action fromString(@Nullable @NonNls final String s, final ConvertContext context) {
    if (s == null) return null;

    final PageflowDefinition pageflowDefinition = getPageflowDefinition(context);

    for (Action action : getPageflowActions(pageflowDefinition)) {
      if (s.equals(action.getName().getStringValue())) {
        return action;
      }
    }

    return null;
  }

  @Nullable
  private static PageflowDefinition getPageflowDefinition(final ConvertContext context) {
    final PageflowModel model = PageflowDomModelManager.getInstance(context.getFile().getProject()).getPageflowModel(context.getFile());

    if (model == null || model.getRoots().size() != 1) return null;

    return model.getRoots().get(0).getRootElement();
  }

  private static List<Action> getPageflowActions(@Nullable final PageflowDefinition pageflowDefinition) {
    List<Action> actions = new ArrayList<Action>();
    if (pageflowDefinition == null) {
      return Collections.emptyList();
    }

    actions.addAll(pageflowDefinition.getActions());

    for (Page page : pageflowDefinition.getPages()) {
      actions.addAll(page.getActions());
    }

    actions.addAll(pageflowDefinition.getStartPage().getActions());

    return actions;

  }

  public String toString(@Nullable final Action pageflowNamedElement, final ConvertContext context) {
    return pageflowNamedElement == null ? null : pageflowNamedElement.getName().getStringValue();
  }
}
