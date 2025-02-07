/*
 * Copyright 2000-2006 JetBrains s.r.o.
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
package jetbrains.communicator.idea.history;

import com.intellij.CommonBundle;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.NonOpaquePanel;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.BaseLocalMessage;
import jetbrains.communicator.idea.ConsoleUtil;
import jetbrains.communicator.idea.IdeaDialog;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.TimeUtil;
import jetbrains.communicator.util.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Kir
 */
public class ShowHistoryDialog extends IdeaDialog {
  private final ConsoleView myConsole;
  private final User myUser;
  private final List<LocalMessage> myFoundMessages;
  private final Project myProject;

  public ShowHistoryDialog(Project project, List<LocalMessage> foundMessages, User user) {
    super(project, true);
    setModal(false);

    setTitle(StringUtil.getMsg("SearchHistoryCommand.search.results.for", user.getDisplayName()));
    setHorizontalStretch(3);
    setVerticalStretch(2);

    TextConsoleBuilder builder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
    myConsole = builder.getConsole();

    myUser = user;
    myFoundMessages = foundMessages;
    myProject = project;

    init();

    UIUtil.runWhenShown(myConsole.getComponent(), new Runnable() {
      public void run() {
        outputMessages();
      }
    });
  }

  private void outputMessages() {
    Date day = null;
    for (LocalMessage message : myFoundMessages) {

      day = printDaySeparatorIfNeeded(message, day);

      BaseLocalMessage localMessage = ((BaseLocalMessage) message);
      ConsoleUtil.outputMessage(localMessage.createConsoleMessage(myUser), myProject, myConsole);
    }

  }

  private Date printDaySeparatorIfNeeded(LocalMessage message, Date day) {
    final Date date = TimeUtil.getDay(message.getWhen());

    if (day == null || !day.equals(date) ) {
      myConsole.print("------------ " + DateFormat.getDateInstance().format(date) + " ------------\n",
          ConsoleViewContentType.NORMAL_OUTPUT);
      day = date;
    }
    return day;
  }

  @NonNls
  protected String getDimensionServiceKey() {
    return getClass().getName();
  }

  protected Action[] createActions() {
    return new Action[]{
        new AbstractAction(CommonBundle.getCloseButtonText()) {
          public void actionPerformed(ActionEvent e) {
            doCancelAction();
          }
        }
    };
  }

  protected void dispose() {
    myConsole.dispose();
    super.dispose();
  }

  @Nullable
  protected JComponent createCenterPanel() {
    return new Panel(myConsole.getComponent());
  }

  public JComponent getPreferredFocusedComponent() {
    return myConsole.getComponent();
  }

  private class Panel extends NonOpaquePanel implements DataProvider {

    public Panel(JComponent wrapped) {
      super(wrapped);
    }

    @Nullable
    public Object getData(@NonNls String dataId) {
      if (DataConstants.PROJECT.equals(dataId)) {
        return myProject;
      }
      return null;
    }
  }
}
