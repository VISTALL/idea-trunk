// Generated on Mon Mar 17 18:02:09 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package org.jetbrains.idea.maven.dom.model;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomElement;

/**
 * http://maven.apache.org/POM/4.0.0:BuildBase interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:BuildBase documentation</h3>
 * 3.0.0+
 * </pre>
 */
public interface MavenDomBuildBase extends MavenDomElement {

  /**
   * Returns the value of the defaultGoal child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:defaultGoal documentation</h3>
   * 3.0.0+
   * </pre>
   *
   * @return the value of the defaultGoal child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  GenericDomValue<String> getDefaultGoal();

  /**
   * Returns the value of the resources child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:resources documentation</h3>
   * 3.0.0+
   * </pre>
   *
   * @return the value of the resources child.
   */
  @NotNull
  MavenDomResources getResources();

  /**
   * Returns the value of the testResources child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:testResources documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the testResources child.
   */
  @NotNull
  MavenDomTestResources getTestResources();

  /**
   * Returns the value of the directory child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:directory documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the directory child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  GenericDomValue<String> getDirectory();

  /**
   * Returns the value of the finalName child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:finalName documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the finalName child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  GenericDomValue<String> getFinalName();

  /**
   * Returns the value of the filters child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:filters documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the filters child.
   */
  @NotNull
  MavenDomFilters getFilters();

  /**
   * Returns the value of the pluginManagement child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:pluginManagement documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the pluginManagement child.
   */
  @NotNull
  MavenDomPluginManagement getPluginManagement();

  /**
   * Returns the value of the plugins child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:plugins documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the plugins child.
   */
  @NotNull
  MavenDomPlugins getPlugins();
}
