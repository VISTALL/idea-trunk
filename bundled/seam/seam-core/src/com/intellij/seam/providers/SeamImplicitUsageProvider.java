package com.intellij.seam.providers;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.seam.constants.SeamAnnotationConstants;
import org.jetbrains.annotations.NonNls;

import java.util.Arrays;
import java.util.List;

public class SeamImplicitUsageProvider implements ImplicitUsageProvider {
  private static final @NonNls List<String> RUSED_SYMBOL_ANNOTATIONS =
      Arrays.asList(SeamAnnotationConstants.JSF_DATA_MODEL_ANNOTATION,
                    SeamAnnotationConstants.OUT_ANNOTATION,
                    SeamAnnotationConstants.FACTORY_ANNOTATION);

  private static final @NonNls List<String> WUSED_SYMBOL_ANNOTATIONS =
      Arrays.asList(SeamAnnotationConstants.JSF_DATA_MODEL_SELECTION_ANNOTATION,
                    SeamAnnotationConstants.JSF_DATA_MODEL_SELECTION_INDEX_ANNOTATION,
                    SeamAnnotationConstants.IN_ANNOTATION,
                    SeamAnnotationConstants.LOGGER_ANNOTATION,
                    SeamAnnotationConstants.REQUEST_PARAMETER_ANNOTATION_1_0,
                    SeamAnnotationConstants.REQUEST_PARAMETER_ANNOTATION_2_0
  );

  public boolean isImplicitUsage(PsiElement element) {
    return element instanceof PsiModifierListOwner &&
           (AnnotationUtil.isAnnotated((PsiModifierListOwner)element, RUSED_SYMBOL_ANNOTATIONS) ||
            AnnotationUtil.isAnnotated((PsiModifierListOwner)element, WUSED_SYMBOL_ANNOTATIONS));
  }

  public boolean isImplicitRead(final PsiElement element) {
    return element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, RUSED_SYMBOL_ANNOTATIONS);
  }

  public boolean isImplicitWrite(final PsiElement element) {
    return element instanceof PsiModifierListOwner && AnnotationUtil.isAnnotated((PsiModifierListOwner)element, WUSED_SYMBOL_ANNOTATIONS);
  }
}
