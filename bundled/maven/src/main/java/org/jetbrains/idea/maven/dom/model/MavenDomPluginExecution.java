// Generated on Mon Mar 17 18:02:09 MSK 2008
// DTD/Schema  :    http://maven.apache.org/POM/4.0.0

package org.jetbrains.idea.maven.dom.model;

import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.Required;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.dom.MavenDomElement;
import org.jetbrains.idea.maven.dom.converters.MavenPhaseConverter;

/**
 * http://maven.apache.org/POM/4.0.0:PluginExecution interface.
 * <pre>
 * <h3>Type http://maven.apache.org/POM/4.0.0:PluginExecution documentation</h3>
 * 4.0.0
 * </pre>
 */
public interface MavenDomPluginExecution extends MavenDomElement {

  /**
   * Returns the value of the id child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:id documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the id child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  GenericDomValue<String> getId();

  /**
   * Returns the value of the phase child.
   * <pre>
   * <h3>Element http://maven.apache.org/POM/4.0.0:phase documentation</h3>
   * 4.0.0
   * </pre>
   *
   * @return the value of the phase child.
   */
  @NotNull
  @Required(value = false, nonEmpty = true)
  @Convert(MavenPhaseConverter.class)
  GenericDomValue<String> getPhase();

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
  GenericDomValue<String> getInherited();

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
