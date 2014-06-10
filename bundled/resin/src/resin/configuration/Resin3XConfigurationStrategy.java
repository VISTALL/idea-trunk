package org.intellij.j2ee.web.resin.resin.configuration;

import com.intellij.execution.ExecutionException;
import org.intellij.j2ee.web.resin.ResinBundle;
import org.intellij.j2ee.web.resin.resin.WebApp;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.io.File;
import java.io.InputStream;
import java.util.List;

class Resin3XConfigurationStrategy extends ResinConfigurationStrategy {
    protected static final String SERVER = "server";
    protected static final String SERVER_DEFAULT = "server-default";
    protected static final String HTTP = "http";
    protected static final String PORT = "port";
    protected static final String DIRTY = "dirty";
    protected static final String WEB_APP = "web-app";
    protected static final String ID = "id";
    protected static final String DOCUMENT_DIRECTORY = "document-directory";
    protected static final String HOST = "host";
    protected static final String RESIN_CONF = "resin3.conf";
    protected static final String CHARSET = "character-encoding";
    protected static final String CLUSTER = "cluster";

  public boolean setPort(final int port, Document document) {
        try{
            Element root = document.getRootElement();
            final Namespace ns = root.getNamespace();
            Element server = root.getChild(SERVER, ns);
            if(server == null){
                Element cluster = root.getChild("cluster", ns);
                server = cluster.getChild(SERVER_DEFAULT, ns);
            }
            Element httpElement = server.getChild(HTTP, ns);
            if (httpElement == null) {
                httpElement = new Element(HTTP, ns);
                try{
                    //Resin 3.0.X
                    httpElement.setAttribute(PORT, Integer.toString(port), ns);
                    server.addContent(httpElement);
                }
                catch(Exception ignore){
                    return false;
                }
                return true;
            }
            else if (!httpElement.getAttribute(PORT).getValue().equals(Integer.toString(port))) {
                httpElement.setAttribute(PORT, Integer.toString(port));
                return true;
            }
            else {
                return false;
            }
        }
        catch(Exception e){
            //Unable to set port... happens with resin 3.1.3
            System.out.println(ResinBundle.message("run.resin.set.port.error"));
            return false;
        }
    }

  public boolean deploy(final WebApp webApp, Document document) throws ExecutionException {
      boolean dirty = false;

      final Namespace ns = document.getRootElement().getNamespace();

      final Element host = getHost(document, ns, webApp);
      if (host.getAttribute(DIRTY) != null) {
          host.removeAttribute(DIRTY);
          dirty = true;
      }

      boolean webAppFound = false;
      final List webpapps = host.getChildren(WEB_APP, ns);
    String rootAttribute = (new File(webApp.getLocation()).isDirectory()) ? DOCUMENT_DIRECTORY : "archive-path";
    if (webpapps != null) {
          for (Object webpapp : webpapps) {

              final Element webappEl = (Element) webpapp;
              if (webappEl.getAttribute(ID).getValue().equals(webApp.getContextPath())) {
                  webAppFound = true;

                  if (webappEl.getAttribute(rootAttribute) == null || !webappEl.getAttribute(rootAttribute).getValue().equals(webApp.getLocation())) {
                      webappEl.setAttribute(rootAttribute, webApp.getLocation());
                      dirty = true;
                  }
                  if (webappEl.getAttribute(CHARSET) == null || !webappEl.getAttribute(CHARSET).getValue().equals(webApp.getCharSet())) {
                      if (webApp.getCharSet() != null && !webApp.getCharSet().trim().equals("")) {
                          webappEl.setAttribute(CHARSET, webApp.getCharSet());
                          dirty = true;
                      }
                  }
              }
          }
      }

      if (!webAppFound) {
          dirty = true;

          final Element newWebAppEl = new Element(WEB_APP, ns);
          newWebAppEl.setAttribute(ID, webApp.getContextPath());
          newWebAppEl.setAttribute(rootAttribute, webApp.getLocation());
          if (webApp.getCharSet() != null && !webApp.getCharSet().trim().equals("")) {
             newWebAppEl.setAttribute(CHARSET, webApp.getCharSet());
          }

          host.addContent(newWebAppEl);
      }


      return dirty;
  }

    protected Element getHost(final Document doc, final Namespace ns, final WebApp webApp) throws ExecutionException {
        try{
            //Resin 3.x
            //      <root>
            //          <server>
            //              <host>
            Element root = doc.getRootElement();
            Element parent = root.getChild(SERVER, ns);
            // Not found, create a new one
            if(parent == null){
                parent = new Element(SERVER, ns);
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

    public boolean undeploy(final WebApp webApp, final Document document) throws ExecutionException {
        boolean dirty = false;

        final Namespace ns = document.getRootElement().getNamespace();

        final List hosts = document.getRootElement().getChild(CLUSTER, ns).getChildren(HOST, ns);
        if (hosts != null) {
            for (Object host1 : hosts) {
                final Element host = (Element) host1;

                final List webpapps = host.getChildren(WEB_APP, ns);
                if (webpapps != null) {
                    for (Object webpapp : webpapps) {
                        final Element webappEl = (Element) webpapp;
                        if (webappEl.getAttribute(ID).getValue().equals(webApp.getContextPath())) {
                            host.removeContent(webappEl);
                            dirty = true;
                        }
                    }
                }
            }
        }

        return dirty;
    }

    public InputStream getDefaultResinConfContent() {
        return this.getClass().getResourceAsStream(RESIN_CONF);
    }

}
