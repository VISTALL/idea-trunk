package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.utils.BaseWSFromFileAction;
import com.advancedtools.webservices.wsengine.WSEngine;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import org.jetbrains.annotations.Nullable;

/**
 * @by Maxim.Mossienko
 */
public class UndeployWebServiceAction extends BaseWSFromFileAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    final Project project = (Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT);
    runAction(project, null);
  }

  private void runAction(Project project, @Nullable UndeployWebServiceDialog previousDialog) {
    final UndeployWebServiceDialog dialog = new UndeployWebServiceDialog(
      project,
      previousDialog
    );

    dialog.show();

    dialog.setOkAction(new Runnable() {
      public void run() {
        runUndeployment(dialog);
      }
    });
  }

  private void runUndeployment(final UndeployWebServiceDialog dialog) {
    final WSEngine currentEngine = dialog.getCurrentEngine();

    currentEngine.undeployWebService(
      dialog.getWSName(),
      dialog.getWsModule(),
      new Runnable() {
        public void run() {
        }
      },
      new Function<Exception, Void>() {
        public Void fun(Exception e) {
          return null;
        }
      },
      new Runnable() {
        public void run() {
          runAction(dialog.getProject(), dialog);
        }
      },
      null);
  }
}
