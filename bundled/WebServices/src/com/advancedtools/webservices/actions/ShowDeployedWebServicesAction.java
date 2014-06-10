package com.advancedtools.webservices.actions;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPlugin;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.axis.AxisUtil;
import com.advancedtools.webservices.environmentFacade.EnvironmentFacade;
import com.advancedtools.webservices.references.WSDLReferenceProvider;
import com.advancedtools.webservices.utils.BaseWSAction;
import com.advancedtools.webservices.wsengine.WSEngineManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NonNls;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author maxim
 */
public class ShowDeployedWebServicesAction extends BaseWSAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    DataContext dataContext = anActionEvent.getDataContext();
    final Project project = (Project) dataContext.getData(DataConstants.PROJECT);

    final ShowDeployedWebSevicesDialog dialog = new ShowDeployedWebSevicesDialog(
      project,
      WSBundle.message("show.deployed.web.services.dialog.title"),
      "ShowDeployedWebServices.html",
      null
    );
    dialog.setOkAction(
      new Runnable() {
        public void run() {
          final String contextName = dialog.getContextName();
          final WebServicesPlugin plugin = WebServicesPlugin.getInstance(project);

          plugin.addLastContext(contextName);
          
          List<String> wsdlUrlList = new ArrayList<String>();
          List<String> wsdlList = new ArrayList<String>();
          final WSEngineManager wsEngineManager = WebServicesPluginSettings.getInstance().getEngineManager();

          for (Module module : ModuleManager.getInstance(project).getModules()) {
            if (EnvironmentFacade.getInstance().isWebModule(module)) {

              for(String wsEngine: wsEngineManager.getAvailableWSEngineNames()) {
                for(String ws:wsEngineManager.getWSEngineByName(wsEngine).getAvailableWebServices(module)) {
                  final String wsdl = AxisUtil.getServiceWsdl(contextName, ws);

                  if (wsdl != null) {
                    wsdlUrlList.add(AxisUtil.getWebServiceUrlReference(contextName, ws)+"?wsdl");
                    wsdlList.add(wsdl);
                  }
                }
              }
            }
          }

          @NonNls StringBuffer text = new StringBuffer();
          text.append("<h2>And now... Some Services</h2>");
          text.append("<ul>\n");
          
          for(int i = 0; i < wsdlUrlList.size(); ++i) {
            String wsdlUrl = wsdlUrlList.get(i);
            String wsdl = wsdlList.get(i);
            
            int qPos = wsdlUrl.lastIndexOf("?");
            int sPos = wsdlUrl.lastIndexOf('/', qPos);

            String serviceName = wsdlUrl.substring(sPos + 1, qPos);
            text.append("<li>").append(serviceName).append(" <a href=\"").append(wsdlUrl).append("\">").append("<i>wsdl</i>").append("</a></li>");
            PsiFile fileFromText = EnvironmentFacade.getInstance().createFileFromText("__1.wsdl", wsdl, project);
            
            if (fileFromText instanceof XmlFile) {
              XmlTag rootTag = ((XmlFile) fileFromText).getDocument().getRootTag();
              XmlTag[] wsdlTags = WSDLReferenceProvider.getWsdlTags(rootTag, WSDLReferenceProvider.PORT_TYPE_TAG_NAME);
  
              for(XmlTag t:wsdlTags) {
                if (serviceName.equals(t.getAttributeValue(WSDLReferenceProvider.NAME_ATTR_NAME))) {
                  XmlTag[] operations = WSDLReferenceProvider.getWsdlTags(t, WSDLReferenceProvider.OPERATION_TAG_NAME);
  
                  for(XmlTag o:operations) {
                    String name = o.getAttributeValue(WSDLReferenceProvider.NAME_ATTR_NAME);
  
                    if (name != null) {
                      text.append("<ul><li>").append(name).append("</ul>\n");
                    }
                  }
                }
              }
            }
          }
          text.append("</ul></html>");

          try {
            File tempFile = File.createTempFile("url",".html");
            tempFile.deleteOnExit();
            OutputStream out = new BufferedOutputStream( new FileOutputStream(tempFile) );

            out.write(text.toString().getBytes());
            out.close();

            plugin.navigate(tempFile.getPath());
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    );

    dialog.show();
  }
}
