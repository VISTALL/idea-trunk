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

package org.jetbrains.android.dom.drawable;

import org.jetbrains.android.dom.AndroidResourceDomFileDescription;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 21, 2009
 * Time: 9:42:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class DrawableDomFileDescription extends AndroidResourceDomFileDescription<DrawableDomElement> {
  public DrawableDomFileDescription() {
    super(DrawableDomElement.class, "selector", "drawable", "color");
  }

  @Override
  public boolean acceptsOtherRootTagNames() {
    return true;
  }
}
