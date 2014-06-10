package com.intellij.seam.model.references;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProviderBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.seam.model.jam.SeamJamComponent;
import com.intellij.seam.model.jam.SeamJamModel;
import com.intellij.seam.model.jam.jsf.SeamJamValidator;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SeamValidatorReferenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
    if (element instanceof XmlAttributeValue) {
      return new PsiReference[]{new SeamValidatorPsiReference(element)};
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static class SeamValidatorPsiReference extends PsiReferenceBase {
    public SeamValidatorPsiReference(final PsiElement element) {
      super(element, true);
    }

    public PsiElement resolve() {
      final String value = getValue();

      if (StringUtil.isEmptyOrSpaces(value)) return null;

      final List<SeamJamComponent> validorComponents = getValidorComponents(getElement());
      for (SeamJamComponent validorComponent : validorComponents) {
        final SeamJamValidator jamValidator = validorComponent.getValidator();
        if (jamValidator != null && value.equals(jamValidator.getId())) {
          return jamValidator.getIdentifyingAnnotation();
        }
        if (value.equals(validorComponent.getComponentName())) {
          return validorComponent.getIdentifyingPsiElement();
        }
      }

      return null;
    }

    private static String getValidatorName(final SeamJamComponent validorComponent) {
      final SeamJamValidator jamValidator = validorComponent.getValidator();
      if (jamValidator != null) {
        final String validatorName = jamValidator.getId();
        if (!StringUtil.isEmptyOrSpaces(validatorName)) {
          return validatorName;
        }
      }
      return validorComponent.getComponentName();
    }

    public Object[] getVariants() {
      List variants = ContainerUtil.mapNotNull(getValidorComponents(getElement()), new Function<SeamJamComponent, Object>() {
        public Object fun(final SeamJamComponent seamJamComponent) {
          return getValidatorName(seamJamComponent);
        }
      });

      return ArrayUtil.toObjectArray(variants);
    }

    @NotNull
    private static List<SeamJamComponent> getValidorComponents(final PsiElement element) {
      final List<SeamJamComponent> validators = new ArrayList<SeamJamComponent>();

      final Module module = ModuleUtil.findModuleForPsiElement(element);
      if (module != null) {
        for (SeamJamComponent seamJamComponent : SeamJamModel.getModel(module).getSeamComponents()) {
          if (seamJamComponent.getValidator() != null) {
            validators.add(seamJamComponent);
          }
        }
      }
      return validators;
    }
  }
}
