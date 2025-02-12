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

package org.jetbrains.android.run.testing;

import com.intellij.execution.configurations.RuntimeConfiguration;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.util.config.Storage;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Aug 28, 2009
 * Time: 1:20:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidTestConsoleProperties extends TestConsoleProperties {
  private final RuntimeConfiguration myConfig;

  public AndroidTestConsoleProperties(final AndroidTestRunConfiguration configuration) {
    super(new Storage.PropertiesComponentStorage("Android.", PropertiesComponent.getInstance()), configuration.getProject());
    myConfig = configuration;
  }

  @Override
  public boolean isDebug() {
    return getDebugSession() != null;
  }

  @Override
  public boolean isPaused() {
    final XDebugSession debuggerSession = getDebugSession();
    return debuggerSession != null && debuggerSession.isPaused();
  }

  public RuntimeConfiguration getConfiguration() {
    return myConfig;
  }

  @Nullable
  public XDebugSession getDebugSession() {
    final XDebuggerManager debuggerManager = XDebuggerManager.getInstance(getProject());
    if (debuggerManager == null) {
      return null;
    }
    final XDebugSession[] sessions = debuggerManager.getDebugSessions();
    for (final XDebugSession debuggerSession : sessions) {
      if (getConsole() == debuggerSession.getRunContentDescriptor().getExecutionConsole()) {
        return debuggerSession;
      }
    }
    return null;
  }
}
