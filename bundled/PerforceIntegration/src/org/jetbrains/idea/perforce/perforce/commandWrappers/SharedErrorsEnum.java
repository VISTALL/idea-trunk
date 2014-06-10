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

package org.jetbrains.idea.perforce.perforce.commandWrappers;

import java.util.regex.Pattern;

abstract class SharedErrorsEnum<T> {
  public abstract boolean matches(final String s);
  protected SharedErrorsEnum() {}

  static class Contained<T> extends SharedErrorsEnum<T> {
    private final String myValue;

    Contained(String value) {
      myValue = value;
    }

    @Override
    public boolean matches(final String s) {
      return s != null && s.contains(myValue);
    }
  }

  static class Patterned<T> extends SharedErrorsEnum<T> {
    private final Pattern myPattern;

    Patterned(final String pattern) {
      myPattern = Pattern.compile(pattern);
    }

    @Override
    public boolean matches(String s) {
      return s != null && myPattern.matcher(s).matches();
    }
  }
}
