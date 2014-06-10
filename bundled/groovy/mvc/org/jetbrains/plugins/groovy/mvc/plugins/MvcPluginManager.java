package org.jetbrains.plugins.groovy.mvc.plugins;

import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.SettingsSavingComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleServiceManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SystemProperties;
import com.intellij.util.xmlb.XmlSerializer;
import gnu.trove.THashMap;
import org.jdom.Document;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.mvc.ConsoleProcessDescriptor;
import org.jetbrains.plugins.groovy.mvc.MvcConsole;
import org.jetbrains.plugins.groovy.mvc.MvcFramework;
import org.jetbrains.plugins.groovy.mvc.MvcModuleStructureSynchronizer;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * User: Dmitry.Krasilschikov
 * Date: 04.09.2008
 */

public class MvcPluginManager implements SettingsSavingComponent {
  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.mvc.MvcPluginManager");

  @NonNls private static final String PLUGIN_INFO_COMMANDS_SEPARATOR =
    "--------------------------------------------------------------------------";
  @NonNls protected final String WELCOME_TO_STRING;
  @NonNls protected static String FOR_FUTHER_INFO_STRING = "For further info visit";
  @NonNls protected static String NAME_STRING = "Name: ";
  @NonNls protected static String RELEASE_STRING = "Release: ";
  @NonNls protected static String LATEST_RELEASE_STRING = "Latest release: ";
  @NonNls protected static String NO_INFO_AVAILBLE_STRING = "<no info available>";
  @NonNls protected static final String AUTHOR_PLUGIN_INFO = "Author: ";
  @NonNls protected static final String AUTHORS_EMAIL_PLUGIN_INFO = "Author's e-mail: ";
  @NonNls protected static final String FIND_MORE_PLUGIN_INFO = "Find more info here: ";
  @NonNls protected static String NO_TITLE_AVAILBLE_STRING = "No info about this plugin available";
  @NonNls protected static String QUESTION_RELEASE_VERSION_STRING = " (?)";
  @NonNls protected static String AVAILABLE_FULL_RELEASES_STRING = "Available full releases: ";
  @NonNls protected static String AVAILABLE_ZIP_RELEASE_STRING = "Available zip releases:  ";

  @NonNls private static final String NO_FULL_RELEASES_AVAILABLE_FOR_THIS_PLUGIN_NOW = "<no full releases available for this plugin now>";
  @NonNls public static final String PLUGIN_XML = "plugin.xml";
  @NonNls public static final String READING_REMOTE_PLUGIN_LIST = "\nReading remote plugin list ...";
  @NonNls private static final String PLUGIN_LIST_OUT_OF_DATE_RETRIEVING = "\nPlugin list out-of-date, retrieving..";
  @NonNls private static final String PLUGIN_LIST_FILE_CORRUPT_RETRIEVING_AGAIN = "\nPlugin list file corrupt, retrieving again..";
  @NonNls private static final String PLUGINS_LIST_CACHE_DOESN_T_EXIST_CREATING = "\nPlugins list cache doesn't exist creating..";
  @NonNls private static final String PLUGINS_LIST_CACHE_IS_CORRUPT_RE_CREATING_REGEXP = "\nPlugins list cache is corrupt .* Re-creating..";
  private static final String PLUGINS_COMMANDS_SEPARATOR = "-------------------------------------------------------------";
  protected CachedMvcPlugins myPluginsCached = new CachedMvcPlugins();
  protected final Module myModule;
  private final MvcFramework myFramework;
  @NonNls private static final String PLUGIN_INFO_COMMAND = "plugin-info";

  public MvcPluginManager(Module module) {
    myModule = module;
    myFramework = MvcModuleStructureSynchronizer.getFramework(module);
    assert myFramework != null;
    final File file = getCacheFile();
    if (file.exists()) {
      try {
        myPluginsCached = XmlSerializer.deserialize(JDOMUtil.loadDocument(file), CachedMvcPlugins.class);
      }
      catch (Exception ignored) {
      }
    }
    WELCOME_TO_STRING = "Welcome to " + myFramework.getFrameworkName();
  }

