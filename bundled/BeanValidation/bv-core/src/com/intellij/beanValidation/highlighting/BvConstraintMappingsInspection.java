/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.intellij.beanValidation.highlighting;

import com.intellij.beanValidation.highlighting.checkers.BvChecker;
import com.intellij.beanValidation.highlighting.checkers.CheckAnnotationIsConstraint;
import com.intellij.beanValidation.highlighting.checkers.CheckClassIsConstraintAnnotation;
import com.intellij.beanValidation.highlighting.checkers.CheckMissingParameters;
import com.intellij.beanValidation.model.xml.BvMappingsDomElement;
import com.intellij.beanValidation.resources.BVBundle;
import com.intellij.beanValidation.resources.BVInspectionBundle;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author Konstantin Bulenkov
 */
public class BvConstraintMappingsInspection extends BasicDomElementsInspection<BvMappingsDomElement>{
  private static final HashMap<String, BvChecker[]> CHECKERS = new HashMap<String, BvChecker[]>();

  static {
    register("constraint/annotation", new CheckClassIsConstraintAnnotation(), new CheckMissingParameters());
    register("element/name", new CheckAnnotationIsConstraint());
  }

  private static void register(String key, BvChecker... checkers) {
    BvConstraintMappingsInspection.CHECKERS.put(key, checkers);
  }

  public BvConstraintMappingsInspection() {
    super(BvMappingsDomElement.class);
  }

  @Nls
  @NotNull
  @Override
  public String getGroupDisplayName() {
    return BVInspectionBundle.message("model.inspection.group.name");
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return BVBundle.message("constraints.config.inspection");
  }

  @NotNull
  @Override
  public String getShortName() {
    return getClass().getSimpleName();
  }

  @Override
  protected void checkDomElement(DomElement element, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
    super.checkDomElement(element, holder, helper);
    if (element instanceof GenericDomValue) {
      final GenericDomValue value = (GenericDomValue)element;
      final String checkerId = getCheckerId(value);
      final BvChecker[] checkers = CHECKERS.get(checkerId);
      if (checkers != null) {
        for (BvChecker checker : checkers) {
          checker.check(value, holder, helper);
        }
      }
    }
  }

  private static String getCheckerId(@NotNull GenericDomValue value) {
    final DomElement parent = value.getParent();
    return parent == null ? value.getXmlElementName() : parent.getXmlElementName() + "/" + value.getXmlElementName();
  }
}
