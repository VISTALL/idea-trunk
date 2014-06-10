/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.grails.lang.gsp.util;

import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.grails.lang.gsp.GspDirectiveKind;
import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.api.directive.GspDirective;

/**
 * @author ilyas
 */
public class GspDirectiveUtil {
  @NonNls
  private static final String PAGE_DIRECTIVE = "page";
  @NonNls
  private static final String TAG_DIRECTIVE = "tag";
  @NonNls
  private static final String INCLUDE = "include";
  @NonNls
  private static final String TAGLIB = "taglib";
  @NonNls
  private static final String ATTRIBUTE = "attribute";
  @NonNls
  private static final String VARIABLE = "variable";


  public static GspDirectiveKind getDirectiveKindByTag(XmlTag tag) {
    if (!(tag instanceof GspDirective)) return null;
    String name = tag.getLocalName();
    if (PAGE_DIRECTIVE.equals(name) || TAG_DIRECTIVE.equals(name)) return GspDirectiveKind.PAGE;
    if (INCLUDE.equals(name)) return GspDirectiveKind.INCLUDE;
    if (TAGLIB.equals(name)) return GspDirectiveKind.TAGLIB;
    if (ATTRIBUTE.equals(name)) return GspDirectiveKind.ATTRIBUTE;
    if (VARIABLE.equals(name)) return GspDirectiveKind.VARIABLE;
    return null;
  }


}
