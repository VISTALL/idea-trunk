package wstests;

import com.advancedtools.webservices.WebServicesPluginSettings;
import com.intellij.testFramework.builders.JavaModuleFixtureBuilder;
import com.intellij.util.containers.HashSet;
import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.Set;

/**
 * @by maxim
 */
abstract class BaseWSInspectionsTestCase extends BaseWSTestCase {
  protected final @NonNls Set<String> myTestsWithoutJavaee = new HashSet<String>();

  protected void configureLibs(JavaModuleFixtureBuilder moduleFixtureBuilder) {
    if (!myTestsWithoutJavaee.contains(getTestName())) {
      moduleFixtureBuilder.addLibrary("javaee", (getLibPath() +"/javaee.jar").replace(File.separator, "/"));
    }
  }

  protected void configureInspections() {
    myFixture.enableInspections(WebServicesPluginSettings.getInspectons());
  }
}
