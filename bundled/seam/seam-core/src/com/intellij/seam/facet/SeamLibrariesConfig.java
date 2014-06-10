package com.intellij.seam.facet;

import com.intellij.facet.ui.libraries.LibraryInfo;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.hash.HashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * User: Sergey.Vasiliev
 */
@SuppressWarnings({"unchecked"})
public class SeamLibrariesConfig {
  @NonNls private static final String DOWNLOAD_JETBRAINS_COM = "http://download.jetbrains.com";
  @NonNls private static final String DOWNLOADING_URL = DOWNLOAD_JETBRAINS_COM + "/idea/j2ee_libs/seam/";

  final static private Pair<String, String> SEAM_JAR = new Pair<String, String>("jboss-seam.jar", "org.jboss.seam.annotations.Name");
  final static private Pair<String, String> SEAM_DEBUG_JAR =
    new Pair<String, String>("jboss-seam-debug.jar", "org.jboss.seam.debug.Contexts");
  final static private Pair<String, String> SEAM_IOC_JAR =
    new Pair<String, String>("jboss-seam-ioc.jar", "org.jboss.seam.ioc.IoCComponent");
  final static private Pair<String, String> SEAM_MAIL_JAR =
    new Pair<String, String>("jboss-seam-mail.jar", "org.jboss.seam.mail.ui.MailComponent");
  final static private Pair<String, String> SEAM_PDF_JAR =
    new Pair<String, String>("jboss-seam-pdf.jar", "org.jboss.seam.pdf.DocumentStore");
  final static private Pair<String, String> SEAM_REMOTING_JAR =
    new Pair<String, String>("jboss-seam-remoting.jar", "org.jboss.seam.remoting.Remoting");
  final static private Pair<String, String> SEAM_UI_JAR =
    new Pair<String, String>("jboss-seam-ui.jar", "org.jboss.seam.ui.facelet.FaceletsRenderer");

  private static final Map<SeamVersion, Pair<String, String>[]> versionLibs = new HashMap<SeamVersion, Pair<String, String>[]>();

  static {
    versionLibs.put(SeamVersion.SEAM_1_2_1,
                    new Pair[]{SEAM_JAR, SEAM_DEBUG_JAR, SEAM_IOC_JAR, SEAM_MAIL_JAR, SEAM_PDF_JAR, SEAM_REMOTING_JAR, SEAM_UI_JAR});

    versionLibs.put(SeamVersion.SEAM_2_0_0,
                    new Pair[]{SEAM_JAR, SEAM_DEBUG_JAR, SEAM_IOC_JAR, SEAM_MAIL_JAR, SEAM_PDF_JAR, SEAM_REMOTING_JAR, SEAM_UI_JAR});

    versionLibs.put(SeamVersion.SEAM_2_1_1,
                    new Pair[]{SEAM_JAR, SEAM_DEBUG_JAR, SEAM_IOC_JAR, SEAM_MAIL_JAR, SEAM_REMOTING_JAR, SEAM_UI_JAR});
  }

  private SeamLibrariesConfig() {

  }

  public static LibraryInfo[] getLibraries(@NotNull final SeamVersion version) {
    final String versionName = version.getName();

    Pair<String, String>[] pairs = versionLibs.get(version);

    if (pairs == null) return new LibraryInfo[0];

    return ContainerUtil.map2Array(pairs, LibraryInfo.class, new Function<Pair<String, String>, LibraryInfo>() {
      public LibraryInfo fun(Pair<String, String> pair) {
        return createLibraryInfo(pair.first, versionName, pair.second);
      }
    });
  }

  private static LibraryInfo createLibraryInfo(final @NonNls String expectedJarName,
                                               final @NonNls String version,
                                               final @NonNls String requiredClasses) {
    return new LibraryInfo(expectedJarName, version, DOWNLOADING_URL + version + "/" + expectedJarName, DOWNLOAD_JETBRAINS_COM,
                           requiredClasses);
  }
}
