/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.converters;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dmitry Avdeev
 */
public abstract class CreateElementQuickFixProvider<T>  {

  private final String myFamilyName;

  public CreateElementQuickFixProvider(String familyName) {

    myFamilyName = familyName;
  }

  public LocalQuickFix[] getQuickFixes(final GenericDomValue<T> value) {
    final LocalQuickFix fix = getQuickFix(value);
    return fix == null ? LocalQuickFix.EMPTY_ARRAY : new LocalQuickFix[] { fix };      
  }

  @Nullable
  public LocalQuickFix getQuickFix(final GenericDomValue<T> value) {

    final String elementName = getElementName(value);
    if (!isAvailable(elementName, value)) {
      return null;
    }
    final GenericDomValue<T> copy = value.createStableCopy();

    return new LocalQuickFix() {

      @NotNull
      public String getName() {
        return getFixName(elementName);
      }

      @NotNull
      public String getFamilyName() {
        return myFamilyName;
      }

      public void applyFix(@NotNull final Project project, @NotNull final ProblemDescriptor descriptor) {
        apply(elementName, copy);
      }
    };
  }

  protected boolean isAvailable(String elementName, GenericDomValue<T> value) {
    return elementName != null && elementName.trim().length() > 0;
  }

  protected abstract void apply(String elementName, GenericDomValue<T> value);

  @NotNull
  protected abstract String getFixName(String elementName);

  @Nullable
  protected String getElementName(@NotNull final GenericDomValue<T> value) {
    return value.getStringValue();
  }
}
