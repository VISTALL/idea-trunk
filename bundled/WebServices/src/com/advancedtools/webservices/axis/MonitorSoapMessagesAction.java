package com.advancedtools.webservices.axis;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.PsiManager;
import com.advancedtools.webservices.utils.BaseWSAction;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.actions.ShowDeployedWebSevicesDialog;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: maxim
 * Date: 03.01.2006
 * Time: 18:09:31
 * To change this template use File | Settings | File Templates.
 */
public class MonitorSoapMessagesAction extends BaseWSAction {
  public void actionPerformed(AnActionEvent e) {
    DataContext dataContext = e.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);

    final ShowDeployedWebSevicesDialog dialog = new ShowDeployedWebSevicesDialog(
      project,
      "Monitor SOAP Messages",
      "MonitorSoapMessages.html",
      getPossiblePorts(project)
    );

    dialog.setOkAction(
      new Runnable() {
        public void run() {
          String contextName = dialog.getContextName();
          WebServicesPlugin instance = WebServicesPlugin.getInstance(project);
          instance.addLastContext(contextName);

          int port = dialog.getPort();
          if (!instance.hasSoapMessagesToolWindow(port)) AxisUtil.ensureSoapMonitorDeployed(contextName, project);
          instance.createOrShowSoapMessagesToolWindow(port);
        }
      }
    );

    dialog.show();
  }

  private static List<String> getPossiblePorts(final Project myProject) {
    final List<String> ports = new ArrayList<String>(1);
    ProjectRootManager.getInstance(myProject).getFileIndex().iterateContent(
      new ContentIterator() {
        public boolean processFile(VirtualFile fileOrDir) {
          if (fileOrDir.getName().equals("web.xml")) {
            final XmlFile file = (XmlFile) PsiManager.getInstance(myProject).findFile(fileOrDir);
            final XmlTag rootTag = file.getDocument().getRootTag();
            final XmlTag[] tags = rootTag.findSubTags("servlet");
            final XmlTag servlet = LibUtils.findServletWithName(tags, "SOAPMonitorService");

            if (servlet != null) {
              final XmlTag[] subTags = servlet.findSubTags("init-param");

              for(XmlTag st:subTags) {
                final XmlTag firstSubTag = st.findFirstSubTag("param-name");

                if (firstSubTag != null &&
                    "SOAPMonitorPort".equals(LibUtils.getStringValue(firstSubTag))) {
                  final XmlTag value = st.findFirstSubTag("param-value");

                  if (value != null) {
                    final String stringValue = LibUtils.getStringValue(value);

                    if (stringValue != null) {
                      try {
                        int i = Integer.parseInt(stringValue);
                        ports.add(stringValue);
                      } catch(NumberFormatException ex) {}
                    }
                  }
                }
              }
            }
          }
          return true;
        }
      }
    );

    return ports;
  }
}
