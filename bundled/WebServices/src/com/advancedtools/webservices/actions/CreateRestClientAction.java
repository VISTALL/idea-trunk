package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.rest.client.RESTClient;
import com.advancedtools.webservices.utils.BaseWSAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NonNls;

/**
 * @by Konstantin Bulenkov
 */
public class CreateRestClientAction extends BaseWSAction {
  @NonNls public static final String REST_CLIENT = WSBundle.message("reference.tool.windows.rest.client");

  public void actionPerformed(final AnActionEvent e) {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null) {
      project = ProjectManager.getInstance().getDefaultProject();
    }
    ToolWindowManager manager = ToolWindowManager.getInstance(project);
    ToolWindow w = manager.getToolWindow(REST_CLIENT);
    if (w == null) {
      RESTClient form = new RESTClient(project);
      w = manager.registerToolWindow(REST_CLIENT, true, ToolWindowAnchor.BOTTOM);
      final Content content = ContentFactory.SERVICE.getInstance().createContent(form.getComponent(), "", false);
      content.setDisposer(form);
      content.setCloseable(true);
      w.getContentManager().addContent(content);
      w.setIcon(IconLoader.getIcon("/com/advancedtools/webservices/rest/client/icons/rest_client_icon_small.png"));
    }
    w.show(null);
    w.activate(null);        
  }
}
