package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.utils.BaseWSAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;

/**
 * @author maxim
 * Date: 05.12.2004
 */
public class EnableWebServicesSupportAction extends BaseWSAction {

  public void actionPerformed(AnActionEvent anActionEvent) {
    runAction((Project) anActionEvent.getDataContext().getData(DataConstants.PROJECT), null);
  }

  private void runAction(final Project project, EnableWebServicesSupportDialog previousDialog) {
    final EnableWebServicesSupportDialog dialog = new EnableWebServicesSupportDialog(project, previousDialog);

    dialog.setOkAction(
      new Runnable() {
        public void run() {
          EnableWebServicesSupportUtils.setupWebServicesInfrastructureForModule(dialog, project, true);
          if (dialog.isToGenerateSampleCode()) {
            if (dialog.isServerSideSupport()) {
              EnableWebServicesSupportUtils.createCodeForServer(
                dialog.getModule(),
                dialog.getWsEngine(),
                dialog.getPackageNameToCreate(),
                dialog.getClassNameToCreate(),
                new Function<Exception, Void>() {
                  public Void fun(Exception e) {
                    e.printStackTrace();
                    return null;
                  }
                },
                new Runnable() {
                  public void run() {
                    runAction(project, dialog);
                  }
                },
                null
              );
            }
            else {
              EnableWebServicesSupportUtils.createCodeForClient(
                dialog.getModule(),
                dialog.getWsEngine(),
                dialog.getPackageNameToCreate(),
                dialog.getClassNameToCreate()
              );
            }
          }
        }
      }
    );

    dialog.show();
  }
}
