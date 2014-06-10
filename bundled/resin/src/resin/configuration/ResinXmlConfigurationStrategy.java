package org.intellij.j2ee.web.resin.resin.configuration;

import com.intellij.execution.ExecutionException;
import org.intellij.j2ee.web.resin.ResinBundle;
import org.intellij.j2ee.web.resin.resin.ResinInstallation;
import org.intellij.j2ee.web.resin.resin.WebApp;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

class ResinXmlConfigurationStrategy extends Resin3XConfigurationStrategy {
    //Constants
    protected static final String RESIN_CONF = "resin32.xml";

    //Variables
    private final boolean resolveResinImports = true;
    private ResinInstallation resinInstallation = null;

    ResinXmlConfigurationStrategy(ResinInstallation resinInstallation){
        super();
        this.resinInstallation = resinInstallation;
    }

  @Override
  public boolean deploy(WebApp webApp, Document document) throws ExecutionException {
    if(resolveResinImports)
        resolveImports(document);
    return super.deploy(webApp, document);
  }


    public InputStream getDefaultResinConfContent() {
        return this.getClass().getResourceAsStream(RESIN_CONF);
    }

    /**
     * This method will resolve every import turning ${__DIR__} into &lt;resin_home&gt;/conf
     * @param document the document
     */
    protected void resolveImports(Document document){
        if(resinInstallation == null || document == null)
            return; //Should never happend

        resolveImports(document.getRootElement());
    }
    
  protected Element getHost(final Document doc, final Namespace ns, final WebApp webApp) throws ExecutionException {
      try{
          //Resin 3.2
          //      <root>
          //          <cluster>
          //              <host>
          Element root = doc.getRootElement();
          Element parent = root.getChild(CLUSTER, ns);
          // Not found, create a new one
          if(parent == null){
              parent = new Element(CLUSTER, ns);
              root.addContent(parent);
          }
          else{
              final List hosts = parent.getChildren(HOST, ns);
              if (hosts != null) {
                  for (Object host1 : hosts) {
                      final Element host = (Element) host1;
                      if (host.getAttribute(ID).getValue().equals(webApp.getHost()))
                          return host;
                  }
              }
          }

          // Not found, create a new one
          final Element host = new Element(HOST, ns);
          host.setAttribute(ID, webApp.getHost());
          host.setAttribute(DIRTY, "true");
          parent.addContent(host);
          return host;
      }
      catch(Exception e){
          throw new ExecutionException(ResinBundle.message("resin.conf.parse.error"));
      }
  }

    /**
     * This method will resolve every import turning ${__DIR__} into &lt;resin_home&gt;/conf
     * @param root the root element
     */
    protected void resolveImports(Element root){
        if(root == null)
            return;

        try{
            if("import".equals(root.getName())){
                Attribute path = root.getAttribute("path");
                if(path != null){
                    String strPath = path.getValue();
                    if(strPath != null){
                        String resinHome = resinInstallation.getResinHome().getAbsolutePath();
                        resinHome = resinHome.replaceAll("\\\\", "/");
                        if(!resinHome.endsWith("/"))
                            resinHome += "/";
                        resinHome += "conf";
                        strPath = strPath.replaceAll("\\$\\{__DIR__\\}", resinHome);
                        root.setAttribute("path", strPath);
                    }
                }
            }
            else{
                List children = root.getChildren();
                if(children == null)
                    return;

                Iterator<Element> it = (Iterator<Element>) children.iterator();
                while (it.hasNext()){
                    resolveImports(it.next());
                }
            }
        }
        catch(Exception ignore){
            //Unable to resolve imports
        }
    }

}
