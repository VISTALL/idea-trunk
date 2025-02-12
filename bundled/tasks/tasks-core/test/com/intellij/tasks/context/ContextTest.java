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

package com.intellij.tasks.context;

import com.intellij.tasks.TaskManager;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;

/**
 * @author Dmitry Avdeev
 */
public class ContextTest extends JavaCodeInsightFixtureTestCase {

  private TaskManager myManager;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myManager = TaskManager.getManager(getProject());
  }

  public void testRunConfigurations() throws Exception {
    //Task activeTask = myManager.getActiveTask();
    //myManager.activateTask(new TaskImpl("test", "summary"), true, false);
  }
}