  public MvcFramework getFramework() {
    return myFramework;
  }

  public void reloadAvailablePlugins() {
    reloadAvailablePlugins(myFramework);
  }

  public Map<String, MvcPluginDescriptor> loadAllPluginsInfo(final Set<String> pluginsNames) {
    setFullPluginsInfo(new HashMap<String, MvcPluginDescriptor>());

    if (pluginsNames.isEmpty()) {
      return Collections.emptyMap();
    }

    parseAllPluginsInfo(pluginsNames);

    return getFullPluginsInfo();
  }

  @Nullable
  public MvcPluginDescriptor getFullPluginInfo(String pluginName) {
    final Map<String, MvcPluginDescriptor> map = getFullPluginsInfo();
    if (map == null) return null;
    return map.get(pluginName);
  }

  @Nullable
  public Map<String, MvcPluginDescriptor> getFullPluginsInfo() {
    return myPluginsCached.fullPluginsInfo;
  }

  public Map<String, MvcPluginDescriptor> getAvailablePlugins() {
    return myPluginsCached.availablePluginsMap;
  }

  @Nullable
  protected static MvcPluginDescriptor parsePluginInfo(String buffer, final String pluginName) {

    try {
      if (buffer.contains("Plugin with name '" + pluginName + "' was not found in the configured repositories")) {
        return null;
      }

      buffer = buffer.replaceAll(READING_REMOTE_PLUGIN_LIST, "");
      buffer = buffer.replaceAll(PLUGIN_LIST_OUT_OF_DATE_RETRIEVING, "");
      buffer = buffer.replaceAll(PLUGIN_LIST_FILE_CORRUPT_RETRIEVING_AGAIN, "");
      buffer = buffer.replaceAll(PLUGINS_LIST_CACHE_DOESN_T_EXIST_CREATING, "");
      buffer = buffer.replaceAll(PLUGINS_LIST_CACHE_IS_CORRUPT_RE_CREATING_REGEXP, "");

      if (buffer.contains("Error reading remote plugin list"))return null;      

      //--------------------------------------------------------------------------
      int informationIndex = buffer.indexOf(PLUGIN_INFO_COMMANDS_SEPARATOR);
      if (informationIndex == -1) {
        return null;
      }

      buffer = buffer.substring(informationIndex);

      //"Information about plugin"

      MvcPluginDescriptor plugin = new MvcPluginDescriptor();

      final int startAuthorIndex = buffer.indexOf(NAME_STRING) + NAME_STRING.length();
      final int endAuthorIndex = buffer.indexOf("\t|");
      final String name = buffer.substring(startAuthorIndex, endAuthorIndex);
      assert name.equals(pluginName);
      plugin.setName(name);

      final String lineSeparator = "\n";

      String releaseVersion = null;
      String strictReleaseVersion = null;
      //int afterReleasesIndex = -1;

      final int releaseIndex = buffer.indexOf(RELEASE_STRING);
      if (releaseIndex != -1) {
        final String afterRelease = buffer.substring(releaseIndex + RELEASE_STRING.length());
        final int endRelease = afterRelease.indexOf(lineSeparator);
        //afterReleasesIndex = endRelease + lineSeparator.length();
        releaseVersion = afterRelease.substring(0, endRelease);
      }

      final int latestReleaseIndex = buffer.indexOf(LATEST_RELEASE_STRING);
      if (latestReleaseIndex != -1) {
        final int noInfoAvailableIndex = buffer.indexOf(NO_INFO_AVAILBLE_STRING);

        if (noInfoAvailableIndex != -1) {
          //afterReleasesIndex = noInfoAvailableIndex + NO_INFO_AVAILBLE_STRING.length();
        }
        else {

          final String afterLatestRelease = buffer.substring(latestReleaseIndex + LATEST_RELEASE_STRING.length());
          final int endLatestRelease = afterLatestRelease.indexOf(lineSeparator);
          releaseVersion = afterLatestRelease.substring(0, endLatestRelease);

          if (releaseVersion.endsWith(QUESTION_RELEASE_VERSION_STRING)) {
            strictReleaseVersion = releaseVersion.substring(0, releaseVersion.length() - QUESTION_RELEASE_VERSION_STRING.length());
          }
          else {
            strictReleaseVersion = releaseVersion;
          }

          //afterReleasesIndex = endLatestRelease + lineSeparator.length();
        }
      }

      if (strictReleaseVersion == null) {
        strictReleaseVersion = releaseVersion;
      }

      if (releaseVersion != null) {
        plugin.setRelease(releaseVersion);
      }

      if (strictReleaseVersion != null) {
        final int releaseNotFoundIndex = buffer.indexOf("<release " + strictReleaseVersion + " not found for this plugin>");

        //release not found
        if (releaseNotFoundIndex == -1) {
          //final String afterVersionBuffer = buffer.substring(afterReleasesIndex);
          final String[] strings = buffer.split(lineSeparator);
          int indexOfNameRelease = 1;


          final int noTitleAvailable = buffer.indexOf(NO_TITLE_AVAILBLE_STRING);
          int descriptionIndex = noTitleAvailable + NO_TITLE_AVAILBLE_STRING.length();

          //Title
          if (noTitleAvailable == -1) {
            final String nextString = strings[indexOfNameRelease + 1];
            assert "--------------------------------------------------------------------------".equals(nextString) : nextString;
            String title = strings[indexOfNameRelease + 2];
            plugin.setTitle(title);
            descriptionIndex = buffer.indexOf(title) + title.length();
          }

          final int afterTitleIndex = descriptionIndex + lineSeparator.length();
          if (!buffer.substring(afterTitleIndex).startsWith(PLUGIN_INFO_COMMANDS_SEPARATOR)) {
            return null;
          }

          descriptionIndex = afterTitleIndex + PLUGIN_INFO_COMMANDS_SEPARATOR.length() + lineSeparator.length();

          final String griffonDescription = parseDescription(buffer, lineSeparator, descriptionIndex);

          //Author
          final int authorIndex = buffer.indexOf(AUTHOR_PLUGIN_INFO);
          if (authorIndex != -1) {
            final int afterAuthorIndex = buffer.indexOf(lineSeparator, authorIndex);
            final String author = buffer.substring(authorIndex + AUTHOR_PLUGIN_INFO.length(), afterAuthorIndex);
            plugin.setAuthor(author);
            descriptionIndex = afterAuthorIndex + lineSeparator.length() + PLUGIN_INFO_COMMANDS_SEPARATOR.length() + lineSeparator.length();
          }
          //assert "--------------------------------------------------------------------------".equals(strings[indexOfNameRelease + 3]);
          //plugin.setAuthor(extactContentAfter(strings[indexOfNameRelease + 4], "Author: "));

          //Author email
          final int emailIndex = buffer.indexOf(AUTHORS_EMAIL_PLUGIN_INFO);
          if (emailIndex != -1) {
            final int afterEmailindex = buffer.indexOf(lineSeparator, emailIndex);
            final String description = buffer.substring(emailIndex + AUTHORS_EMAIL_PLUGIN_INFO.length(), afterEmailindex);
            plugin.setEmail(description);
            descriptionIndex = afterEmailindex + lineSeparator.length() + PLUGIN_INFO_COMMANDS_SEPARATOR.length() + lineSeparator.length();
          }
          //assert "--------------------------------------------------------------------------".equals(strings[indexOfNameRelease + 5]);
          //plugin.setEmail(extactContentAfter(strings[indexOfNameRelease + 6], "Author's e-mail: "));

          //Documentation
          final int urlIndex = buffer.indexOf(FIND_MORE_PLUGIN_INFO);
          if (urlIndex != -1) {
            final int afterUrlIndex = buffer.indexOf(lineSeparator, urlIndex);
            final String url = buffer.substring(urlIndex + FIND_MORE_PLUGIN_INFO.length(), afterUrlIndex);
            plugin.setUrl(url);
            descriptionIndex = afterUrlIndex + lineSeparator.length() + PLUGIN_INFO_COMMANDS_SEPARATOR.length() + lineSeparator.length();
          }
          //assert "--------------------------------------------------------------------------".equals(strings[indexOfNameRelease + 7]);
          //plugin.setUrl(extactContentAfter(strings[indexOfNameRelease + 8], "Find more info here: "));

          //Description

          //assert "--------------------------------------------------------------------------".equals(strings[indexOfNameRelease + 9]);


          final String description = parseDescription(buffer, lineSeparator, descriptionIndex);
          if ((StringUtil.isEmpty(description) || description.startsWith(AVAILABLE_FULL_RELEASES_STRING)) && !griffonDescription.startsWith(AUTHOR_PLUGIN_INFO)) {
            plugin.setDescription(griffonDescription);
          } else {
            plugin.setDescription(description);
          }
        }
      }

      final int noFullReleaseIndex = buffer.indexOf(AVAILABLE_FULL_RELEASES_STRING);
      assert noFullReleaseIndex != -1;

      final String afterMainInfoBuffer = buffer.substring(noFullReleaseIndex);
      final String[] afterMainInfoStrings = afterMainInfoBuffer.split(lineSeparator);

      final String versionsString = afterMainInfoStrings[0];
      assert versionsString.startsWith(AVAILABLE_FULL_RELEASES_STRING);

      final String versionsAsString = versionsString.substring(AVAILABLE_FULL_RELEASES_STRING.length());

      if (!NO_FULL_RELEASES_AVAILABLE_FOR_THIS_PLUGIN_NOW.equals(versionsAsString)) {
        LinkedHashSet<String> versSet = new LinkedHashSet<String>(Arrays.asList(versionsAsString.split(" ")));
        versSet.remove("");
        plugin.setVersions(versSet);
      }

      final String zipString = afterMainInfoStrings[1];

      if (zipString.startsWith(AVAILABLE_ZIP_RELEASE_STRING)) {
        final String zipRelease = zipString.substring(AVAILABLE_ZIP_RELEASE_STRING.length());
        plugin.setZipRelease(zipRelease);
      }

      return plugin;
    }
    catch (Exception e) {
      return null;
    }
  }

