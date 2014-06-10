/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.advancedtools.webservices.rest.client;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.RestUtils;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.BackgroundTaskQueue;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.net.HttpConfigurable;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Konstantin Bulenkov
 */
public class RestClientControllerImpl implements RestClientController {
  private final RestClientModel model;
  @NonNls private static final String EMPTY_RESPONSE = "No reply has been produced";
  @NonNls private static final String REFORMAT_DOCUMENT = "Response reformating...";
  private Project project;

  public RestClientControllerImpl(RestClientModel model, Project project) {
    this.model = model;
    this.project = project;
  }

  public void onUpdateURLs() {
    Set<String> allPaths = new HashSet<String>();
    allPaths.addAll(Arrays.asList(RestUtils.getAllPathes(project)));
    model.setURLTemplates(allPaths);
    model.updateModel(project);
  }

  public void onGoButtonClick() {
    model.setResponse("");
    model.setStatus("");
    new BackgroundTaskQueue(project, WSBundle.message("accessing.url", model.getURL()))
      .run(new Task.Backgroundable(project, WSBundle.message("accessing.url", model.getURL()), true) {
        public void run(@NotNull ProgressIndicator indicator) {
          try {
            HttpClient client = createHttpClient();
            String url = model.getURL();
            final HttpMethod method = createHttpMethod(url);
            try {
              URL uri = new URL(url);
              String userinfo = uri.getUserInfo();
              if (userinfo != null && userinfo.length() > 0) {
                client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userinfo));
                method.setDoAuthentication(true);
              }
            }
            catch (MalformedURLException e) {//
            }


            addHeaders(method);
            initParameters(method);

            final int code = client.executeMethod(method);
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                String text = "Response code: " + code;
                final String status = method.getStatusText();
                if (status != null && status.length() > 0) {
                  text += "(" + status + ")";
                }
                model.setStatus(text);
              }
            });
            final Header contentLength = method.getResponseHeader("Content-Length");
            final Long length = contentLength == null ? null : fromString(contentLength.getValue());
            if (length != null) {
              indicator.setIndeterminate(false);
              indicator.setText("Accessing resource");
              indicator.setFraction(0.0);
            }

