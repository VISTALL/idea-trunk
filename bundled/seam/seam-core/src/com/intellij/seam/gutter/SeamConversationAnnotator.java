package com.intellij.seam.gutter;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.intellij.seam.SeamIcons;
import com.intellij.seam.model.jam.SeamJamComponent;
import com.intellij.seam.model.jam.context.SeamJamBegin;
import com.intellij.seam.model.jam.context.SeamJamEnd;
import com.intellij.seam.resources.SeamBundle;
import com.intellij.seam.utils.SeamCommonUtils;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

import java.util.List;

/**
 * User: Sergey.Vasiliev
 */
public class SeamConversationAnnotator implements Annotator {

  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof PsiIdentifier) {
      final PsiElement parent = psiElement.getParent();
      if (parent instanceof PsiClass) {
        final PsiClass aClass = (PsiClass)parent;
        SeamJamComponent seamJamComponent = SeamCommonUtils.getSeamJamComponent(aClass);
        if (seamJamComponent != null)  {
          final List<PsiMethod> begins = ContainerUtil.map2List(seamJamComponent.getBegins(), new Function<SeamJamBegin, PsiMethod>() {
            public PsiMethod fun(final SeamJamBegin seamJamBegin) {
              return seamJamBegin.getPsiElement();
          }}) ;

          final List<PsiMethod> ends = ContainerUtil.map2List(seamJamComponent.getEnds(), new Function<SeamJamEnd, PsiMethod>() {
            public PsiMethod fun(final SeamJamEnd end) {
              return end.getPsiElement();
          }}) ;

          for (SeamJamBegin begin : seamJamComponent.getBegins()) {
            NavigationGutterIconBuilder.create(SeamIcons.SEAM_BEGIN_CONVERSATION_ICON).
            setTargets(ends).
            setCellRenderer(new MyPsiElementListCellRenderer()).
            setPopupTitle(SeamBundle.message("seam.begin.conversation.to.end.title")).
            setTooltipText(SeamBundle.message("seam.begin.conversation.to.end.tooltip.text")).
            install(holder, begin.getIdentifyingAnnotation());
          }

          for (SeamJamEnd end : seamJamComponent.getEnds()) {
            NavigationGutterIconBuilder.create(SeamIcons.SEAM_END_CONVERSATION_ICON).
            setTargets(begins).
            setCellRenderer(new MyPsiElementListCellRenderer()).
            setPopupTitle(SeamBundle.message("seam.end.conversation.to.begin.title")).
            setTooltipText(SeamBundle.message("seam.end.conversation.to.begin.tooltip.text")).
            install(holder, end.getIdentifyingAnnotation());
          }
        }
      }
    }
  }


  private static class MyPsiElementListCellRenderer extends PsiElementListCellRenderer<PsiMethod> {
    public String getElementText(final PsiMethod element) {
      return element.getName();
    }

    protected String getContainerText(final PsiMethod element, final String name) {
      return null;
    }

    protected int getIconFlags() {
      return 0;
    }
  }
}