  private static String parseDescription(String buffer, String lineSeparator, int descriptionIndex) {
    final int endDescription = buffer.indexOf(lineSeparator, descriptionIndex);
    final String description = buffer.substring(descriptionIndex, endDescription);
    return description;
  }

  public ConsoleProcessDescriptor parseSinglePluginInfo(final String pluginName) {
    ProcessBuilder pb = myFramework.createCommand(myModule, false, PLUGIN_INFO_COMMAND, pluginName);
    final ConsoleProcessDescriptor descriptor = MvcConsole.getInstance(myModule.getProject()).executeProcess(myModule, pb, null, false);
    descriptor.addProcessListener(new ProcessAdapter() {
      final StringBuilder builder = new StringBuilder();

      @Override
      public void processTerminated(final ProcessEvent event) {
        MvcPluginDescriptor descriptor = parsePluginInfo(builder.toString(), pluginName);
        if (descriptor != null) {
          putFullPluginsInfo(descriptor.getName(), descriptor);
        }
      }

      @Override
      public void onTextAvailable(final ProcessEvent event, final Key outputType) {
        builder.append(StringUtil.convertLineSeparators(event.getText()));
      }
    });

    return descriptor;
  }

  protected void setFullPluginsInfo(@NotNull Map<String, MvcPluginDescriptor> plugins) {
    myPluginsCached.fullPluginsInfo = plugins;
  }

