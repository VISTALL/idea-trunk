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

package org.jetbrains.android.facet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.android.util.AndroidUtils;
import com.intellij.psi.PsiClass;

/**
* Created by IntelliJ IDEA.
* User: Eugene.Kudelevsky
* Date: Jun 11, 2009
* Time: 8:29:28 PM
* To change this template use File | Settings | File Templates.
*/
public class SimpleClassMapConstructor implements ClassMapConstructor {
  private static SimpleClassMapConstructor INSTANCE;

  private SimpleClassMapConstructor() {
  }

  public static SimpleClassMapConstructor getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new SimpleClassMapConstructor();
    }
    return INSTANCE;
  }

  public String getTagNameByClass(@NotNull PsiClass c) {
    String name = c.getName();
    String qualifiedName = c.getQualifiedName();
    if (qualifiedName != null && !isAndroidLibraryClass(qualifiedName)) {
      name = qualifiedName;
    }
    return name;
  }

  protected static boolean isAndroidLibraryClass(@NotNull String qualifiedClassName) {
    String[] ar = qualifiedClassName.split("\\.");
    return ar.length < 0 || ar[0].equals(AndroidUtils.ANDROID_PACKAGE);
  }
}
