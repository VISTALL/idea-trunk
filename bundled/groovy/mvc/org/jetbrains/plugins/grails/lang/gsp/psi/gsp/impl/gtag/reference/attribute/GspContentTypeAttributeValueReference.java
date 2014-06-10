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

package org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.reference.attribute;

import org.jetbrains.plugins.grails.lang.gsp.psi.gsp.impl.gtag.reference.GspAttributeValueReferenceBase;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author ilyas
 */
public class GspContentTypeAttributeValueReference extends GspAttributeValueReferenceBase{

  public GspContentTypeAttributeValueReference(final PsiElement element) {
    super(element);
  }

  @Nullable
  public PsiElement resolve() {
    return null;
  }

  public Object[] getVariants() {
    return new String[]{"text/html; ISO-8859-1"};
  }

  public boolean isSoft() {
    return true;
  }
}
