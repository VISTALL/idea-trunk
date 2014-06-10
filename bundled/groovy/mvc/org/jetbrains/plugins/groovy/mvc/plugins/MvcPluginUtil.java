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

package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author peter
 */
public class MvcPluginUtil {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.mvc.plugins.MvcPluginUtil");
  private static final String SET_PROXY_COMMAND = "set-proxy";
  private static final String ENTER_HTTP_PROXY_HOST = "Enter HTTP proxy host";
  private static final String ENTER_HTTP_PROXY_PORT = "Enter HTTP proxy port";
  private static final String ENTER_HTTP_PROXY_USERNAME = "Enter HTTP proxy username";
  private static final String ENTER_HTTP_PROXY_PASSWORD = "Enter HTTP proxy password";
  public static final Color COLOR_REMOVE_PLUGIN = FileStatus.DELETED_FROM_FS.getColor();
  public static final Color COLOR_INSTALL_PLUGIN = FileStatus.ADDED.getColor();
  @NonNls public static final String LIST_PLUGINS_COMMAND = "list-plugins";

  private MvcPluginUtil() {
  }

  public static Map<String, MvcPluginDescriptor> getInstalledPlugins(final Module module, MvcFramework framework) {
    Map<String, MvcPluginDescriptor> result = new HashMap<String, MvcPluginDescriptor>();

    for (VirtualFile pluginDir : framework.getPluginRoots(module)) {
      VirtualFile pluginXML = pluginDir.findChild(MvcPluginManager.PLUGIN_XML);
      if (pluginXML != null) {
        MvcPluginDescriptor mvcPluginDescriptor = MvcPluginXmlParser.parse(pluginXML);
        if (mvcPluginDescriptor != null) {
          result.put(mvcPluginDescriptor.getName(), mvcPluginDescriptor);
        }
      }
    }

    return result;
  }

  public static void setFrameworkProxy(final boolean useProxy, final String host, final int port, final String username, final String password,
                                       final Module module,
                                       final MvcFramework framework) {
    ProcessBuilder pb = framework.createCommand(module, false, SET_PROXY_COMMAND);

    final OutputStreamWriter writer;
    try {
      Process process = pb.start();
      final OSProcessHandler handler = new OSProcessHandler(process, "");
      MvcConsole.getInstance(module.getProject()).getConsole().attachToProcess(handler);

      writer = new OutputStreamWriter(process.getOutputStream());
      writer.write(useProxy ? "y\n" : "n\n");
      writer.flush();

      handler.addProcessListener(new ProcessAdapter() {

        @Override
        public void startNotified(final ProcessEvent event) {
        }

        @Override
        public void processTerminated(final ProcessEvent event) {
        }

        @Override
        public void processWillTerminate(final ProcessEvent event, final boolean willBeDestroyed) {
        }

        @Override
        public void onTextAvailable(final ProcessEvent event, final Key outputType) {
          String text = event.getText();

          try {
            if (text.contains(ENTER_HTTP_PROXY_HOST)) {
              writer.write(host + "\n");
            }
            else if (text.contains(ENTER_HTTP_PROXY_PORT)) {
              writer.write(port + "\n");
            }
            else if (text.contains(ENTER_HTTP_PROXY_USERNAME)) {
              writer.write(username + "\n");
            }
            else if (text.contains(ENTER_HTTP_PROXY_PASSWORD)) {
              writer.write(password + "\n");
            }
            writer.flush();
          }
          catch (IOException e) {
            e.printStackTrace();
          }
        }
      });

      handler.startNotify();

    }
    catch (IOException e) {
      LOG.error("Process to set proxy cannot start", e);
    }
  }

  public static String cleanPath(final String path) {
    String pluginFilePath = path;
    if (path.endsWith("!/")) {
      pluginFilePath = path.substring(0, path.length() - "!/".length());
    }
    else if (path.endsWith("!")) {
      pluginFilePath = path.substring(0, path.length() - "!".length());
    }
    return pluginFilePath;
  }

  @Nullable
  public static MvcPluginDescriptor parsePlugins(final String line) {
    StringTokenizer tokenizer = new StringTokenizer(line, " \r\t\n<>", true);

    MvcPluginDescriptor plugin;
    String name;
    String version;
    String minusMinus;
    String description = "";

    plugin = new MvcPluginDescriptor();

    if (!tokenizer.hasMoreTokens()) {
      return null;
    }

    name = skipWhiteSpacesIfAny(tokenizer);

    plugin.setName(name);
    String lastToken = skipWhiteSpacesIfAny(tokenizer);

    if (!"<".equals(lastToken)) {
      return null;
    }

    if (tokenizer.hasMoreTokens()) {
      version = parseVersionWithBrackets(tokenizer);
      if (version == null) {
        return null;
      }

      plugin.setRelease(version);
    }

    minusMinus = skipWhiteSpacesIfAny(tokenizer);

    if ("--".equals(minusMinus)) {
      description += skipWhiteSpacesIfAny(tokenizer);

      while (tokenizer.hasMoreElements()) {
        description += tokenizer.nextToken();
      }
      plugin.setTitle(description);
    }

    return plugin;
  }

  @Nullable
  public static String skipWhiteSpacesIfAny(final StringTokenizer tokenizer) {
    String token = null;
    while (tokenizer.hasMoreTokens()) {
      token = tokenizer.nextToken();
      if (!(" ".equals(token) || "\r".equals(token)) || "\n".equals(token) || "\t".equals(token)) {
        break;
      }
    }
    return token;
  }

  @Nullable
  public static String parseVersionWithBrackets(final StringTokenizer tokenizer) {
    if (!tokenizer.hasMoreTokens()) {
      return null;
    }

    String token = tokenizer.nextToken();

    if (token.endsWith(">")) {
      return null;
    }

    String version = "";
    while (tokenizer.hasMoreTokens() && !token.endsWith(">")) {
      version += token;
      token = tokenizer.nextToken();
    }

    return version;
  }
}
