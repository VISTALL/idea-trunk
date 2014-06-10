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

package com.intellij.uml.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class CollectionUtils {
  private CollectionUtils() {
  }

  public static <T> List<T> intersection(Collection<T> collection, Collection<T>...other) {
    final List<T> common = new ArrayList<T>();
    for (T t : collection) {
      boolean add = true;
      for (Collection<T> col : other) {
        if (!col.contains(t)) {
          add = false;
          break;
        }
      }

      if (add) common.add(t);
    }
    return common;
  }
}
