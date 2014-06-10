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

package com.intellij.uml.presentation;

/**
 * @author Konstantin Bulenkov
 */
public enum VisibilityLevel {
  //Do not change the order of levels
  PUBLIC, PACKAGE, PROTECTED, PRIVATE;

  private static final String ALL = "All";

  public static VisibilityLevel fromString(String name) {
    if (name == null || name.equalsIgnoreCase(ALL)) return PRIVATE;
    for (VisibilityLevel level : VisibilityLevel.values()) {
      if (level.name().equalsIgnoreCase(name)) return level;
    }
    return PRIVATE;
  }

  public String toString() {
    return this == PRIVATE ? ALL : capitalizeFirstLetter(name().toLowerCase());
  }

  private static String capitalizeFirstLetter(String s) {
    return s.substring(0,1).toUpperCase() + s.substring(1);
  }

  public boolean weakerThan(VisibilityLevel level) {
    return this.compareTo(level) <= 0;
  }
}
