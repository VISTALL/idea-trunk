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

package com.intellij.execution.junit2.ui;

import org.jetbrains.annotations.NonNls;

public interface TestStatistics {
  @NonNls String RUNNING_SUITE_PREFIX = "RUNNING: ";
  TestStatistics ABCENT = new NoStatistics("NO SELECTION");
  TestStatistics NOT_RUN = new NoStatistics("NOT RUN");
  TestStatistics RUNNING = new NoStatistics("RUNNING");

  String getTime();

  String getMemoryUsageDelta();

  String getBeforeMemory();

  String getAfterMemory();

  static class NoStatistics implements TestStatistics {
    private final String myMessage;

    public NoStatistics(@NonNls final String message) {
      myMessage = "<" + message + ">";
    }

    public String getTime() {
      return myMessage;
    }

    public String getMemoryUsageDelta() {
      return myMessage;
    }

    public String getBeforeMemory() {
      return myMessage;
    }

    public String getAfterMemory() {
      return myMessage;
    }

    public String toString() {
      return myMessage;
    }
  }
}
