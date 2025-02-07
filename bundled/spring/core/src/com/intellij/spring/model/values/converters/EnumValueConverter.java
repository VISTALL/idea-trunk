package com.intellij.spring.model.values.converters;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.spring.model.values.PropertyValueConverter;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.List;

/**
 * @author Taras Tielkes
 */
public class EnumValueConverter extends Converter<PsiField> implements CustomReferenceConverter {

  @NotNull
  public static PsiReference[] createReferences(final PsiType type, final GenericDomValue genericDomValue, final PsiElement element) {
    final String stringValue = genericDomValue.getStringValue();
    if (type != null && type instanceof PsiClassType) {
      final PsiClass psiClass = ((PsiClassType)type).resolve();
      if (psiClass != null) {
        return new PsiReference[]{createReference(psiClass, element, stringValue)};
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }

  private static PsiReference createReference(final PsiClass psiClass, final PsiElement element, final String stringValue) {
    return new PsiReferenceBase<PsiElement>(element, true) {
      public PsiElement resolve() {
        final PsiField psiField = psiClass.findFieldByName(stringValue, false);
        if (psiField == null && !psiClass.isEnum()) {
          return element;
        }
        return psiField;
      }

      public Object[] getVariants() {
        return getFields(psiClass);
      }
    };
  }

  public PsiField fromString(@Nullable @NonNls String s, final ConvertContext context) {
    return null;
  }

  public String toString(@Nullable PsiField s, final ConvertContext context) {
    return null;
  }

  @NotNull
  public PsiReference[] createReferences(GenericDomValue genericDomValue, PsiElement element, ConvertContext context) {
    Converter converter = genericDomValue.getConverter();
    while (converter instanceof WrappingConverter) {
      if (converter instanceof PropertyValueConverter) {
        final List<? extends PsiType> types = ((PropertyValueConverter)converter).getValueTypes(genericDomValue);
        for (PsiType type : types) {
          final PsiReference[] psiReferences = createReferences(type, genericDomValue, element);
          if (psiReferences.length > 0) {
            return psiReferences;
          }
        }
      }
      converter = ((WrappingConverter) converter).getConverter(genericDomValue);
    }
    return PsiReference.EMPTY_ARRAY;
  }

  public static class TypeCondition implements Condition<Pair<PsiType, GenericDomValue>> {
    private final List<String> EXCLUDE_CLASSES = Arrays.asList(Boolean.class.getName(), Locale.class.getName());

    public boolean value(Pair<PsiType, GenericDomValue> pair) {

      final PsiType type = pair.getFirst();
      if (type != null && type instanceof PsiClassType) {
        if (EXCLUDE_CLASSES.contains(type.getCanonicalText())) return false;
        
        final PsiClass psiClass = ((PsiClassType)type).resolve();
        if (psiClass != null) {
          if (psiClass.isEnum()) {
            return true;
          }
          for (PsiField psiField: psiClass.getFields()) {
            if (psiField.hasModifierProperty(PsiModifier.STATIC) &&
                psiField.hasModifierProperty(PsiModifier.PUBLIC) &&
                psiField.getType().equals(type)) {
                return true;
            }
          }
        }
      }
      return false;
    }
  }

  private static PsiField[] getFields(@NotNull final PsiClass psiClass) {
    final ArrayList<PsiField> fields = new ArrayList<PsiField>();
    final PsiField[] psiFields = psiClass.getFields();
    for (PsiField psiField : psiFields) {
      if (psiField.hasModifierProperty(PsiModifier.STATIC) && psiField.hasModifierProperty(PsiModifier.PUBLIC)) {
        final PsiType type = psiField.getType();
        if (type instanceof PsiClassType) {
          final PsiClass typeClass = ((PsiClassType)type).resolve();
          if (typeClass != null && typeClass.equals(psiClass)) {
            fields.add(psiField);
          }
        }
      }
    }

    return fields.toArray(new PsiField[fields.size()]);
  }
}
