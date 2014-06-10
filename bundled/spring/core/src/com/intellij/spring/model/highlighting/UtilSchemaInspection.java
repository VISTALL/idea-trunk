package com.intellij.spring.model.highlighting;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.converters.SpringConverterUtil;
import com.intellij.spring.model.xml.beans.Beans;
import com.intellij.spring.model.xml.beans.ListOrSet;
import com.intellij.spring.model.xml.util.UtilList;
import com.intellij.spring.model.xml.util.UtilMap;
import com.intellij.spring.model.xml.util.UtilSet;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UtilSchemaInspection extends InjectionValueTypeInspection {

  public void checkFileElement(final DomFileElement<Beans> domFileElement, final DomElementAnnotationHolder holder) {
    final Beans beans = domFileElement.getRootElement();

    for (UtilSet springSet : DomUtil.getDefinedChildrenOfType(beans, UtilSet.class)) {
      checkSetBean(springSet, holder);
      checkElementsHolder(springSet, holder);
    }
    for (UtilList list : DomUtil.getDefinedChildrenOfType(beans, UtilList.class)) {
      checkListBean(list, holder);
      checkElementsHolder(list, holder);
    }
    for (UtilMap map : DomUtil.getDefinedChildrenOfType(beans, UtilMap.class)) {
      checkMapBean(map, holder);
    }
  }

  private void checkElementsHolder(final ListOrSet springSet, final DomElementAnnotationHolder holder) {
     checkSpringPropertyCollection(springSet, holder);
  }

  private static void checkMapBean(final UtilMap map, final DomElementAnnotationHolder holder) {
    checkProperClass(map.getMapClass(), Map.class, holder);
  }

  private static void checkListBean(final UtilList list, final DomElementAnnotationHolder holder) {
    checkProperClass(list.getListClass(), List.class, holder);
  }

  private static void checkSetBean(final UtilSet set, final DomElementAnnotationHolder holder) {
    checkProperClass(set.getSetClass(), Set.class, holder);
  }

  private static void checkProperClass(final GenericAttributeValue<PsiClass> attrClass,
                                       final Class aClass,
                                       final DomElementAnnotationHolder holder) {
    final PsiClass psiClass = attrClass.getValue();
    if (psiClass != null) {
      if (!isAssignable(psiClass, aClass)) {
        final String s = SpringBundle.message("util.requred.class.message", aClass.getName());
        holder.createProblem(attrClass, s);
      }
    }
  }

  private static boolean isAssignable(final PsiClass psiClass, final Class fromClass) {
    final Project project = psiClass.getProject();
    final PsiType fromType = SpringConverterUtil.findType(fromClass, project);
    final PsiClassType classType = JavaPsiFacade.getInstance(project).getElementFactory().createType(psiClass);

    return fromType != null && fromType.isAssignableFrom(classType);
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("util.schema.inspection.name");
  }

  @NonNls
  @NotNull
  public String getShortName() {
    return "UtilSchemaInspection";
  }

  @NotNull
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }
}

