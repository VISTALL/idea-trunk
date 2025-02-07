/*
 * Copyright 2007 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.reference.jsp;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Various utilities for taglibs.
 *
 * @author Yann C&eacute;bron
 */
final class TaglibUtil {

  /**
   * Splits action-name from action-method.
   */
  static final char BANG_SYMBOL = '!';

  private TaglibUtil() {
  }

  /**
   * Checks whether the given attribute value is a dynamic expression.
   * Currently only checks for OGNL ("%{EXPRESSION}").
   *
   * @param attributeValue The attribute value to check.
   * @return true if yes, false otherwise.
   */
  static boolean isDynamicExpression(@NotNull @NonNls final String attributeValue) {
    return StringUtil.startsWith(attributeValue, "%{");
  }

  /**
   * Trims the given value to the real action path.
   *
   * @param attributeValue Custom tag attribute value.
   * @return Action path.
   */
  @NotNull
  @NonNls
  static String trimActionPath(@NotNull @NonNls final String attributeValue) {
    final int bangIndex = StringUtil.indexOf(attributeValue, BANG_SYMBOL);
    if (bangIndex == -1) {
      return attributeValue;
    }

    return attributeValue.substring(0, bangIndex);
  }

}