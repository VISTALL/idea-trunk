// Generated on Mon Mar 17 18:02:09 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package org.jetbrains.idea.maven.dom.model;

import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomElement;

/**
 * http://maven.apache.org/POM/4.0.0:Plugin interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:Plugin documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface MavenDomPlugin extends MavenDomElement, MavenDomArtifactCoordinates {
  @Required(value = false, nonEmpty = true)
  GenericDomValue<String> getGroupId();

  @Required(value = false, nonEmpty = true)
  GenericDomValue<String> getVersion();

  /**
   * Returns the value of the extensions child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:extensions documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the extensions child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  GenericDomValue<Boolean> getExtensions();

  /**
   * Returns the value of the executions child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:executions documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the executions child.
   */
  @NotNull
  MavenDomExecutions getExecutions();

  /**
   * Returns the value of the dependencies child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:dependencies documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the dependencies child.
   */
  @NotNull
  MavenDomDependencies getDependencies();

  /**
   * Returns the value of the goals child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:goals documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the goals child.
   */
  @NotNull
  MavenDomGoals getGoals();

  /**
   * Returns the value of the inherited child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:inherited documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the inherited child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  GenericDomValue<Boolean> getInherited();

  /**
   * Returns the value of the configuration child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:configuration documentation</h3>
   * 0.0.0+
   * </pre>
   *
   * @return the value of the configuration child.
   */
  @NotNull
  MavenDomConfiguration getConfiguration();
}
