package com.intellij.spring.model.actions.patterns.frameworks.util;

import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.HashMap;
import com.intellij.util.xmlb.XmlSerializer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LibrariesConfigurationManager implements Disposable {
  private final Map<String, List<LibraryInfo>> myLibraries = new HashMap<String, List<LibraryInfo>>();
  private static final String REQUIRED_CLASSES_DELIMITER = ",";

  @NotNull
  public static LibrariesConfigurationManager getInstance(@NotNull Project project) {
    synchronized (project) {
      return ServiceManager.getService(project, LibrariesConfigurationManager.class);
    }
  }

  @NonNls private static final String DOC_LINKS_RESOURCE_XML = "/resources/frameworks/libraries.xml";

  public LibrariesConfigurationManager() {
    final LibrariesConfigurationInfo libs =
      XmlSerializer.deserialize(LibrariesConfigurationInfo.class.getResource(DOC_LINKS_RESOURCE_XML), LibrariesConfigurationInfo.class);

    assert libs != null;
    assert libs.getLibraryConfigurationInfos() != null;

    for (LibraryConfigurationInfo libInfo : libs.getLibraryConfigurationInfos()) {
      final String frameworkId = libInfo.getFrameworkId();
      final LibraryInfo info = new LibraryInfo(libInfo.getJarName(), libInfo.getVersion(), libInfo.getDownloadUrl(),
                                                       libInfo.getPresentationdUrl(), getRequredClasses(libInfo.getRequiredClasses()));

      if( myLibraries.get(frameworkId) == null) myLibraries.put(frameworkId, new ArrayList<LibraryInfo>());

      myLibraries.get(frameworkId).add(info);
    }
  }

  private static String[] getRequredClasses(final String requiredClasses) {
    final List<String> strings = StringUtil.split(requiredClasses, REQUIRED_CLASSES_DELIMITER);
    return ArrayUtil.toStringArray(strings);
  }

  @Nullable
  public LibraryInfo[] getLibraryInfos(final String frameworkId) {
    final List<LibraryInfo> libraryInfos = myLibraries.get(frameworkId);

    return libraryInfos == null ? LibraryInfo.EMPTY_ARRAY : libraryInfos.toArray(new LibraryInfo[libraryInfos.size()] );
  }

   public void dispose() {

  }
}
