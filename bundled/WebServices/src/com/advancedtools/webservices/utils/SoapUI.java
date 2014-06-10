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

package com.advancedtools.webservices.utils;

import com.advancedtools.webservices.WSBundle;
import com.advancedtools.webservices.WebServicesPluginSettings;
import com.intellij.ide.IdeBundle;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.ex.ApplicationManagerEx;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.UrlConnectionUtil;
import com.intellij.util.io.ZipUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.jar.Pack200;
import java.util.zip.GZIPInputStream;

/**
 * @author Konstantin Bulenkov
 */
public class SoapUI {
  public static final @NonNls PluginId ID = PluginId.getId("soapUI Plugin");
  private static final @NonNls String[] OPTIONS = {"Yes", "No", "Don't ask me again"};
  @NonNls private static final String SOAP_UI = "SoapUI";
  @NonNls private static final String PACK_GZ_SUFFIX = ".pack.gz";
  @NonNls private static final String JAR_SUFFIX = ".jar";

  private SoapUI() {}

  public static void installSoapUI(final @NotNull Project project) {
    if (PluginManager.getPlugin(SoapUI.ID) != null
        || ! WebServicesPluginSettings.getInstance().isAllowedToAskAboutSoapUI()) return;

    int result = Messages.showDialog(project,
                                     WSBundle.message("install.soapui.plugin"),
                                     SOAP_UI,
                                     OPTIONS,
                                     0,
                                     Messages.getQuestionIcon());
    if (result == 2) {
      WebServicesPluginSettings.getInstance().setAllowedToAskInstallSoapUI(false);
    } else if (result == 0) {
      install(project);
    }
  }

  public static void install(final Project p) {
    SoapUIDownloader process = new SoapUIDownloader();
    boolean completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(process,
                                                                      WSBundle.message("downloading.and.installing.soapui"),
                                                                      true,
                                                                      p);
    if (completed) {
      if (process.isCompletedSuccessfully()) {
        int result = Messages.showOkCancelDialog(WSBundle.message("soapui.instalation.complete"),
                            WSBundle.message("installation.complete"),
                            Messages.getInformationIcon());
        if (result == 0) {
          ApplicationManagerEx.getApplicationEx().exit();
        }
      } else {
        Messages.showErrorDialog(WSBundle.message("soapui.installation.error", process.getMessage()),
                                 WSBundle.message("soapui.error"));
      }
    }
  }



  private static class SoapUIDownloader implements Runnable {
    private boolean ok = false;
    private Exception e = null;
    private static final int TIMEOUT = 30*1000;
    @NonNls private static final String SOAPUI_URL = "http://download.jetbrains.com/idea/j2ee_libs/soapui/intellij-soapui-plugin-3.0.zip";

    public void run() {
      final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
      final @NonNls String presentableUrl = "http://download.jetbrains.com";
      final String url = SOAPUI_URL;
      indicator.setText2(IdeBundle.message("progress.connecting.to.dowload.jar.text", presentableUrl));
      indicator.setIndeterminate(true);
      try {
      HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
      connection.setConnectTimeout(TIMEOUT);
      connection.setReadTimeout(TIMEOUT);

      InputStream input = null;
      BufferedOutputStream output = null;
      File tempFile = null;
      try {
        final int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
          throw new IOException(IdeBundle.message("error.connection.failed.with.http.code.N", responseCode));
        }

        final int size = connection.getContentLength();

        tempFile = FileUtil.createTempFile("soapui", ".zip");
        input = UrlConnectionUtil.getConnectionInputStreamWithException(connection, indicator);
        output = new BufferedOutputStream(new FileOutputStream(tempFile));
        indicator.setText2(WSBundle.message("progress.download.soapui.text", presentableUrl));
        indicator.setIndeterminate(size == -1);

        int len;
        final byte[] buf = new byte[1024];
        int count = 0;
        while ((len = input.read(buf)) > 0) {
          indicator.checkCanceled();
          count += len;
          if (size > 0) {
            indicator.setFraction((double)count / size);
          }
          output.write(buf, 0, len);
        }
      }
      finally {
        if (input != null) input.close();
        if (output != null) output.close();
        connection.disconnect();
        if (tempFile != null) {
          File to = new File(PathManager.getPluginsPath());
          ZipUtil.extract(tempFile, to, null);
          FileUtil.delete(tempFile);
          findAndUnpack200(to);
          ok = true;
        }
      }
      } catch (Exception ex) {//
        ok = false;
        e = ex;
      }
    }

  private static void findAndUnpack200(final File file) {
    if (file.isDirectory()) {
      for (File f: file.listFiles()) {
        findAndUnpack200(f);
      }
    } else if (file.isFile() && file.getName().endsWith(PACK_GZ_SUFFIX)) {
      String jarPath = file.getAbsolutePath();
      jarPath = jarPath.substring(0, jarPath.length() - PACK_GZ_SUFFIX.length());

      if (jarPath.length() == 0) return;

      if (!jarPath.endsWith(JAR_SUFFIX)) jarPath += JAR_SUFFIX;
      try {
        unpack200(file, jarPath);
        file.deleteOnExit();
      } catch (IOException ex){//
      }
    }
  }

  private static void unpack200(File pack200, String jarPath) throws IOException {
    OutputStream out = new BufferedOutputStream(new FileOutputStream(jarPath));
    JarOutputStream jarOutputStream = new JarOutputStream(out);
    GZIPInputStream inputStream = new GZIPInputStream(new BufferedInputStream(new FileInputStream(pack200)));
    Pack200.newUnpacker().unpack(inputStream, jarOutputStream);
    inputStream.close();
    jarOutputStream.close();
    if (!pack200.delete()) pack200.deleteOnExit();
  }    

    public boolean isCompletedSuccessfully() {
      return ok;
    }

    public String getMessage() {
      return e == null ? "" : e.getMessage();
    }
  }


}
