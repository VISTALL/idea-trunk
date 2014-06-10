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
package org.jetbrains.idea.perforce.actions;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.GuiUtils;

import javax.swing.*;

/**
 * @author AKireyev
 */
public class MessageManager {
  private static final Logger LOG = Logger.getInstance(MessageManager.class.getName());

  public static void showMessageDialog(final Project project, final String msg, final String title, final Icon icon) {
    Runnable runnable = new Runnable() {
      public void run() {
        Messages.showMessageDialog(project, msg, title, icon);
      }
    };
    runShowAction(runnable);
  }

  public static void runShowAction(Runnable runnable) {
    try {
      GuiUtils.runOrInvokeAndWait(runnable);
    }
    catch (Exception e) {
      LOG.error(e);
    }
  }

  public static void runShowAction(final Runnable runnable, final ModalityState state) {
    final Application application = ApplicationManager.getApplication();
    if (application.isDispatchThread()) {
      runnable.run();
    }
    application.invokeAndWait(runnable, state);
  }

  public static int showDialog(final Project project, final String msg, final String title, final String[] options, final int defaultOptionIndex, final Icon icon) {
    final int result[] = new int[1];
    Runnable runnable = new Runnable() {
      public void run() {
        result[0] = Messages.showDialog(project, msg, title, options, defaultOptionIndex, icon);
      }
    };
    runShowAction(runnable);
    return result[0];
  }

  public static int showOkCancelDialog(final Project project, final String msg, final String title, final Icon icon) {
    final int result[] = new int[1];
    Runnable runnable = new Runnable() {
      public void run() {
        result[0] = Messages.showOkCancelDialog(project, msg, title, icon);
      }
    };
    runShowAction(runnable);
    return result[0];
  }

  public static int showYesNoDialog(final String msg, final String title, final Icon icon) {
    final int result[] = new int[1];
    Runnable runnable = new Runnable() {
      public void run() {
        result[0] = Messages.showYesNoDialog(msg, title, icon);
      }
    };
    runShowAction(runnable);
    return result[0];
  }

}
