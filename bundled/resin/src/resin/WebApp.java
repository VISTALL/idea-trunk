package org.intellij.j2ee.web.resin.resin;

public class WebApp {
    private String location;
    private String contextPath;
    private String host;
    private String charset;

    public WebApp(String contextPath, String host, String location, String charset) {
        this.contextPath = contextPath;
        this.host = host;
        this.location = location;
        this.charset = charset;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public String getHost() {
        if (host == null) return "";
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getCharSet() {
        return charset;
    }

    public void setCharSet(String charset) {
      this.charset = charset;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WebApp)) return false;

        final WebApp webApp = (WebApp) o;

        if (contextPath != null ? !contextPath.equals(webApp.contextPath) : webApp.contextPath != null) return false;
        if (host != null ? !host.equals(webApp.host) : webApp.host != null) return false;
        //noinspection RedundantIfStatement
        if (location != null ? !location.equals(webApp.location) : webApp.location != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (location != null ? location.hashCode() : 0);
        result = 29 * result + (contextPath != null ? contextPath.hashCode() : 0);
        result = 29 * result + (host != null ? host.hashCode() : 0);
        return result;
    }
}
