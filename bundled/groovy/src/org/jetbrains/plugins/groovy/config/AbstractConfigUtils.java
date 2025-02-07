package org.jetbrains.plugins.groovy.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.groovy.util.GroovyUtils;
import org.jetbrains.plugins.groovy.util.LibrariesUtil;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ilyas
 */
public abstract class AbstractConfigUtils {

  // SDK-dependent entities
  @NonNls protected String STARTER_SCRIPT_FILE_NAME;

  private final Condition<Library> LIB_SEARCH_CONDITION = new Condition<Library>() {
    public boolean value(Library library) {
      return isSDKLibrary(library);
    }
  };

  // Common entities
  @NonNls public static final String UNDEFINED_VERSION = "undefined";
  @NonNls public static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";


  /**
   * Define, whether  given home is appropriate SDK home
   *
   * @param file
   * @return
   */
  public abstract boolean isSDKHome(final VirtualFile file);

  @NotNull
  public abstract String getSDKVersion(@NotNull String path);

  /**
   * Return value of Implementation-Version attribute in jar manifest
   * <p/>
   *
   * @param jarPath      directory containing jar file
   * @param jarRegex     filename pattern for jar file
   * @param manifestPath path to manifest file in jar file
   * @return value of Implementation-Version attribute, null if not found
   */
  @Nullable
  public static String getSDKJarVersion(String jarPath, final String jarRegex, String manifestPath) {
    try {
      File[] jars = GroovyUtils.getFilesInDirectoryByPattern(jarPath, jarRegex);
      if (jars.length != 1) {
        return null;
      }
      JarFile jarFile = new JarFile(jars[0]);
      try {
        JarEntry jarEntry = jarFile.getJarEntry(manifestPath);
        if (jarEntry == null) {
          return null;
        }
        final InputStream inputStream = jarFile.getInputStream(jarEntry);
        Manifest manifest;
        try {
          manifest = new Manifest(inputStream);
        }
        finally {
          inputStream.close();
        }
        final String version = manifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
        if (version != null) {
          return version;
        }

        final Matcher matcher = Pattern.compile(jarRegex).matcher(jars[0].getName());
        if (matcher.matches() && matcher.groupCount() == 1) {
          return matcher.group(1);
        }
        return null;
      }
      finally {
        jarFile.close();
      }
    }
    catch (Exception e) {
      return null;
    }
  }

  public Library[] getProjectSDKLibraries(Project project) {
    if (project == null || project.isDisposed()) return new Library[0];
    final LibraryTable table = ProjectLibraryTable.getInstance(project);
    final List<Library> all = ContainerUtil.findAll(table.getLibraries(), LIB_SEARCH_CONDITION);
    return all.toArray(new Library[all.size()]);
  }

  public Library[] getAllSDKLibraries(@Nullable Project project) {
    return ArrayUtil.mergeArrays(getGlobalSDKLibraries(), getProjectSDKLibraries(project), Library.class);
  }

  public Library[] getGlobalSDKLibraries() {
    return LibrariesUtil.getGlobalLibraries(LIB_SEARCH_CONDITION);
  }

  public abstract boolean isSDKLibrary(Library library);

  @NotNull
  public String getSDKLibVersion(Library library) {
    return getSDKVersion(LibrariesUtil.getGroovyLibraryHome(library));
  }

  public Library[] getSDKLibrariesByModule(final Module module) {
    final Condition<Library> condition = new Condition<Library>() {
      public boolean value(Library library) {
        return isSDKLibrary(library);
      }
    };
    return LibrariesUtil.getLibrariesByCondition(module, condition);
  }

  public Collection<String> getSDKVersions(final Project project) {
    return ContainerUtil.map2List(getAllSDKLibraries(project), new Function<Library, String>() {
      public String fun(Library library) {
        return getSDKLibVersion(library);
      }
    });
  }

  @NotNull
  public abstract String getSDKInstallPath(Module module);

}