            final InputStream body = method.getResponseBodyAsStream();
            if (body == null) {
              ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                  model.setResponse(EMPTY_RESPONSE);
                }
              });
              return;
            }

            final byte[] b = new byte[4096];
            int len;
            final StringBuffer buf = new StringBuffer();
            while ((len = body.read(b)) != -1 && !indicator.isCanceled()) {
              buf.append(new String(b, 0, len));
              if (length != null) {
                indicator.setText((buf.length() / 1000) + " of " + (length.longValue() / 1000) + "Kb");
                indicator.setFraction((double)buf.length() / (double)length.longValue());
              }
            }

            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                model.appendToResponse(buf.toString());
                if (model.getResponse().length() == 0) {
                  model.setResponse(EMPTY_RESPONSE);
                }
                String text = "Response code: " + code;
                final String status = method.getStatusText();
                if (status != null && status.length() > 0) {
                  text += "(" + status + ")";
                }
                text += "; Content length: " + buf.length() + " bytes";
                model.setStatus(text);
                model.setResponseHeader(RestClientControllerImpl.toString(method.getResponseHeaders()));
              }
            });


            WebServicesPluginSettings.getInstance().setLastRestClientHost(model.getURLBase());
          }
          catch (final Exception ex) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
              public void run() {
                model.setResponse(ex.toString());
              }
            });
          }
        }
      });
  }

  private void addHeaders(HttpMethod method) {
    for (int i = 0; i < model.getHeaderSize(); i++) {
      method.addRequestHeader(model.getHeaderName(i), model.getHeaderValue(i));
    }
  }

  private static String toString(Header[] headers) {
    StringBuilder builder = new StringBuilder();
    for (Header header : headers) {
      builder.append(header.toString());
    }
    return builder.toString();
  }

  private HttpClient createHttpClient() {
    HttpClientParams params = new HttpClientParams();
    HttpClient client = new HttpClient(params);
    final HttpConfigurable proxy = HttpConfigurable.getInstance();
    if (proxy.USE_HTTP_PROXY) {
      client.getHostConfiguration().setProxy(proxy.PROXY_HOST, proxy.PROXY_PORT);
      if (proxy.PROXY_AUTHENTICATION) {
        final AuthScope authScope = new AuthScope(proxy.PROXY_HOST, proxy.PROXY_PORT);
        final UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxy.PROXY_LOGIN, proxy.getPlainProxyPassword());
        client.getState().setProxyCredentials(authScope, credentials);
      }
    }
    return client;
  }


  private void initParameters(HttpMethod method) {
    if (method instanceof PostMethod) {
      for (int i = 0; i < model.getParametersSize(); i++) {
        method.getParams().setParameter(model.getParameterName(i), model.getParameterValue(i));
      }
    }
  }

  public boolean isValidURL(final String url) {
    try {
      new URL(url);
      return true;
    }
    catch (Exception ex) {
      return false;
    }
  }

  private String createQueryString() {
    StringBuilder builder = new StringBuilder();
    String charset = Charset.defaultCharset().name();
    for (int i = 0; i < model.getParametersSize(); i++) {
      try {
        builder.append(URLEncoder.encode(model.getParameterName(i), charset)).append("=")
          .append(URLEncoder.encode(model.getParameterValue(i), charset));
      }
      catch (Exception e) {//
      }
      if (i != model.getParametersSize() - 1) {
        builder.append("&");
      }
    }
    return builder.toString();
  }

  private HttpMethod createHttpMethod(String uri) {
    if (model.isParametersEnabled() && model.getHttpMethod() != HTTPMethod.POST) {
      String query = createQueryString();
      uri += (uri.contains("?") ? "&" : "?") + query;
    }

    switch (model.getHttpMethod()) {
      case GET:
        return new GetMethod(uri);
      case HEAD:
        return new HeadMethod(uri);
      case DELETE:
        return new DeleteMethod(uri);
      case PUT:
        return new PutMethod(uri);
      case TRACE:
        return new TraceMethod(uri);
      case OPTIONS:
        return new OptionsMethod(uri);
      case POST:
        PostMethod method = new PostMethod(uri);
        if (model.isParametersEnabled()) {
          for (int i = 0; i < model.getParametersSize(); i++) {
            method.addParameter(model.getParameterName(i), model.getParameterValue(i));
          }
        }
        return method;
    }
    return null;
  }

  public void openResponseInEditorAs(@NonNls String fileExtension, final Project project) {
    VirtualFile vf = null;
    try {
      File file = FileUtil.createTempFile("tmpRestClientResponse", "." + fileExtension);
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(model.getResponse().getBytes());
      fos.close();
      vf = VirtualFileManager.getInstance().refreshAndFindFileByUrl(LocalFileSystem.PROTOCOL_PREFIX + file.getAbsolutePath());
    }
    catch (IOException ex) {//
    }
    final FileEditorManager editorManager = FileEditorManager.getInstance(project);
    if (vf != null) {
      final PsiFile file = PsiManager.getInstance(project).findFile(vf);
      boolean opened = editorManager.isFileOpen(vf);
      if (file != null && file.getChildren().length > 0) {
        if (opened) {
          vf.refresh(false, false);
        }
        editorManager.openFile(vf, true);

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
          public void run() {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
              public void run() {
                try {
                  CodeStyleManager.getInstance(project).reformatText(file, 0, file.getTextLength());
                }
                catch (Exception e) {//
                }
              }
            });
          }
        }, REFORMAT_DOCUMENT, null);
      }
    }
  }

  public void openResponseInBrowser() {
    VirtualFile vf = null;
    try {
      File file = FileUtil.createTempFile("tmpRestClientResponse", ".html");
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(model.getResponse().getBytes());
      fos.close();
      BrowserUtil.launchBrowser(file.getAbsolutePath());
    }
    catch (IOException ex) {//
    }
  }

  @Nullable
  private static Long fromString(String num) {
    try {
      return new Long(num);
    } catch (Exception e) {
      return null;
    }
  }
}
