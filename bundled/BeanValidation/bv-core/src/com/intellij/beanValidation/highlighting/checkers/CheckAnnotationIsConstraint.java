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

package com.intellij.beanValidation.highlighting.checkers;

import com.intellij.beanValidation.model.xml.Element;
import static com.intellij.beanValidation.resources.BVBundle.message;
import static com.intellij.codeInspection.ProblemHighlightType.ERROR;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTagValue;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;

/**
 * @author Konstantin Bulenkov
 */
public class CheckAnnotationIsConstraint implements BvChecker {
  public void check(GenericDomValue value, DomElementAnnotationHolder holder, DomHighlightingHelper helper) {
    final Object obj = value.getValue();
    final DomElement parent = value.getParent();
    if (obj instanceof PsiMethod && parent instanceof Element) {
      final PsiMethod method = (PsiMethod)obj;
      final Element element = (Element)parent;
      final String val = element.getValue();
      final XmlTag tag = parent.getXmlTag();
      final XmlTagValue tagValue = tag.getValue();
      final TextRange textRange = TextRange.from(tagValue.getTextRange().getStartOffset() - tag.getTextRange().getStartOffset(), tagValue.getText().length());

      final PsiType type = method.getReturnType();
      if (PsiType.BOOLEAN.equals(type)) {
        if (!(("false".equals(val) || ("true".equals(val))))) {
          holder.createProblem(element, ERROR, message("should.be.boolean.value"), textRange);
        }
      } else if (PsiType.BYTE.equals(type)) {
        try {
          Byte.parseByte(val);
        } catch (Exception e) {
          holder.createProblem(element, ERROR, message("should.be.number", Byte.MIN_VALUE, Byte.MAX_VALUE), textRange);
        }
      } else if (PsiType.SHORT.equals(type)) {
        try {
          Short.parseShort(val);
        } catch (Exception e) {
          holder.createProblem(element, ERROR, message("should.be.number", Short.MIN_VALUE, Short.MAX_VALUE), textRange);
        }
      } else if (PsiType.INT.equals(type)) {
        try {
          Integer.parseInt(val);
        } catch (Exception e) {
          holder.createProblem(element, ERROR, message("should.be.number", Integer.MIN_VALUE, Integer.MAX_VALUE), textRange);
        }
      } else if (PsiType.LONG.equals(type)) {
        try {
          Long.parseLong(val);
        } catch (Exception e) {
          holder.createProblem(element, ERROR, message("should.be.number", Long.MIN_VALUE, Long.MAX_VALUE), textRange);
        }
      } else if (PsiType.FLOAT.equals(type)) {
        try {
          Float.parseFloat(val);
        } catch (Exception e) {
          holder.createProblem(element, ERROR, message("cant.parse.float"), textRange);
        }
      } else if (PsiType.DOUBLE.equals(type)) {
        try {
          Double.parseDouble(val);
        } catch (Exception e) {
          holder.createProblem(element, ERROR, message("cant.parse.double"), textRange);
        }
      }
    }
  }
}
