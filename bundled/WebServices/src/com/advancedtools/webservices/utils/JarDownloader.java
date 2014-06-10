package com.advancedtools.webservices.utils;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.io.UrlConnectionUtil;
import org.jetbrains.annotations.NonNls;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class JarDownloader implements Runnable {
  private final List<String> urls;
  private final File dir;

  /**
   *
   * @param urls urls to jar
   * @param dir folder where to save jars
   */
  public JarDownloader(List<String> urls, File dir) {
    this.urls = urls;
    this.dir = dir;
  }

  private boolean ok = false;
  private Exception e = null;
  private static final int TIMEOUT = 30 * 1000;

  public void run() {
    final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
    final @NonNls String presentableUrl = "http://download.jetbrains.com";
    int cur = 0;
    for (String url : urls) {
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
          final String filename = url.substring(url.lastIndexOf('/') + 1);
          tempFile = new File(dir, filename);
          if (!FileUtil.createIfDoesntExist(tempFile)) {
            throw new IOException("Can't create file " + filename);
          }
          input = UrlConnectionUtil.getConnectionInputStreamWithException(connection, indicator);
          output = new BufferedOutputStream(new FileOutputStream(tempFile));
          indicator.setText2("Downloading Jars " + ++cur + " of " + urls.size() + " : " + filename);
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
        } finally {
          if (input != null) input.close();
          if (output != null) output.close();
          connection.disconnect();
          if (tempFile.exists()) {
            ok = true;
          }
        }
      } catch (Exception ex) {//
        ok = false;
        e = ex;
      }
    }
  }

  public boolean isCompletedSuccessfully() {
    return ok;
  }

  public String getMessage() {
    return e == null ? "" : e.getMessage();
  }
}
