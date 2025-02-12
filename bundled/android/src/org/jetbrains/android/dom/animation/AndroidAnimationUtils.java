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

package org.jetbrains.android.dom.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.ClassMapConstructor;
import org.jetbrains.android.util.AndroidUtils;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.psi.PsiClass;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Jun 19, 2009
 * Time: 5:50:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidAnimationUtils {
  private AndroidAnimationUtils() {
  }

  private static final String[] TAG_NAMES = {"set", "alpha", "scale", "translate", "rotate", "layoutAnimation", "gridLayoutAnimation"};

  public static String getStyleableNameByTagName(@NotNull String tagName) {
    if (tagName.equals("set")) {
      return "AnimationSet";
    }
    String capitalizedTagName = StringUtil.capitalize(tagName);
    String suffix = "Animation";
    if (ArrayUtil.contains(tagName, TAG_NAMES) && !tagName.endsWith(suffix)) {
      return capitalizedTagName + suffix;
    }
    return capitalizedTagName;
  }

  public static List<String> getPossibleChildren(@NotNull AndroidFacet facet) {
    List<String> children = new ArrayList<String>();
    Collections.addAll(children, TAG_NAMES);
    children.addAll(facet.getClassMap(AndroidUtils.INTERPOLATOR_CLASS_NAME, new ClassMapConstructor() {
      public String getTagNameByClass(@NotNull PsiClass c) {
        return StringUtil.decapitalize(c.getName());
      }
    }).keySet());
    return children;
  }
}
