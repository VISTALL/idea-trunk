package org.intellij.j2ee.web.resin.resin.configuration;

import com.intellij.execution.ExecutionException;
import org.intellij.j2ee.web.resin.resin.WebApp;
import org.jdom.Document;
import org.jdom.Element;

import java.io.InputStream;
import java.util.List;

class Resin2XConfigurationStrategy extends ResinConfigurationStrategy {
    private static final String HTTP_SERVER = "http-server";
    private static final String HTTP = "http";
    private static final String PORT = "port";
    private static final String APP_DIR = "app-dir";
    private static final String DIRTY = "dirty";
    private static final String WEB_APP = "web-app";
    private static final String ID = "id";
    private static final String HOST = "host";
    private static final String RESIN_CONF = "resin2.conf";
    private static final String CHARSET = "character-encoding";

    public boolean setPort(final int port, Document document) {
        Element httpElement =
                document.getRootElement().getChild(HTTP_SERVER).getChild(HTTP);
        if (httpElement == null) {
            httpElement = new Element(HTTP);
            httpElement.setAttribute(PORT, Integer.toString(port));
            document.getRootElement().getChild(HTTP_SERVER).addContent(httpElement);
            return true;
        } else if (!httpElement.getAttribute(PORT).getValue().equals(Integer.toString(port))) {
            httpElement.setAttribute(PORT, Integer.toString(port));
            return true;
        } else {
            return false;
        }
    }

    public boolean deploy(final WebApp webApp, final Document document) throws ExecutionException {
        boolean dirty = false;

        final Element host = getHost(document.getRootElement().getChild(HTTP_SERVER), webApp);
        if (host.getAttribute(APP_DIR) != null) {
            host.removeAttribute(APP_DIR);
            dirty = true;
        }
        if (host.getAttribute(DIRTY) != null) {
            host.removeAttribute(DIRTY);
            dirty = true;
        }

        boolean webAppFound = false;
        final List webpapps = host.getChildren(WEB_APP);
        if (webpapps != null) {
            for (Object webpapp : webpapps) {

                final Element webappEl = (Element) webpapp;
                if (webappEl.getAttribute(ID).getValue().equals(webApp.getContextPath())) {
                    webAppFound = true;

                    if (webappEl.getAttribute(APP_DIR) == null || !webappEl.getAttribute(APP_DIR).getValue().equals(webApp.getLocation())) {
                        webappEl.setAttribute(APP_DIR, webApp.getLocation());
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

            final Element newWebAppEl = new Element(WEB_APP);
            newWebAppEl.setAttribute(ID, webApp.getContextPath());
            newWebAppEl.setAttribute(APP_DIR, webApp.getLocation());
            if (webApp.getCharSet() != null && !webApp.getCharSet().trim().equals("")) {
               newWebAppEl.setAttribute(CHARSET, webApp.getCharSet());
            }

            host.addContent(newWebAppEl);
        }

        return dirty;
    }

    private Element getHost(final Element parent, final WebApp webApp) {
        final List hosts = parent.getChildren(HOST);
        if (hosts != null) {
            for (Object host1 : hosts) {
                final Element host = (Element) host1;
                if (host.getAttribute(ID).getValue().equals(webApp.getHost())) {
                    return host;
                }
            }
        }

        // Not found, create a new one
        final Element host = new Element(HOST);
        host.setAttribute(ID, webApp.getHost());
        host.setAttribute(DIRTY, "true");
        parent.addContent(host);
        return host;
    }

    public boolean undeploy(final WebApp webApp, final Document document) throws ExecutionException {
        boolean dirty = false;

        final List hosts = document.getRootElement().getChild(HTTP_SERVER).getChildren(HOST);
        if (hosts != null) {
            for (Object host1 : hosts) {
                final Element host = (Element) host1;

                final List webpapps = host.getChildren(WEB_APP);
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
