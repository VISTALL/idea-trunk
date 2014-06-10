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
import com.intellij.seam.model.jam.jsf.SeamJamConverter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SeamConverterRefertenceProvider extends PsiReferenceProviderBase {

  @NotNull
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull final ProcessingContext context) {
    if (element instanceof XmlAttributeValue) {
      return new PsiReference[]{new SeamConverterPsiReference(element)};
    }
    return PsiReference.EMPTY_ARRAY;
  }

  private static class SeamConverterPsiReference extends PsiReferenceBase {
    public SeamConverterPsiReference(final PsiElement element) {
      super(element, true);
    }

    public PsiElement resolve() {
      final String value = getValue();

      if (StringUtil.isEmptyOrSpaces(value)) return null;

      final List<SeamJamComponent> validorComponents = getConverterComponents(getElement());
      for (SeamJamComponent validorComponent : validorComponents) {
        final SeamJamConverter jamConverter = validorComponent.getConverter();
        if (jamConverter != null && value.equals(jamConverter.getId())) {
          return jamConverter.getIdentifyingAnnotation();
        }
        if (value.equals(validorComponent.getComponentName())) {
          return validorComponent.getIdentifyingPsiElement();
        }
      }

      return null;
    }

    private static String getConverterName(final SeamJamComponent validorComponent) {
      final SeamJamConverter jamConverter = validorComponent.getConverter();
      if (jamConverter != null) {
        final String validatorName = jamConverter.getId();
        if (!StringUtil.isEmptyOrSpaces(validatorName)) {
          return validatorName;
        }
      }
      return validorComponent.getComponentName();
    }

    public Object[] getVariants() {
      List variants = ContainerUtil.mapNotNull(getConverterComponents(getElement()), new Function<SeamJamComponent, Object>() {
        public Object fun(final SeamJamComponent seamJamComponent) {
          return getConverterName(seamJamComponent);
        }
      });

      return ArrayUtil.toObjectArray(variants);
    }

    @NotNull
    private static List<SeamJamComponent> getConverterComponents(final PsiElement element) {
      final List<SeamJamComponent> converters = new ArrayList<SeamJamComponent>();

      final Module module = ModuleUtil.findModuleForPsiElement(element);
      if (module != null) {
        for (SeamJamComponent seamJamComponent : SeamJamModel.getModel(module).getSeamComponents()) {
          if (seamJamComponent.getConverter() != null) {
            converters.add(seamJamComponent);
          }
        }
      }
      return converters;
    }
  }
}
