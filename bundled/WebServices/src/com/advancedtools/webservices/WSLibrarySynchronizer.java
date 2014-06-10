package com.advancedtools.webservices;

import com.advancedtools.webservices.utils.FileUtils;
import com.advancedtools.webservices.utils.JarDownloader;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.utils.facet.BaseWebServicesFacetConfiguration;
import com.advancedtools.webservices.utils.facet.WebServicesClientLibraries;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lib synchronizer copies downloaded libraries to system/webservices folder.
 * So, next time a user wants to create WS project the necessary libraries will be
 * copied from system/webservices/%platform%/%version%/*.jar
 *
 * Also Lib synchronizer solves the problems with outdated classes from javaee.jar.
 * It rearranges libraries in a such way that javaee.jar is always the latest library
 * in the list
 *  
 * @author Konstantin Bulenkov
 */
public class WSLibrarySynchronizer implements ModuleComponent {
  private final Module myModule;
  private static @NonNls final String NAME = WSLibrarySynchronizer.class.getName();
  @NonNls private static final String JAVAEE_JAR = "javaee.jar";

  public WSLibrarySynchronizer(Module module) {
    myModule = module;
  }

  public void projectOpened() {
  }

  public void moduleAdded() {
    if (myModule == null) return;

    final Facet[] facets = FacetManager.getInstance(myModule).getAllFacets();
    for (Facet facet : facets) {
      if (facet.getConfiguration() instanceof BaseWebServicesFacetConfiguration) {
        String platform = ((BaseWebServicesFacetConfiguration)facet.getConfiguration()).getWsEngine().getName();

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            ModifiableRootModel model = ModuleRootManager.getInstance(myModule).getModifiableModel();
            OrderEntry[] orderEntries = model.getOrderEntries();
            OrderEntry[] newOrder = new OrderEntry[orderEntries.length];
            OrderEntry javaee = null;
            for (int i = 0, j = 0; i < orderEntries.length; i++) {
              OrderEntry entry = orderEntries[i];
              String name = entry.getPresentableName();
              if (javaee == null && entry instanceof LibraryOrderEntry && name != null && name.endsWith(JAVAEE_JAR)) {
                javaee = orderEntries[i];
              } else {
                newOrder[j++] = entry;
              }
            }
            if (javaee != null) {
              newOrder[newOrder.length - 1] = javaee;
            }
            model.rearrangeOrderEntries(newOrder);
            model.commit();
          }
        });

        if (!Arrays.asList(WebServicesClientLibraries.SUPPORTED_PLATFORM).contains(platform)) continue;

        String[] missed = getMissedJars(platform);
        for (String jar : missed) {
          try {
            File in = new File(LibUtils.getLibUrlByName(jar, myModule));
            File out = new File(getLibStorage(platform) + File.separator + jar);
            FileUtils.copyFile(in, out);
          }
          catch (Exception e) {//
          }
        }
      }
    }
  }

  public void projectClosed() {
  }

  @NotNull
  public String getComponentName() {
    return NAME;
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  public static String[] getMissedJars(String platform) {
    String[] jars = WebServicesClientLibraries.getJarShortNames(platform);
    if (jars == null) jars = new String[0];
    File dir = getLibStorage(platform);
    if (!dir.exists()) {
      dir.mkdirs();
      return jars;
    }
    List libs = Arrays.asList(dir.list());
    List<String> needed = new ArrayList<String>();
    for (String jar : jars) {
      if (! libs.contains(jar)) needed.add(jar);
    }
    return needed.toArray(new String[needed.size()]);
  }

  private static final FileFilter JAR_FILE_FILTER = new FileFilter() {
    public boolean accept(final File pathname) {
      return pathname.getName().endsWith(".jar");
    }
  };


  public static List<String> getAvailableJarPaths(String platform) {
    List<String> jars = new ArrayList<String>();
    File dir = getLibStorage(platform);
    if (dir.exists()) {
      for (File jar : dir.listFiles(JAR_FILE_FILTER)) {
        jars.add(jar.getAbsolutePath());
      }
    }
    return jars;
  }

  public static File getLibStorage(String platform) {
    final String path = LibUtils.getExtractedResourcesWebServicesDir() + File.separator
                  + WebServicesClientLibraries.PLATFORM_FOLDERS.get(platform) + File.separator
                  + WebServicesClientLibraries.DEFAULT_VERSIONS.get(platform);
    return new File(path);
  }

  public static boolean downloadMissedJars(String platform, Project project) {
    if (WebServicesClientLibraries.isSupported(platform)) {
      final LibraryInfo[] libraries = WebServicesClientLibraries.getNecessaryLibraries(platform);
      final List<String> urls = new ArrayList<String>();
      for (LibraryInfo library : libraries) {
        if (library.getDownloadingInfo().getDownloadUrl().startsWith("http")) {
          urls.add(library.getDownloadingInfo().getDownloadUrl());
        }
      }
      if (urls.size() > 0) {
        JarDownloader process = new JarDownloader(urls, getLibStorage(platform));
        boolean completed = ProgressManager.getInstance().runProcessWithProgressSynchronously(process,
                                                                          WSBundle.message("downloading.missed.jars"),
                                                                          true,
                                                                          project);
        if (completed) {
          if (process.isCompletedSuccessfully()) {
            return true;
          } else {
            Messages.showErrorDialog("Can't download jars + \n" + process.getMessage(), "Error");
          }
        }
      }
    }
    return false;
  }
}
