package com.advancedtools.webservices.jaxb;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.advancedtools.webservices.utils.LibUtils;
import com.advancedtools.webservices.jwsdp.JWSDPWSEngine;
import com.advancedtools.webservices.wsengine.ExternalEngine;
import com.advancedtools.webservices.wsengine.LibraryInfo;
import com.advancedtools.webservices.wsengine.ExternalEngineThatBundlesJEEJars;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author maxim
 */
public class JaxbMappingEngine implements ExternalEngine, ExternalEngineThatBundlesJEEJars {
  private static final String ACTIVATION_LIBRARY_NAME = "Activation";
  private static final String STAX_LIBRARY_NAME = "Stax";
  private static final String JAXB_LIBRARY_NAME = "JAXB2 EA";
  private static final String JAXB2_FINAL_LIBRARY_NAME = "JAXB2.X final";
  public static final String JAXB_2_ENGINE = "JAXB 2";
  public static Collection<? extends String> mappedClassesSet = Arrays.asList("javax.xml.bind.annotation.XmlType");
  @NonNls
  public static final String JAXB_IMPL_JAR = "jaxb-impl.jar";
  @NonNls
  private static final String JAXB_API_JAR = "jaxb-api.jar";
  @NonNls
  private static final String JAXB_XJC_LIBRARY_NAME = "JAXB XJC";

  public String getName() {
    return JAXB_2_ENGINE;
  }

  public LibraryDescriptor[] getLibraryDescriptors(LibraryDescriptorContext context) {
    final String basePath = getBasePath();
    if (basePath == null) return LibraryDescriptor.EMPTY_ARRAY;

    LibraryDescriptor[] glassFishLibs = JWSDPWSEngine.getLibInfosIfGlassFishOrMetroInstall(basePath, context);
    if (glassFishLibs != null) return glassFishLibs;

    if(new File(basePath + File.separator + "jaxb").exists()) { // JWSDP2
      final String commonJaxbPrefix = "jaxb" + File.separator + "lib" + File.separator;
      final String[] jaxbPlainJars = new String[]{
        commonJaxbPrefix + JAXB_IMPL_JAR,
        commonJaxbPrefix + "jaxb1-impl.jar",
        commonJaxbPrefix + JAXB_API_JAR
      };

      if (context.isForRunningGeneratedCode()) {
        final String sjsxpPrefix = File.separator + "sjsxp" + File.separator + "lib" + File.separator;
        final String jswdpPrefix = File.separator + "jwsdp-shared" + File.separator + "lib" + File.separator;

        return new LibraryDescriptor[] {
          new LibraryInfo(JAXB_LIBRARY_NAME, jaxbPlainJars),
          new LibraryInfo(ACTIVATION_LIBRARY_NAME, jswdpPrefix + "activation.jar"),
          new LibraryInfo(STAX_LIBRARY_NAME, new String[] { sjsxpPrefix + "jsr173_api.jar", sjsxpPrefix + "sjsxp.jar" })
        };
      } else {
        return new LibraryDescriptor[] {
          new LibraryInfo(JAXB_LIBRARY_NAME, jaxbPlainJars),
          new LibraryInfo(JAXB_XJC_LIBRARY_NAME, commonJaxbPrefix + "jaxb-xjc.jar")
        };
      }
    } else {
      boolean accessingLibsFromPlugin = LibUtils.accessingLibraryJarsFromPluginBundledLibs(basePath);
      final String commonJaxbPrefix = accessingLibsFromPlugin ? "":"lib" + File.separator;
      return buildJaxbNeededJars(commonJaxbPrefix, context);
    }
  }

  public static LibraryDescriptor[] buildJaxbNeededJars(String commonJaxbPrefix, LibraryDescriptorContext context) {
    final String[] jaxbPlainJars = new String[]{
      commonJaxbPrefix + JAXB_IMPL_JAR,
      commonJaxbPrefix + JAXB_API_JAR
    };

    final LibraryInfo info = new LibraryInfo(STAX_LIBRARY_NAME, new String[]{commonJaxbPrefix + "jsr173_api.jar", commonJaxbPrefix + "sjsxp.jar"});

    if (context.isForRunningGeneratedCode()) {
      return new LibraryDescriptor[] {
        new LibraryInfo(JAXB2_FINAL_LIBRARY_NAME, jaxbPlainJars),
        new LibraryInfo(ACTIVATION_LIBRARY_NAME, commonJaxbPrefix + "activation.jar"),
        info
      };
    } else {
      return new LibraryDescriptor[] {
        new LibraryInfo(JAXB2_FINAL_LIBRARY_NAME, jaxbPlainJars),
        new LibraryInfo(JAXB_XJC_LIBRARY_NAME, commonJaxbPrefix + "jaxb-xjc.jar"),
        info
      };
    }
  }

  public String getBasePath() {
    return WebServicesPluginSettings.getInstance().getJwsdpPath();
  }

  public String[] getJEEJarNames(@NotNull ExternalEngine.LibraryDescriptorContext context) {
    return JWSDPWSEngine.getJaxWsJarsForOverriding(this, context);
  }
}
