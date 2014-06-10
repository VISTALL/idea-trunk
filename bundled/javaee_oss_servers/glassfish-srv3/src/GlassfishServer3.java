/*
 * Copyright (c) 2004 - 2009 by Fuhrer Engineering AG, CH-2504 Biel/Bienne, Switzerland
 */

package com.fuhrer.idea.glassfish.server;

import com.fuhrer.idea.glassfish.GlassfishUtil;
import com.fuhrer.idea.glassfish.model.GlassfishAppRoot;
import com.fuhrer.idea.glassfish.model.GlassfishWebModule;
import com.fuhrer.idea.glassfish.model.GlassfishWebRoot;
import com.fuhrer.idea.javaee.server.JavaeeServer;
import com.intellij.javaee.deployment.DeploymentModel;
import com.intellij.javaee.deployment.DeploymentStatus;
import com.intellij.javaee.facet.JavaeeFacet;
import com.intellij.javaee.model.xml.application.JavaeeModule;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.sun.enterprise.admin.cli.remote.RemoteException;
import com.sun.enterprise.admin.cli.remote.RemoteResponseManager;
import com.sun.enterprise.admin.cli.remote.RemoteSuccessException;
import com.sun.enterprise.admin.cli.util.AuthenticationInfo;
import com.sun.enterprise.admin.cli.util.HttpConnectorAddress;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GlassfishServer3 extends JavaeeServer {

    @Override
    protected boolean isConnected() throws Exception {
        return invoke("version").length > 0;
    }

    @Override
    @NotNull
    protected DeploymentStatus handleDeployment(DeploymentModel deployment, File source, boolean deploy, boolean undeploy) throws Exception {
        String name = FileUtil.getNameWithoutExtension(source);
        if (undeploy && !deploy) {
            invoke("undeploy?DEFAULT=" + URLEncoder.encode(name, "UTF-8"));
        }
        if (deploy) {
            @NonNls String command = "deploy?force=true";
            if (((GlassfishServerModel) deployment.getCommonModel().getServerModel()).PRESERVE) {
                command += "&keepSessions=true";
            }
            if (source.isDirectory() || !isRemote(deployment)) {
                invoke(command + "&path=" + URLEncoder.encode(source.getAbsolutePath(), "UTF-8"));
            } else {
                invoke(command + "&path=" + URLEncoder.encode(source.getName(), "UTF-8"), source);
            }
        }
        return getDeploymentStatus(name);
    }

    @Override
    @Nullable
    protected String getContextRoot(JavaeeFacet facet) {
        GlassfishWebRoot web = GlassfishUtil.getWebRoot(facet);
        return web != null ? web.getContextRoot().getValue() : null;
    }

    @Override
    protected void getContextRoots(JavaeeFacet facet, Map<String, String> roots) {
        GlassfishAppRoot app = GlassfishUtil.getAppRoot(facet);
        if (app != null) {
            for (GlassfishWebModule web : app.getWebs()) {
                JavaeeModule source = web.getWebUri().getValue();
                if ((source != null) && !StringUtil.isEmpty(web.getContextRoot().getValue())) {
                    roots.put(source.getId().getValue(), web.getContextRoot().getValue());
                }
            }
        }
    }

    @Override
    protected boolean isStartupScriptTerminating() {
        return true;
    }

    private DeploymentStatus getDeploymentStatus(String name) {
        DeploymentStatus status = DeploymentStatus.NOT_DEPLOYED;
        try {
            for (String part : invoke("list-components")) {
                if (name.equals(part.split(" <")[0])) {
                    status = DeploymentStatus.DEPLOYED;
                }
            }
        } catch (Exception e) {
            status = DeploymentStatus.UNKNOWN;
        }
        return status;
    }

    private String[] invoke(@NonNls String command) throws IOException, RemoteException {
        @NonNls HttpURLConnection connection = getConnection(command, "GET");
        connection.connect();
        return parseResponse(connection);
    }

    private String[] invoke(@NonNls String command, @NotNull File file) throws IOException, RemoteException {
        @NonNls HttpURLConnection connection = getConnection(command, "POST");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/zip");
        connection.setChunkedStreamingMode(0);
        connection.connect();
        ZipOutputStream zip = new ZipOutputStream(connection.getOutputStream());
        ZipEntry entry = new ZipEntry(file.getName());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        @NonNls Properties properties = new Properties();
        properties.setProperty("Content-Type", "application/octet-stream");
        properties.setProperty("data-request-type", "file-xfer");
        properties.setProperty("data-request-name", "path");
        properties.setProperty("last-modified", Long.toString(file.lastModified()));
        properties.store(out, null);
        entry.setExtra(out.toByteArray());
        zip.putNextEntry(entry);
        FileUtil.copy(new BufferedInputStream(new FileInputStream(file)), zip);
        zip.closeEntry();
        return parseResponse(connection);
    }

    private HttpURLConnection getConnection(@NonNls String command, @NonNls String method) throws IOException {
        @NonNls HttpConnectorAddress url = new HttpConnectorAddress(getHost(), getPort(), false);
        url.setAuthenticationInfo(new AuthenticationInfo(getUsername(), getPassword()));
        @NonNls HttpURLConnection connection = (HttpURLConnection) url.openConnection("/__asadmin/" + command);
        connection.setRequestMethod(method);
        connection.setRequestProperty("User-Agent", "hk2-agent");
        connection.setRequestProperty(HttpConnectorAddress.AUTHORIZATION_KEY, url.getBasicAuthString());
        return connection;
    }

    private String[] parseResponse(HttpURLConnection connection) throws RemoteException, IOException {
        try {
            new RemoteResponseManager(connection.getInputStream(), connection.getResponseCode()).process();
        } catch (RemoteSuccessException e) {
            return e.getMessage().split("[\\r\\n]+");
        }
        return ArrayUtil.EMPTY_STRING_ARRAY;
    }
}
