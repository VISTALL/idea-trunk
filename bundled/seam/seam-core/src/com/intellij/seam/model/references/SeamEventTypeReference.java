package com.intellij.seam.model.references;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.seam.model.jam.SeamJamComponent;
import com.intellij.seam.model.jam.SeamJamModel;
import com.intellij.seam.model.jam.context.SeamJamObserver;
import com.intellij.seam.model.xml.SeamDomModel;
import com.intellij.seam.model.xml.SeamDomModelManager;
import com.intellij.seam.model.xml.components.SeamEvent;
import com.intellij.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public abstract class SeamEventTypeReference<T extends PsiElement>  extends BasicEventTypeReference<T> {

  public SeamEventTypeReference(final T element) {
    super(element);
  }

  public Object[] getVariants() {
    final Module module = ModuleUtil.findModuleForPsiElement(getElement());
    if (module != null) {
      List<String> eventTypes = new ArrayList<String>();
      for (SeamJamComponent seamJamComponent : SeamJamModel.getModel(module).getSeamComponents()) {
        for (SeamJamObserver observer : seamJamComponent.getObservers()) {
          eventTypes.addAll(Arrays.asList(observer.getEventTypes()));
        }
      }

      for (SeamDomModel domModel : SeamDomModelManager.getInstance(module.getProject()).getAllModels(module)) {
        for (SeamEvent eventType : domModel.getEvents()) {
          final String value = eventType.getType().getStringValue();
          if (!StringUtil.isEmptyOrSpaces(value)) {
            eventTypes.add(value);
          }
        }
      }
      return ArrayUtil.toObjectArray(eventTypes);
    }
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  public static class SeamLiteralExpression extends SeamEventTypeReference<PsiLiteralExpression> {
    public SeamLiteralExpression(final PsiLiteralExpression element) {
      super(element);
    }

    protected String getEventType(final PsiLiteralExpression psiElement) {
      return (String)psiElement.getValue();
    }
  }
}
