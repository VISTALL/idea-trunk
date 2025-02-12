/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
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

package com.intellij.struts.highlighting;

import com.intellij.struts.StrutsProjectComponent;
import com.intellij.struts.facet.StrutsFacet;

/**
 * Provides {@link TilesInspection} on make.
 *
 * @author Dmitry Avdeev
 */
public class TilesValidator extends StrutsValidatorBase {

  public TilesValidator(StrutsProjectComponent component) {
    super("Tiles Model validator", "Validating Tiles model...", component.getTilesFactory(), TilesInspection.class);
  }

  protected boolean isAvailableOnFacet(final StrutsFacet facet) {
    return facet.getConfiguration().getValidationConfiguration().myTilesValidationEnabled;
  }

}
