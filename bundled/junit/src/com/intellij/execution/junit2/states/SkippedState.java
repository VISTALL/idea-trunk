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

package com.intellij.execution.junit2.states;

import com.intellij.execution.junit2.TestProxy;
import com.intellij.execution.junit2.segments.ObjectReader;
import com.intellij.execution.junit2.ui.Formatters;
import com.intellij.execution.testframework.Printer;
import com.intellij.execution.testframework.ui.PrintableTestProxy;
import com.intellij.execution.ui.ConsoleViewContentType;

class SkippedState extends ReadableState {
  private TestProxy myPeformedTest;

  void initializeFrom(final ObjectReader reader) {
    myPeformedTest = reader.readObject();
  }

  public void printOn(final Printer printer) {
    printer.print(Formatters.printTest(myPeformedTest) + ":" + PrintableTestProxy.NEW_LINE,
                  ConsoleViewContentType.SYSTEM_OUTPUT);
    myPeformedTest.printOn(printer);
  }
}
