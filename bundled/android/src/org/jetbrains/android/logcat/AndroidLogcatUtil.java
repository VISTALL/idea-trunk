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

package org.jetbrains.android.logcat;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.android.run.LoggingReceiver;
import org.jetbrains.android.util.AndroidBundle;
import org.jetbrains.android.util.AndroidOutputReceiver;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene.Kudelevsky
 * Date: Sep 12, 2009
 * Time: 7:06:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidLogcatUtil {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.android.run.AndroidDebugRunner");
  private static Map<String, Log.LogLevel> LOG_LEVELS;
  private static int minLogLevelNameLength;
  private static int maxLogLevelNameLength;

  private AndroidLogcatUtil() {
  }

  @Nullable
  public synchronized static Log.LogLevel getLogLevel(String message) {
    if (LOG_LEVELS == null) {
      LOG_LEVELS = new HashMap<String, Log.LogLevel>();
      for (Log.LogLevel level : Log.LogLevel.values()) {
        String name = level.name();
        if (minLogLevelNameLength == 0 || name.length() < minLogLevelNameLength) {
          minLogLevelNameLength = name.length();
        }
        if (name.length() > maxLogLevelNameLength) {
          maxLogLevelNameLength = name.length();
        }
        LOG_LEVELS.put(name, level);
      }
    }
    for (int i = 0, n = message.length(); i < n; i++) {
      for (int j = i + minLogLevelNameLength; j <= i + maxLogLevelNameLength && j < n; j++) {
        String s = message.substring(i, j);
        Log.LogLevel logLevel = LOG_LEVELS.get(s);
        if (logLevel != null) return logLevel;
      }
    }
    return null;
  }

  private static void executeCommand(IDevice device, String command, AndroidOutputReceiver receiver, boolean infinite) throws IOException {
    int attempt = 0;
    while (attempt < 5) {
      device.executeShellCommand(command, receiver);
      if (infinite && !receiver.isCancelled()) {
        attempt++;
      }
      else if (receiver.isTryAgain()) {
        attempt++;
      }
      else {
        break;
      }
      receiver.invalidate();
    }
  }

  private static void startLogging(IDevice device, AndroidOutputReceiver receiver) throws IOException {
    executeCommand(device, "logcat -v long", receiver, true);
  }

  public static void clearLogcat(final Project project, IDevice device) {
    try {
      executeCommand(device, "logcat -c", new LoggingReceiver(LOG), false);
    }
    catch (final IOException e) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        public void run() {
          Messages.showErrorDialog(project, e.getMessage(), AndroidBundle.message("android.logcat.error.dialog.title"));
        }
      });
    }
  }

  @Nullable
  public static Reader startLoggingThread(final Project project, final IDevice device, final boolean clearLogcat) {
    PipedWriter logWriter = new PipedWriter();
    final AndroidLogcatReceiver receiver = new AndroidLogcatReceiver(logWriter);
    final PipedReader logReader;
    try {
      logReader = new PipedReader(logWriter) {
        @Override
        public void close() throws IOException {
          super.close();
          receiver.cancel();
        }

        @Override
        public synchronized boolean ready() {
          // We have to avoid Logging error in LogConsoleBase if logcat is finished incorrectly
          try {
            return super.ready();
          }
          catch (IOException e) {
            return false;
          }
        }
      };
    }
    catch (IOException e) {
      Messages.showErrorDialog(project, "Unable to run logcat. IOException: " + e.getMessage(), CommonBundle.getErrorTitle());
      return null;
    }
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        if (clearLogcat) {
          clearLogcat(project, device);
        }
        try {
          startLogging(device, receiver);
        }
        catch (final IOException e) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              Messages.showErrorDialog(project, e.getMessage(), AndroidBundle.message("android.logcat.error.dialog.title"));
            }
          });
        }
      }
    });
    return logReader;
  }
}