  @Nullable
  public MvcPluginDescriptor extractPluginInfo(final String path) {
    if (!path.startsWith(myFramework.getFrameworkName().toLowerCase() + "-")) return null;
    if (!path.endsWith(".zip")) return null;

    final VirtualFile pluginZipVirtualFile = JarFileSystem.getInstance().findFileByPath(path);
    if (pluginZipVirtualFile == null) return null;

    final JarFileSystem pluginZipFileSystem = JarFileSystem.getInstance();
    try {
      final ZipFile zipPluginFile = pluginZipFileSystem.getJarFile(pluginZipVirtualFile);
      if (zipPluginFile == null) return null;

      final ZipEntry pluginXmlEntry = zipPluginFile.getEntry(MvcPluginManager.PLUGIN_XML);
      if (pluginXmlEntry == null) return null;

      final InputStream inputStream = zipPluginFile.getInputStream(pluginXmlEntry);

      try {
        return MvcPluginXmlParser.parse(inputStream);
      }
      finally {
        inputStream.close();
      }
      //return null;
    }
    catch (IOException e1) {
      LOG.error("File on path" + path + "hasn't access or wasn't found", e1);
      return null;
    }
  }


  protected void reloadAvailablePlugins(MvcFramework framework) {
    setFullPluginsInfo(new THashMap<String, MvcPluginDescriptor>());
    myPluginsCached.availablePluginsMap = new HashMap<String, MvcPluginDescriptor>();

    ProcessBuilder pb = framework.createCommand(myModule, false, MvcPluginUtil.LIST_PLUGINS_COMMAND);
    final ConsoleProcessDescriptor processDescriptor =
          MvcConsole.getInstance(myModule.getProject()).executeProcess(myModule, pb, null, false);

    ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable() {
      public void run() {
        final ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        progressIndicator.setText("Downloading plugin list...");

        final StringBuilder builder = new StringBuilder();

        processDescriptor.addProcessListener(new ProcessAdapter() {
          @Override
          public void onTextAvailable(final ProcessEvent event, final Key outputType) {
            progressIndicator.setText2(event.getText());
            builder.append(event.getText());
          }
        }).waitWith(progressIndicator, new Runnable() {
          public void run() {
            final Map<String, MvcPluginDescriptor> plugins = parseListPluginsOutput(builder, progressIndicator);
            for (final String name : plugins.keySet()) {
              myPluginsCached.availablePluginsMap.put(name, plugins.get(name));
            }
          }
        });
      }
    }, "Updating plugin list...", true, myModule.getProject());
  }

  private static Map<String, MvcPluginDescriptor> parseListPluginsOutput(final StringBuilder builder, final ProgressIndicator progressIndicator) {
    int separatorIndex = builder.indexOf(PLUGINS_COMMANDS_SEPARATOR);
    if (separatorIndex == -1) {
      return Collections.emptyMap();
    }

    Map<String, MvcPluginDescriptor> result = new THashMap<String, MvcPluginDescriptor>();

    String buffer = builder.toString();
    String bufferAfterSeparator = buffer.substring(separatorIndex + PLUGINS_COMMANDS_SEPARATOR.length());
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new StringReader(bufferAfterSeparator));


      String line = reader.readLine();
      while (line != null) {
        MvcPluginDescriptor plugin = MvcPluginUtil.parsePlugins(line);

        if (plugin == null) {
          line = reader.readLine();
          continue;
        }

        progressIndicator.setText2(plugin.getName());
        result.put(plugin.getName(), plugin);
        line = reader.readLine();
      }
      return result;
    }
    catch (IOException e) {
      LOG.error("Cannot read plugins information from output", e);
      return Collections.emptyMap();
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException e) {
          LOG.error(e);
        }
      }
    }
  }

  public void putFullPluginsInfo(final String pluginName, final MvcPluginDescriptor plugin) {
    myPluginsCached.fullPluginsInfo.put(pluginName, plugin);
  }

  protected File getCacheFile() {
    return new File(PathManager.getSystemPath() + File.separator + myFramework.getFrameworkName().toLowerCase() +
                    "Plugins" + File.separator + myModule.getName() + ".xml");
  }

  public void save() {
    try {
      final File file = getCacheFile();
      FileUtil.createIfDoesntExist(file);
      JDOMUtil.writeDocument(new Document(XmlSerializer.serialize(myPluginsCached)), file, SystemProperties.getLineSeparator());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void parseAllPluginsInfo(final Set<String> pluginsNames) {
    final StringBuilder builder = new StringBuilder();

    final Task.Modal modalTask = new Task.Modal(myModule.getProject(), "Downloading plugin list...", true) {

      public void run(@NotNull final ProgressIndicator progressIndicator) {
        progressIndicator.setText("Download plugin information from server...");
        int i = 0;
        for (String pluginsName : pluginsNames) {
          getPluginInfo(progressIndicator, pluginsName);
          progressIndicator.setFraction((1.0 * (i + 1) / pluginsNames.size()));
          i++;
          if (progressIndicator.isCanceled()) {
            break;
          }
        }
      }

      private void getPluginInfo(final ProgressIndicator progressIndicator, String pluginName) {

        ProcessBuilder pb = myFramework.createCommand(myModule, false, PLUGIN_INFO_COMMAND, pluginName);

        try {
          final Ref<Integer> pluginNum = new Ref<Integer>(0);
          Process process = pb.start();
          OSProcessHandler handler = new OSProcessHandler(process, "");
          MvcConsole.getInstance(myProject).getConsole().attachToProcess(handler);

          ProcessTerminatedListener.attach(handler);

          handler.addProcessListener(new ProcessAdapter() {
            @Override
            public void startNotified(final ProcessEvent event) {
            }

            @Override
            public void processTerminated(final ProcessEvent event) {
              //parsePluginsInfo(builder, pluginsNames);
            }

            @Override
            public void processWillTerminate(final ProcessEvent event, final boolean willBeDestroyed) {
            }

            @Override
            public void onTextAvailable(final ProcessEvent event, final Key outputType) {

              final String text = StringUtil.convertLineSeparators(event.getText());

              if (text.contains(WELCOME_TO_STRING)) {
                progressIndicator.startNonCancelableSection();
              }

              if (text.contains(FOR_FUTHER_INFO_STRING)) {
                progressIndicator.finishNonCancelableSection();
              }

              if (text.contains(NAME_STRING)) {
                final int startAuthorIndex = text.indexOf(NAME_STRING) + NAME_STRING.length();
                final int endAuthorIndex = text.indexOf("\t|");
                if (startAuthorIndex != -1 && endAuthorIndex != -1 && startAuthorIndex < endAuthorIndex) {
                  final String name = text.substring(startAuthorIndex, endAuthorIndex);
                  progressIndicator.setText2(name);
                  pluginNum.set(pluginNum.get().intValue() + 1);
                }
              }
              builder.append(text);
            }
          });


          handler.startNotify();
          while (!handler.waitFor(500)) {
            if (progressIndicator.isCanceled()) {
              handler.destroyProcess();
              break;
            }
          }

        }
        catch (IOException e) {
          LOG.error("Process to get list og plugins cannot start", e);
        }
      }
    };

    ProgressManager.getInstance().run(modalTask);

    parsePluginsInfo(builder, pluginsNames);
  }

  private void parsePluginsInfo(StringBuilder builder, Set<String> pluginsNames) {
    final String[] pluginBuffers = builder.toString().split(WELCOME_TO_STRING);
    final String[] namesArray = pluginsNames.toArray(new String[pluginsNames.size()]);

    assert pluginBuffers.length - 1 <= pluginsNames.size();

    String pluginBuffer;
    String pluginName;
    MvcPluginDescriptor mvcPluginDescriptor;
    for (int i1 = 0; i1 < pluginBuffers.length - 1; i1++) {

      pluginBuffer = pluginBuffers[i1 + 1];
      pluginName = namesArray[i1];
      mvcPluginDescriptor = parsePluginInfo(pluginBuffer, pluginName);

      if (mvcPluginDescriptor == null) {
        LOG.warn("can't perform operation: get info for plugin '" + pluginName + "'");
        break;
      }

      putFullPluginsInfo(pluginName, mvcPluginDescriptor);
    }
  }

  public static MvcPluginManager getInstance(Module module) {
    return ModuleServiceManager.getService(module, MvcPluginManager.class);
  }
}
