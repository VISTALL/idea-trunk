/*
 * Copyright 2000-2005 JetBrains s.r.o.
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
package com.intellij.j2meplugin.module.settings.general;

import com.intellij.openapi.util.Comparing;

/**
 * User: anna
 * Date: Sep 26, 2004
 */
public class UserDefinedOption {

  String myKey;
  String myValue;

  public UserDefinedOption(String key, String value) {
    myKey = key;
    myValue = value;
  }

  public String getKey() {
    return myKey;
  }

  public void setKey(final String key) {
    myKey = key;
  }

  public String getValue() {
    return myValue;
  }

  public void setValue(final String value) {
    myValue = value;
  }

  public boolean equals(final Object o) {
    if (!(o instanceof UserDefinedOption)) return false;
    final UserDefinedOption second = (UserDefinedOption)o;
    return Comparing.equal(myKey, second.myKey) && Comparing.equal(myValue, second.myValue);
  }

  public int hashCode() {
    return Comparing.hashcode(myKey, myValue);
  }

}
