/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package com.intellij.execution.junit2.ui.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.execution.ExecutionBundle;

class Flag {
  private final Logger myLogger;
  private boolean myValue;

  public Flag(final Logger logger, final boolean value) {
    myLogger = logger;
    myValue = value;
  }

  public void setValue(final boolean value) {
    myValue = value;
//    StringWriter out = new StringWriter();
//    new Exception(String.valueOf(value)).printStackTrace(new PrintWriter(out));
//    myLastAccess = out.toString();
  }

  public void assertValue(final boolean expected) {
    myLogger.assertTrue(expected == myValue, "first time");
  }

  public boolean getValue() {
    return myValue;
  }
}
